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
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import org.slf4j.Logger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

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

    private fun serverMaintenance() {
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

    private fun reconcileServers(oldConfigs: List<ServerConfig>, newConfigs: List<ServerConfig>) {
        val oldServerNames = servers.keys
        val newServerNames = newConfigs.map { it.name }
        val allServerInstances = proxy.allServers

        val toRemove = oldServerNames - newServerNames.toSet()
        toRemove.forEach {
            val server = servers[it]
            server?.removeServer()?.onFailure { error ->
                logger.error("Server $error failed to remove, it may be in an invalid state")
            }
            servers.remove(it)
        }

        val toAdd = newServerNames - oldServerNames
        toAdd.forEach { serverName ->
            val serverInstance = allServerInstances.find { it.serverInfo.name == serverName }
            val config = newConfigs.find { it.name == serverName }
            if (serverInstance == null || config == null) {
                logger.warn("Server $serverName unable to be configured, missing instance or configuration")
            } else {
                val brokerFactory = ServiceRegistry.instance.getServerBroker()
                brokerFactory.createFromConfig(config, logger)
                    .onSuccess {
                        servers[serverName] = Server(it, serverInstance, config, proxy, plugin, logger)
                    }
                    .onFailure {
                        logger.error("ServerManager: server $serverName failed to create: ${it.message}")
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

    @Subscribe
    fun onConfigReloadEvent(event: ConfigReloadEvent): EventTask {
        return EventTask.async {
            logger.debug("ServerManager: starting server configuration reload")
            if (!event.result.isAllowed) {
                logger.trace("ServerManager: configuration reload denied")
                return@async
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
    }
}
