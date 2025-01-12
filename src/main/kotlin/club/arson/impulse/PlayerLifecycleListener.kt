package club.arson.impulse

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.slf4j.Logger

class PlayerLifecycleListener(val logger: Logger) {
    @Subscribe(order = PostOrder.FIRST)
    fun onServerPreConnectEvent(event: ServerPreConnectEvent): EventTask {
        logger.debug("Handling ServerPreConnectEvent for ${event.player.username} from ${event.previousServer?.serverInfo?.name ?: "No Previous Server"} to ${event.originalServer.serverInfo.name}")
        val prevServer = event.previousServer
        return EventTask.async({
            val server = ServiceRegistry.instance.serverManager?.getServer(event.originalServer.serverInfo.name)
            if (server != null) {
                server.startServer().onSuccess {
                    logger.trace("Server started successfully, allowing connection")
                    it.awaitReady()
                    if (prevServer != null) {
                        ServiceRegistry.instance.serverManager?.getServer(prevServer.serverInfo.name)?.handleDisconnect(event.player.username)
                    }
                }.onFailure {
                    logger.warn("Error: failed to start server, rejecting connection")
                    logger.warn(it.message)
                    event.result = ServerPreConnectEvent.ServerResult.denied()
                    event.player.disconnect(
                        MiniMessage
                            .miniMessage()
                            .deserialize(ServiceRegistry.instance.configManager?.messages?.startupError ?: "<red>Unknown error</red>")
                    )
                }
            } else {
                logger.debug("Server is not managed by us, taking no action")
            }
        })
    }

    @Subscribe(order = PostOrder.LAST)
    fun onDisconnectEvent(event: DisconnectEvent) {
        runCatching {
            event.player.currentServer.get().server
        }.onSuccess {
            ServiceRegistry.instance.serverManager?.getServer(it.serverInfo.name)?.handleDisconnect(event.player.username)
        }.onFailure {
            logger.warn("unable to determine tha disconnect server for ${event.player.username}")
        }
    }
}