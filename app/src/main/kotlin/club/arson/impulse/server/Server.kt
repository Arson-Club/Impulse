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

package club.arson.impulse.server

import club.arson.impulse.Impulse
import club.arson.impulse.ServiceRegistry
import club.arson.impulse.api.config.ReconcileBehavior
import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.config.ShutdownBehavior
import club.arson.impulse.api.server.Broker
import com.google.inject.Inject
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

/**
 * Represents a managed server instance
 *
 * @property broker the broker instance used to control the server
 * @property serverRef a reference to the server record in Velocity
 * @property config the server configuration
 * @property proxyServer a reference to the Velocity proxy server
 * @property plugin a reference to the Impulse plugin
 * @property logger an optional logger instance
 * @constructor creates a new server instance
 */
class Server @Inject constructor(
    private val broker: Broker,
    val serverRef: RegisteredServer,
    var config: ServerConfig,
    private val proxyServer: ProxyServer,
    private val plugin: Impulse,
    private val logger: Logger? = null
) {
    private var shutdownTask: ScheduledTask? = null
    private var pendingReconciliationTask: ScheduledTask? = null
    private var pendingReconciliationHandler: Runnable? = null
    var pinned = false

    /**
     * Registers the server with the event manager to handle configuration updates
     */
    init {
        proxyServer.eventManager.register(plugin, this)
    }

    private fun showReconciliationTitle(audience: Audience, stay: Long) {
        val messages = ServiceRegistry.instance.configManager?.messages
        audience.showTitle(
            Title.title(
                MiniMessage.miniMessage().deserialize(messages?.reconcileRestartTitle ?: "Server Restarting"),
                MiniMessage.miniMessage()
                    .deserialize(messages?.reconcileRestartMessage ?: "Server is restarting to apply changes"),
                Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(stay), Duration.ofSeconds(0))
            )
        )
    }

    /**
     * Gets the current status of the server
     * @return the server status
     */
    fun getStatus() = broker.getStatus()

    /**
     * Starts the server
     * @return a result containing the server or an error
     */
    fun startServer(): Result<Server> {
        shutdownTask?.cancel()
        shutdownTask = null
        return broker.startServer().fold(
            onSuccess = { Result.success(this) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Stops the server
     * @return a result containing the server or an error
     */
    fun stopServer(): Result<Server> {
        return if (!pinned) {
            broker.stopServer().fold(
                onSuccess = {
                    if (config.lifecycleSettings.reconciliationBehavior == ReconcileBehavior.ON_STOP && pendingReconciliationHandler != null) {
                        pendingReconciliationHandler?.run()
                        broker.stopServer() // Brokers should not restart a stopped server on reconcile, but just make sure it is stopped
                    }
                    Result.success(this)
                },
                onFailure = { Result.failure(it) }
            )
        } else {
            Result.failure(Throwable("Server ${serverRef.serverInfo.name} is pinned and cannot be stopped"))
        }
    }

    /**
     * Checks if the server is running
     * @return true if the server is running, false otherwise
     */
    fun isRunning() = broker.isRunning()

    /**
     * Schedules a shutdown of the server
     *
     * Uses the Velocity event system to register a future shutdown task.
     * @param delay the delay in seconds before the server is shutdown
     * @return a result containing the server or an error
     */
    fun scheduleShutdown(delay: Long = config.lifecycleSettings.timeouts.inactiveGracePeriod): Result<Server> {
        if (delay >= 0 && shutdownTask == null && isRunning()) {
            logger?.debug("Server ${serverRef.serverInfo.name} has no players, scheduling shutdown")
            shutdownTask = proxyServer
                .scheduler
                .buildTask(plugin, Runnable {
                    if (serverRef.playersConnected.isEmpty()) {
                        logger?.info("Server ${serverRef.serverInfo.name} has no players, stopping")
                        when (config.lifecycleSettings.shutdownBehavior) {
                            ShutdownBehavior.STOP -> stopServer()
                            ShutdownBehavior.REMOVE -> removeServer()
                        }
                    }
                    shutdownTask = null
                }).delay(config.lifecycleSettings.timeouts.inactiveGracePeriod, TimeUnit.SECONDS)
                .schedule()
        }
        return Result.success(this)
    }

    /**
     * Removes the server from the broker
     * @return a result containing the server or an error
     */
    fun removeServer(): Result<Server> {
        return if (!pinned) {
            broker.removeServer().fold(
                onSuccess = { Result.success(this) },
                onFailure = { Result.failure(it) }
            )
        } else {
            Result.failure(Throwable("Server ${serverRef.serverInfo.name} is pinned and cannot be removed"))
        }
    }

    /**
     * Reconciles the server with a new configuration
     *
     * This method will cancel any pending reconciliation tasks and set up a new one if needed.
     * @param newConfig the new server configuration
     * @return a result containing the server or an error
     */
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
            } ?: run {
                config = newConfig
                return@run null
            }
        }.onFailure {
            return Result.failure(it)
        }

        // If we have work to do, and we're forcing, schedule it
        if (newConfig.lifecycleSettings.reconciliationBehavior == ReconcileBehavior.FORCE && pendingReconciliationHandler != null) {
            pendingReconciliationTask = proxyServer.scheduler
                .buildTask(plugin, Runnable {
                    pendingReconciliationHandler?.run()
                    pendingReconciliationTask = null
                })
                .delay(Duration.ofSeconds(newConfig.lifecycleSettings.timeouts.reconciliationGracePeriod))
                .schedule()
            showReconciliationTitle(serverRef, newConfig.lifecycleSettings.timeouts.reconciliationGracePeriod)
        }
        return Result.success(this)
    }

    /**
     * Waits for the server to respond to Minecraft Pings
     *
     * This method is to wait for the server to be ready to accept connections, as opposed to [isRunning] which only checks
     * if the broker is reporting a "running" status.
     */
    fun awaitReady(): Result<Unit> {
        val startTime = System.currentTimeMillis()
        val timeout = config.lifecycleSettings.timeouts.startup * 1000
        var isReady = false

        while (!isReady && isRunning() && System.currentTimeMillis() - startTime < timeout) {
            try {
                serverRef.ping().get()
                isReady = true
            } catch (e: Exception) {
                Thread.sleep(200)
            }
        }

        return if (isReady) Result.success(Unit) else Result.failure(Throwable("Server ${serverRef.serverInfo.name} failed to start (timeout)"))
    }

    /**
     * Handles a player disconnect event
     *
     * If the disconnecting player is the last one on the server, and the server is configured to shut down when empty,
     * this method will schedule a shutdown.
     * @param user the username of the disconnecting player
     */
    fun handleDisconnect(user: String) {
        val playerCount = serverRef.playersConnected.filter { it.username != user }.size
        if (playerCount <= 0 && config.lifecycleSettings.allowAutoStop && !pinned) {
            scheduleShutdown()
        }
    }

    /**
     * Handles the ServerPostConnectEvent
     *
     * Currently, this is used to display the reconciliation title to players who join during the reconciliation grace period.
     * @param event the ServerPostConnectEvent
     */
    @Subscribe
    @Suppress("UnstableApiUsage")
    fun onPlayerJoin(event: ServerPostConnectEvent) {
        // Bail if the player is not connecting to this server
        val currentServer = event.player.currentServer.orElse(null) ?: return
        if (currentServer.serverInfo.name != config.name) return

        pendingReconciliationTask?.let {
            showReconciliationTitle(
                event.player,
                config.lifecycleSettings.timeouts.reconciliationGracePeriod
            )
        }
    }
}