package club.arson.impulse

import club.arson.impulse.TransferStage.*
import club.arson.impulse.api.events.*
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.permission.Tristate
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.slf4j.Logger
import java.util.*

/**
 * Stages to display for the transfer process
 *
 * @property INITIALIZING the transfer is initializing, the destination server state is unknown
 * @property STARTING the destination server is starting
 * @property STARTED the destination server has started
 * @property AWAITING_READY we are waiting for the destination server to report ready to accept players
 * @property TRANSFERRING the player is being transferred
 * @property ERROR an error occurred during the transfer
 */
enum class TransferStage {
    INITIALIZING,
    STARTING,
    STARTED,
    AWAITING_READY,
    TRANSFERRING,
    ERROR
}

/**
 * A data class to hold the active transfer information
 */
private data class ActiveTransfer(val from: RegisteredServer, val to: RegisteredServer, val bar: BossBar)

/**
 * Listens for transfer events and updates the player's boss bar with the current transfer status
 *
 * @param logger the logger to write messages to
 * @constructor creates a new TransferStatusListener registered with a logger.
 */
class TransferStatusListener @Inject constructor(private val logger: Logger) {
    private val playersAwaitingTransfer: MutableMap<String, ActiveTransfer> =
        Collections.synchronizedMap(mutableMapOf())

    companion object {
        private const val DISPLAY_TRANSFER_STATUS_PERMISSION_SCOPE = "impulse.transfer.displayStatus"
    }

    /**
     * Sets the boss bar for the player to display the current transfer status
     *
     * @param bar the boss bar to update
     * @param stage the current stage of the transfer
     * @param serverName the name of the server the player is transferring to
     */
    private fun setTransferStatusBar(bar: BossBar, stage: TransferStage, serverName: String) {
        val transferMessages = ServiceRegistry.instance.configManager?.messages?.transferDisplayMessages

        val title: String
        val color: BossBar.Color
        val progress: Float

        when (stage) {
            INITIALIZING -> {
                title = transferMessages?.initializing ?: ""
                color = BossBar.Color.BLUE
                progress = 0.17f
            }

            STARTING -> {
                title = transferMessages?.starting ?: ""
                color = BossBar.Color.BLUE
                progress = 0.34f
            }

            STARTED -> {
                title = transferMessages?.started ?: ""
                color = BossBar.Color.BLUE
                progress = 0.51f
            }

            AWAITING_READY -> {
                title = transferMessages?.awaitingReady ?: ""
                color = BossBar.Color.BLUE
                progress = 0.67f
            }

            TRANSFERRING -> {
                title = transferMessages?.transferring ?: ""
                color = BossBar.Color.GREEN
                progress = 1.0f
            }

            ERROR -> {
                title = transferMessages?.error ?: ""
                color = BossBar.Color.RED
                progress = 1.0f
            }
        }
        bar.name(
            MiniMessage.miniMessage()
                .deserialize(title, Placeholder.component("server", Component.text(serverName)))
        )
        bar.color(color)
        bar.progress(progress)
    }

    private fun updatePlayersForServer(stage: TransferStage, server: RegisteredServer) {
        logger.debug("Updating players for server {} with stage {}", server.serverInfo.name, stage)
        playersAwaitingTransfer
            .values
            .filter { it.to == server }
            .forEach { transfer ->
                setTransferStatusBar(transfer.bar, stage, server.serverInfo.name)
            }
    }

    /**
     * Updates the player's boss bar to [STARTING].
     *
     * @param event the server pre start event
     */
    @Subscribe
    fun onServerPreStartEvent(event: ServerPreStartEvent) = updatePlayersForServer(STARTING, event.server)

    /**
     * Updates the player's boss bar to [STARTED] if the server started successfully, otherwise [ERROR].
     *
     * @param event the server started event
     */
    @Subscribe
    fun onServerStartedEvent(event: ServerStartedEvent) = updatePlayersForServer(
        if (event.success) STARTED else ERROR,
        event.server
    )

    /**
     * Updates the player's boss bar to [AWAITING_READY].
     *
     * @param event the awaiting server ready event
     */
    @Subscribe
    fun onAwaitServerReady(event: AwaitingServerReadyEvent) =
        updatePlayersForServer(AWAITING_READY, event.server)

    /**
     * Updates the player's boss bar to [ERROR].
     *
     * @param event the impulse player transfer error event
     */
    @Subscribe
    fun onImpulsePlayerTransferErrorEvent(event: ImpulsePlayerTransferErrorEvent) = updatePlayersForServer(
        ERROR,
        event.nextServer
    )

    /**
     * Updates the player's boss bar to [TRANSFERRING].
     */
    @Subscribe
    fun onImpulsePlayerTransferEvent(event: ImpulsePlayerTransferEvent) =
        updatePlayersForServer(TRANSFERRING, event.nextServer)

    /**
     * Removes the player's boss bar when they have connected to the server.
     */
    @Subscribe
    fun onServerPostConnectEvent(event: ServerPostConnectEvent) {
        playersAwaitingTransfer[event.player.username]?.let { transfer ->
            event.player.hideBossBar(transfer.bar)
            playersAwaitingTransfer.remove(event.player.username)
        }
    }

    /**
     * Listens for the [PreImpulsePlayerTransferEvent] and sets the player's boss bar to [INITIALIZING].
     */
    @Subscribe
    fun onPreImpulsePlayerTransferEvent(event: PreImpulsePlayerTransferEvent) {
        val prev = event.previousServer
        val serverManager = ServiceRegistry.instance.serverManager
        // Bail if the player is not transferring, we explicitly don't have permission to add the bar, or the server has disabled transfer displays
        if (prev == null
            || event.player.getPermissionValue(DISPLAY_TRANSFER_STATUS_PERMISSION_SCOPE) == Tristate.FALSE
            || ServiceRegistry.instance.configManager?.disableTransferDisplays == true || serverManager?.servers?.get(
                prev.serverInfo.name
            )?.config?.transferSettings?.enableDisplays == false
        ) {
            return
        }
        logger.debug("Player ${event.player.username} is transferring from ${prev.serverInfo.name} to ${event.nextServer.serverInfo.name}")

        playersAwaitingTransfer[event.player.username]?.let { transfer ->
            if (transfer.from == prev && transfer.to == event.nextServer) {
                return
            } else {
                event.player.hideBossBar(transfer.bar)
                playersAwaitingTransfer.remove(event.player.username)
            }
        }

        val message =
            ServiceRegistry.instance.configManager?.messages?.transferDisplayMessages?.initializing ?: ""
        val bossBar = BossBar.bossBar(
            Component.text(message),
            0.0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.NOTCHED_6
        )
        event.player.showBossBar(bossBar)

        playersAwaitingTransfer[event.player.username] = ActiveTransfer(prev, event.nextServer, bossBar)
    }
}