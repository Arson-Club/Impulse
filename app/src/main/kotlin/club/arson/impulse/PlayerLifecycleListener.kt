/*
 *  Impulse Server Manager for Velocity
 *  Copyright (c) 2025  Dabb1e
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package club.arson.impulse

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.slf4j.Logger

/**
 * Listens for player lifecycle events and processes them
 *
 * Listens for connect and disconnect events so that we can start and stop servers
 * @param logger the logger to write messages to
 * @constructor creates a new PlayerLifecycleListener registered with an optional logger.
 */
class PlayerLifecycleListener(private val logger: Logger? = null) {
    private fun getMM(message: String?): Component {
        return MiniMessage
            .miniMessage()
            .deserialize(message ?: "<red>Unknown error</red>")
    }

    /**
     * Handles the ServerPreConnectEvent
     *
     * This event is fired when a player is about to connect to a server. We use this event to start the server if it is not already running.
     * @param event the ServerPreConnectEvent
     * @return an EventTask that will start the server if it is not already running
     * @see [ServerPreConnectEvent](https://jd.papermc.io/velocity/3.4.0/com/velocitypowered/api/event/player/ServerPreConnectEvent.html)
     */
    @Subscribe(order = PostOrder.FIRST)
    fun onServerPreConnectEvent(event: ServerPreConnectEvent): EventTask {
        logger?.debug("Handling ServerPreConnectEvent for ${event.player.username} from ${event.previousServer?.serverInfo?.name ?: "No Previous Server"} to ${event.originalServer.serverInfo.name}")
        val prevServer = event.previousServer
        return EventTask.async {
            val server = ServiceRegistry.instance.serverManager?.getServer(event.originalServer.serverInfo.name)
            if (server != null) {
                var isRunning = server.isRunning()

                // if the server is not running and auto start is enabled, start the server
                if (!isRunning && server.config.lifecycleSettings.allowAutoStart) {
                    server.startServer().onSuccess {
                        logger?.trace("Server started successfully, allowing connection")
                        isRunning = true
                    }.onFailure {
                        logger?.warn("Error: failed to start server, rejecting connection")
                        logger?.warn(it.message)
                        event.result = ServerPreConnectEvent.ServerResult.denied()
                        event.player.disconnect(getMM(ServiceRegistry.instance.configManager?.messages?.startupError))
                    }
                }

                // If we are started, await ready and transfer the player
                if (isRunning) {
                    server.awaitReady().onSuccess {
                        logger?.trace("Server reporting ready, transferring player")
                        if (prevServer != null) {
                            ServiceRegistry.instance.serverManager?.getServer(prevServer.serverInfo.name)
                                ?.handleDisconnect(event.player.username)
                        }
                    }.onFailure {
                        event.result = ServerPreConnectEvent.ServerResult.denied()
                        event.player.disconnect(getMM(ServiceRegistry.instance.configManager?.messages?.startupError))
                    }
                } else if (!server.config.lifecycleSettings.allowAutoStart) {
                    // If we are not started and auto start is disabled, reject the connection with the correct message
                    event.result = ServerPreConnectEvent.ServerResult.denied()
                    event.player.disconnect(getMM(ServiceRegistry.instance.configManager?.messages?.autoStartDisabled))
                } else {
                    // Otherwise reject with an unknown error
                    event.result = ServerPreConnectEvent.ServerResult.denied()
                    event.player.disconnect(getMM(null))
                }
            } else {
                logger?.debug("Server is not managed by us, taking no action")
            }
        }
    }

    /**
     * Handles the DisconnectEvent
     *
     * This is fired when a player disconnects from the server. We will use this to schedule a shutdown if the server is empty.
     * @param event the DisconnectEvent
     * @see [DisconnectEvent](https://jd.papermc.io/velocity/3.4.0/com/velocitypowered/api/event/connection/DisconnectEvent.html)
     */
    @Subscribe(order = PostOrder.LAST)
    fun onDisconnectEvent(event: DisconnectEvent) {
        runCatching {
            event.player.currentServer.get().server
        }.onSuccess {
            ServiceRegistry.instance.serverManager?.getServer(it.serverInfo.name)
                ?.handleDisconnect(event.player.username)
        }.onFailure {
            logger?.warn("unable to determine tha disconnect server for ${event.player.username}")
        }
    }
}