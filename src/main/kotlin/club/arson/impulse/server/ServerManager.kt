package club.arson.impulse.server

import club.arson.impulse.Impulse
import club.arson.impulse.ServiceRegistry
import club.arson.impulse.config.ConfigReloadEvent
import club.arson.impulse.config.ServerConfig
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import org.slf4j.Logger
import java.util.concurrent.TimeUnit

class ServerManager(val proxy: ProxyServer, val plugin: Impulse, val logger: Logger? = null) {
    var servers = mutableMapOf<String, Server>()
    var maintenanceInterval: Long
    var maintenanceTask: ScheduledTask

    init {
        proxy.eventManager.register(plugin, this)
        val config = ServiceRegistry.instance.configManager
        _reconcileServers(emptyList(), config?.servers ?: emptyList())
        maintenanceInterval = config?.maintenanceInterval ?: 300
        maintenanceTask = proxy.scheduler
            .buildTask(plugin, this::_serverMaintenance)
            .repeat(maintenanceInterval, TimeUnit.SECONDS)
            .schedule()
    }

    private fun _serverMaintenance() {
        servers.values.forEach { server ->
            if (server.serverRef.playersConnected.isEmpty()) {
                logger?.trace("Found empty server ${server.serverRef.serverInfo.name}")
                server.scheduleStop()
            }
        }
    }

    fun getServer(name: String): Server? {
        return servers[name]
    }

    private fun _reconcileServers(oldConfigs: List<ServerConfig>, newConfigs: List<ServerConfig>) {
        val oldServerNames = servers.keys
        val newServerNames = newConfigs.map { it.name }
        val allServerInstances = proxy.allServers

        val toRemove = oldServerNames - newServerNames
        toRemove.forEach {
            val server = servers[it]
            server?.removeServer()?.onFailure {
                logger?.error("Server $it failed to remove, it may be in an invalid state")
            }
            servers.remove(it)
        }

        val toAdd = newServerNames - oldServerNames
        toAdd.forEach{ serverName ->
            val serverInstance = allServerInstances.find { it.serverInfo.name == serverName }
            val config = newConfigs.find { it.name == serverName }
            if (serverInstance == null || config == null) {
                logger?.warn("Server $serverName unable to be configured, missing instance or configuration")
            } else {
                ServerBroker.createFromConfig(config, logger).onSuccess {
                    servers[serverName] = Server(it, serverInstance, config, proxy, plugin, logger)
                    logger?.debug("ServerManager: server $serverName created")
                }.onFailure {
                    logger?.error("ServerManager: server $serverName failed to create: ${it.message}")
                }
            }
        }

        val toReconcile = newServerNames intersect oldServerNames
        toReconcile.forEach{ serverName ->
            val newConfig = newConfigs.find { it.name == serverName }
            if (oldConfigs.find { it.name == serverName } == newConfig) {
                logger?.trace("ServerManager: server $serverName configuration unchanged")
                return@forEach
            }
            servers[serverName]?.reconcile(newConfig!!)?.onSuccess {
                logger?.info("ServerManager: server $serverName reconciled")
            }?.onFailure {
                logger?.error("ServerManager: server $serverName failed to reconcile: ${it.message}")
            }
        }
    }

    @Subscribe
    fun onConfigReloadEvent(event: ConfigReloadEvent): EventTask {
        return EventTask.async({
            logger?.debug("ServerManager: starting server configuration reload")
            if (!event.result.isAllowed) {
                logger?.trace("ServerManager: configuration reload denied")
                return@async
            }
            _reconcileServers(event.oldConfig.servers, event.config.servers)
            if (event.config.serverMaintenanceInterval != maintenanceInterval) {
                maintenanceInterval = event.config.serverMaintenanceInterval
                maintenanceTask.cancel()
                maintenanceTask = proxy.scheduler
                    .buildTask(plugin, this::_serverMaintenance)
                    .repeat(maintenanceInterval, TimeUnit.SECONDS)
                    .schedule()
            }
            logger?.info("ServerManager: server configuration reload complete")
        })
    }
}
