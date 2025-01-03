package club.arson.ogsoVelocity

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import org.slf4j.Logger

class PlayerLifecycleListener(val logger: Logger) {
    @Subscribe(order = PostOrder.FIRST)
    fun onServerPreConnectEvent(event: ServerPreConnectEvent) {
        logger.info("Handling ServerPreConnectEvent for ${event.player.username} from ${event.previousServer?.serverInfo?.name ?: "No Previous Server"} to ${event.originalServer.serverInfo.name}")
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onDisconnectEvent(event: DisconnectEvent) {
        logger.info("Handling DisconnectEvent for ${event.player.username}")
    }
}