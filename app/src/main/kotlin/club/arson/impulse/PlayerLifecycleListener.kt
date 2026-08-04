/*
 *  Impulse Server Manager for Velocity
 *  Copyright (c) 2025 Dabb1e
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.*
 */

package club.arson.impulse
import com.google.inject.Inject
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent.ServerResult
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.slf4j.Logger
import java.time.Duration

/**
 * Listens for connect and disconnect events so managed servers can be started
 * before transfer and stopped after becoming empty.
 */
class PlayerLifecycleListener @Inject constructor(
    private val logger: Logger
) {
    private val miniMessage = MiniMessage.miniMessage()

    private fun getMM(message: String?): Component {
        return miniMessage.deserialize(message ?: "<red>Unknown error</red>")
    }

    private fun showStartingTitle(player: Player, serverName: String) {
        player.showTitle(
            Title.title(
                getMM("<gold>Starting $serverName</gold>"),
                getMM("<gray>Please wait while the server comes online...</gray>"),
                Title.Times.times(
                    Duration.ofMillis(500),
                    Duration.ofSeconds(10),
                    Duration.ofMillis(1000)
                )
            )
        )
    }

    private fun showTransferTitle(player: Player, serverName: String) {
        player.showTitle(
            Title.title(
                getMM("<green>Server Ready</green>"),
                getMM("<gray>Transferring to $serverName...</gray>"),
                Title.Times.times(
                    Duration.ofMillis(100),
                    Duration.ofSeconds(2),
                    Duration.ofMillis(300)
                )
            )
        )
    }

    private fun showFailureTitle(player: Player, serverName: String) {
        player.showTitle(
            Title.title(
                getMM("<red>Server Unavailable</red>"),
                getMM("<gray>Unable to connect to $serverName.</gray>"),
                Title.Times.times(
                    Duration.ofMillis(100),
                    Duration.ofSeconds(3),
                    Duration.ofMillis(400)
                )
            )
        )
    }

    /**
     * Either lets Velocity continue its initial try list or denies a transfer
     * and sends the configured message to the player.
     */
    private fun handleTimeout(
        player: Player,
        previousServer: RegisteredServer?,
        message: String? = null
    ): ServerResult {
        if (previousServer == null) {
            throw IllegalStateException(
                "Server hit timeout while starting: ${message ?: "Unknown error"}"
            )
        } else {
            player.sendMessage(getMM(message))
        }

        return ServerResult.denied()
    }

    /**
     * Processes a connection attempt to a managed server.
     *
     * This remains public because the existing unit tests invoke it directly.
     */
    fun handlePlayerConnectEvent(event: ServerPreConnectEvent) {
        val serverName = event.originalServer.serverInfo.name
        val server = ServiceRegistry.instance.serverManager?.getServer(serverName)

        if (server != null) {
            val previousManagedServer =
                if (event.previousServer != null) {
                    ServiceRegistry.instance.serverManager
                        ?.getServer(event.previousServer!!.serverInfo.name)
                } else {
                    null
                }

            var isRunning = server.isRunning()

            if (!isRunning && server.config.lifecycleSettings.allowAutoStart) {
                showStartingTitle(event.player, serverName)

                server.startServer().onSuccess {
                    logger.debug("Server started successfully, allowing connection")
                    isRunning = true
                }.onFailure {
                    logger.warn("Error: failed to start server, rejecting connection")
                    logger.warn(it.message)
                    showFailureTitle(event.player, serverName)
                }
            }

            if (isRunning) {
                server.awaitReady().onSuccess {
                    logger.trace("Server reporting ready, transferring player")
                    showTransferTitle(event.player, serverName)
                    previousManagedServer?.handleDisconnect(event.player.username)
                }.onFailure {
                    logger.debug("Server failed to report ready, rejecting connection")
                    showFailureTitle(event.player, serverName)

                    event.result = handleTimeout(
                        event.player,
                        event.previousServer,
                        ServiceRegistry.instance.configManager?.messages?.startupError
                    )
                }
            } else if (!server.config.lifecycleSettings.allowAutoStart) {
                event.result = handleTimeout(
                    event.player,
                    event.previousServer,
                    ServiceRegistry.instance.configManager?.messages?.autoStartDisabled
                )
            } else {
                event.result = handleTimeout(
                    event.player,
                    event.previousServer,
                    ServiceRegistry.instance.configManager?.messages?.startupError
                )
            }
        } else {
            logger.debug("Server is not managed by us, taking no action")
        }
    }

    /**
     * Starts a managed destination server before Velocity completes transfer.
     */
    @Subscribe(order = PostOrder.FIRST)
    fun onServerPreConnectEvent(event: ServerPreConnectEvent): EventTask {
        logger.debug(
            "Handling ServerPreConnectEvent for ${event.player.username} " +
                "from ${event.previousServer?.serverInfo?.name ?: "No Previous Server"} " +
                "to ${event.originalServer.serverInfo.name}"
        )

        return EventTask.async {
            handlePlayerConnectEvent(event)
        }
    }

    /**
     * Schedules shutdown when the final player leaves a managed server.
     */
    @Subscribe(order = PostOrder.LAST)
    fun onDisconnectEvent(event: DisconnectEvent) {
        runCatching {
            event.player.currentServer.get().server
        }.onSuccess {
            ServiceRegistry.instance.serverManager
                ?.getServer(it.serverInfo.name)
                ?.handleDisconnect(event.player.username)
        }.onFailure {
            logger.debug(
                "unable to determine tha disconnect server for ${event.player.username}"
            )
        }
    }
}
