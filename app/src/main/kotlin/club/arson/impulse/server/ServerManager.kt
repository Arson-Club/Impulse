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
import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.events.ConfigReloadEvent
import club.arson.impulse.api.server.Broker
import club.arson.impulse.inject.modules.ServerModule
import com.google.inject.Inject
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import org.slf4j.Logger
import java.util.concurrent.TimeUnit

class ServerManager @Inject constructor(
    private val proxy: ProxyServer,
    private val plugin: Impulse,
    private val logger: Logger
) {
    var servers = mutableMapOf<String, Server>()
    private var maintenanceInterval: Long
    private var maintenanceTask: ScheduledTask

    init {
        proxy.eventManager.register(plugin, this)
        val config = ServiceRegistry.instance.configManager
        reconcileServers(emptyList(), config?.servers ?: emptyList())
        maintenanceInterval = config?.maintenanceInterval ?: 300
        maintenanceTask = proxy.scheduler
            .buildTask(plugin, this::serverMaintenance)
            .repeat(maintenanceInterval, TimeUnit.SECONDS)
            .schedule()
    }

    fun serverMaintenance() {
        servers.values.forEach { server ->
            if (server.serverRef.playersConnected.isEmpty() && server.config.lifecycleSettings.allowAutoStop && !server.pinned) {
                logger.trace("Found empty server ${server.serverRef.serverInfo.name}")
                server.scheduleShutdown()
            }
        }
    }

    fun getServer(name: String): Server? {
        return servers[name]
    }

    private fun createServer(serverName: String, config: ServerConfig): Result<Unit> {
        val brokerFactory = ServiceRegistry.instance.getServerBroker()

        return runCatching {
            brokerFactory.createFromConfig(config, logger)
                .onSuccess { broker ->
                    registerServer(broker, serverName, config)
                }
                .onFailure { exception ->
                    logger.warn(
                        "ServerManager: Unable to find valid broker for $serverName: ${exception.message}"
                    )
                }
        }
    }

    private fun registerServer(broker: Broker, serverName: String, config: ServerConfig) {
        ServiceRegistry.instance.injector?.createChildInjector(ServerModule(proxy, config, broker, logger))
            ?.let { injector ->
                runCatching {
                    servers[serverName] = injector.getInstance(Server::class.java)
                }.fold(
                    onSuccess = {
                        logger.debug("ServerManager: Created server $serverName")
                    },
                    onFailure = {
                        logger.warn("ServerManager: Failed to create server $serverName: ${it.message}")
                    }
                )
            } ?: run {
            logger.error("ServerManager: Failed to create child injector for server $serverName")
            return
        }
    }

    fun removeServer(serverName: String) {
        servers[serverName]?.removeServer()?.onFailure { error ->
            logger.error("Server $error failed to remove, it may be in an invalid state")
        }

        // Only remove the server record if it is dynamic
        val velocityServer = proxy.getServer(serverName).orElse(null)
        if (velocityServer != null && !proxy.configuration.servers.keys.contains(velocityServer.serverInfo.name)) {
            proxy.unregisterServer(velocityServer.serverInfo)
        }

        servers.remove(serverName)
    }

    private fun reconcileServers(oldConfigs: List<ServerConfig>, newConfigs: List<ServerConfig>) {
        val oldServerNames = servers.keys.toSet()
        val newServerNames = newConfigs.map { it.name }

        val toRemove = oldServerNames - newServerNames.toSet()
        toRemove.forEach(::removeServer)

        val toAdd = newServerNames - oldServerNames
        toAdd.forEach { serverName ->
            newConfigs.find { it.name == serverName }?.let { config ->
                createServer(serverName, config).onSuccess {
                    logger.info("ServerManager: server $serverName added")
                }.onFailure {
                    logger.error("ServerManager: server $serverName failed to add: ${it.message}")
                }
            }
        }

        val toReconcile = newServerNames intersect oldServerNames
        toReconcile.forEach { serverName ->
            val newConfig = newConfigs.find { it.name == serverName }
            if (oldConfigs.find { it.name == serverName } == newConfig) {
                logger.trace("ServerManager: server $serverName configuration unchanged")
                return@forEach
            }
            servers[serverName]?.reconcile(newConfig!!)?.onSuccess {
                logger.info("ServerManager: server $serverName reconciled")
            }?.onFailure {
                logger.error("ServerManager: server $serverName failed to reconcile: ${it.message}")
            }
        }
    }

    fun handleConfigReloadEvent(event: ConfigReloadEvent) {
        logger.debug("ServerManager: starting server configuration reload")
        if (!event.result.isAllowed) {
            logger.trace("ServerManager: configuration reload denied")
            return
        }
        reconcileServers(event.oldConfig.servers, event.config.servers)
        if (event.config.serverMaintenanceInterval != maintenanceInterval) {
            maintenanceInterval = event.config.serverMaintenanceInterval
            maintenanceTask.cancel()
            maintenanceTask = proxy.scheduler
                .buildTask(plugin, this::serverMaintenance)
                .repeat(maintenanceInterval, TimeUnit.SECONDS)
                .schedule()
        }
        logger.info("ServerManager: server configuration reload complete")
    }

    @Subscribe
    fun onConfigReloadEvent(event: ConfigReloadEvent): EventTask {
        return EventTask.async {
            handleConfigReloadEvent(event)
        }
    }
}
