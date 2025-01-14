package club.arson.impulse.server

import club.arson.impulse.Impulse
import club.arson.impulse.ServiceRegistry
import club.arson.impulse.config.ServerConfig
import club.arson.impulse.config.ShutdownBehavior
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.scheduler.ScheduledTask
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.slf4j.Logger
import java.time.Duration
import java.util.concurrent.TimeUnit

class Server(val broker: ServerBroker, val serverRef: RegisteredServer, var config: ServerConfig, val proxyServer: ProxyServer, val plugin: Impulse, val logger: Logger? = null) {
    var shutdownTask: ScheduledTask? = null
    var pendingReconciliationTask: ScheduledTask? = null
    var pendingReconciliationHandler: Runnable? = null

    init {
        proxyServer.eventManager.register(plugin, this)
    }

    private fun _showReconciliationTitle(audience: Audience, stay: Long ) {
        val messages = ServiceRegistry.instance.configManager?.messages
        audience.showTitle(Title.title(
            MiniMessage.miniMessage().deserialize(messages?.reconcileRestartTitle ?: "Server Restarting"),
            MiniMessage.miniMessage().deserialize(messages?.reconcileRestartMessage ?: "Server is restarting to apply changes"),
            Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(stay), Duration.ofSeconds(0))
        ))
    }

    fun getStatus() = broker.getStatus()

    fun startServer(): Result<Server> {
        shutdownTask?.cancel()
        shutdownTask = null
        return broker.startServer().fold(
            onSuccess = { Result.success(this) },
            onFailure = { Result.failure(it) }
        )
    }

    fun stopServer(): Result<Server> {
        return broker.stopServer().fold(
            onSuccess = {
                if (!config.forceServerReconciliation && pendingReconciliationHandler != null) {
                    pendingReconciliationHandler?.run()
                }
                Result.success(this)
                        },
            onFailure = { Result.failure(it) }
        )
    }

    fun isRunning() = broker.isRunning()

    fun scheduleShutdown(delay: Long = config.inactiveTimeout): Result<Server> {
        if (delay > 0 && shutdownTask == null && isRunning()) {
            logger?.debug("Server ${serverRef.serverInfo.name} has no players, scheduling shutdown")
            shutdownTask = proxyServer
                .scheduler
                .buildTask(plugin, Runnable {
                    if (serverRef.playersConnected.isEmpty()) {
                        logger?.info("Server ${serverRef.serverInfo.name} has no players, stopping")
                        when (config.shutdownBehavior) {
                            ShutdownBehavior.STOP -> stopServer()
                            ShutdownBehavior.REMOVE -> removeServer()
                        }
                    }
                    shutdownTask = null
                }).delay(config.inactiveTimeout, TimeUnit.SECONDS)
                .schedule()
        }
        return Result.success(this)
    }

    fun removeServer(): Result<Server> {
        return broker.removeServer().fold(
            onSuccess = { Result.success(this) },
            onFailure = { Result.failure(it) }
        )
    }

    fun reconcile(newConfig: ServerConfig): Result<Server> {
        // Cancel any other reconciliation tasks
        pendingReconciliationTask?.cancel()
        pendingReconciliationTask = null

        // See if we have any reconciliation work to do, if so set up our handler
        broker.reconcile(newConfig).onSuccess { handler ->
            pendingReconciliationHandler = handler?.let {
                Runnable {
                    it.run()
                    config = newConfig
                    pendingReconciliationHandler = null
                }
            }
        }.onFailure {
            return Result.failure(it)
        }

        // If we have work to do, and we're forcing, schedule it
        if (newConfig.forceServerReconciliation && pendingReconciliationHandler != null) {
            pendingReconciliationTask = proxyServer.scheduler
                .buildTask(plugin, Runnable {
                    pendingReconciliationHandler?.run()
                    pendingReconciliationTask = null
                })
                .delay(Duration.ofSeconds(newConfig.serverReconciliationGracePeriod))
                .schedule()
            _showReconciliationTitle(serverRef, newConfig.serverReconciliationGracePeriod)
        }
        return Result.success(this)
    }

    fun awaitReady(): Result<Unit> {
        val startTime = System.currentTimeMillis()
        val timeout = config.startupTimeout * 1000
        var isReady = false

        while (!isReady && System.currentTimeMillis() - startTime < timeout) {
            try {
                serverRef.ping().get()
                isReady = true
            } catch(e: Exception) {
                Thread.sleep(200)
            }
        }

        return if (isReady) Result.success(Unit) else Result.failure(Throwable("Server ${serverRef.serverInfo.name} failed to start (timeout)"))
    }

    fun handleDisconnect(user: String) {
        val playerCount = serverRef.playersConnected.filter { it.username != user }.size
        if (playerCount <= 0 && config.inactiveTimeout > 0) {
            scheduleShutdown()
        }
    }

    @Subscribe
    fun onPlayerJoin(event: ServerPostConnectEvent) {
        // Bail if the player is not connecting to this server
        if (event.player.currentServer.get().serverInfo?.name != config.name) {
            return
        }

        if (pendingReconciliationTask != null) {
            _showReconciliationTitle(
                event.player,
                config.serverReconciliationGracePeriod
            )
        }
    }
}