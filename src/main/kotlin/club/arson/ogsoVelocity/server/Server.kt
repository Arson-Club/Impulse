package club.arson.ogsoVelocity.server

import club.arson.ogsoVelocity.OgsoVelocity
import club.arson.ogsoVelocity.ServiceRegistry
import club.arson.ogsoVelocity.config.ServerConfig
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

class Server(val broker: ServerBroker, val serverRef: RegisteredServer, var config: ServerConfig, val proxyServer: ProxyServer, val plugin: OgsoVelocity, val logger: Logger? = null) {
    var shutdownTask: ScheduledTask? = null
    var reconciliationEvent: ScheduledTask? = null

    init {
        proxyServer.eventManager.register(plugin, this)
    }

    private fun _showTitle(audience: Audience, title: String, subtitle: String, stay: Long) {
        audience.showTitle(Title.title(
            MiniMessage.miniMessage().deserialize(title),
            MiniMessage.miniMessage().deserialize(subtitle),
            Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(stay), Duration.ofSeconds(0))
        ))
    }

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
            onSuccess = { Result.success(this) },
            onFailure = { Result.failure(it) }
        )
    }

    fun scheduleStop(delay: Long = config.inactiveTimeout): Result<Server> {
        if (delay > 0 && shutdownTask == null) {
            logger?.info("Server ${serverRef.serverInfo.name} has no players, scheduling shutdown")
            shutdownTask = proxyServer
                .scheduler
                .buildTask(plugin, Runnable {
                    if (serverRef.playersConnected.isEmpty()) {
                        logger?.info("Server ${serverRef.serverInfo.name} has no players, stopping")
                        stopServer()
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
        var result: Result<Server> = Result.failure(Throwable("Reconciliation failed"))
        broker.reconcile(newConfig).onSuccess { reconcileHandler ->
            if (reconcileHandler != null) {
                if (config.forceServerReconciliation) {

                }
                reconciliationEvent?.cancel()
                reconciliationEvent = proxyServer.scheduler
                    .buildTask(plugin, Runnable {
                        reconcileHandler.run()
                        config = newConfig
                        reconciliationEvent = null
                    })
                    .delay(Duration.ofSeconds(newConfig.serverReconciliationGracePeriod))
                    .schedule()
                val messages = ServiceRegistry.instance.configManager?.messages
                _showTitle(
                    serverRef,
                    messages?.reconcileRestartTitle ?: "Server Restarting",
                    messages?.reconcileRestartMessage ?: "Server is restarting to apply changes",
                    config.serverReconciliationGracePeriod
                )
            } else {
                config = newConfig
            }
            result = Result.success(this)
        }
        return result
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
            scheduleStop()
        }
    }

    @Subscribe
    fun onPlayerJoin(event: ServerPostConnectEvent) {
        // Bail if the player is not connecting to this server
        if (event.player.currentServer.get().serverInfo?.name != config.name) {
            return
        }

        if (reconciliationEvent != null) {
            val messages = ServiceRegistry.instance.configManager?.messages
            _showTitle(
                event.player,
                messages?.reconcileRestartTitle ?: "Server Restarting",
                messages?.reconcileRestartMessage ?: "Server is restarting to apply changes",
                config.serverReconciliationGracePeriod
            )
        }
    }
}