package club.arson.ogsoVelocity

import club.arson.ogsoVelocity.config.ConfigReloadEvent
import club.arson.ogsoVelocity.config.ServerConfig
import club.arson.ogsoVelocity.serverBrokers.ServerBroker
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import org.slf4j.Logger

class ServerManager(proxy: ProxyServer, plugin: OgsoVelocity, val logger: Logger? = null) {
    var servers = mutableMapOf<String, ServerBroker>()

    init {
        proxy.eventManager.register(plugin, this)
        val config = ServiceRegistry.instance.configManager?.servers ?: emptyList()
        _reconcileServers(emptyList(), config)
    }

    fun getServer(name: String): ServerBroker? {
        return servers[name]
    }

    fun awaitReady(server: RegisteredServer): Result<Unit> {
        val startTime = System.currentTimeMillis()
        var isReady = false
        val timeout = ServiceRegistry
            .instance
            .configManager
            ?.servers
            ?.find { it.name == server.serverInfo.name }
            ?.startupTimeout ?: 120000

        // TODO make timeout configurable
        while (!isReady && System.currentTimeMillis() - startTime < timeout) {
            try {
                server.ping().get()
                isReady = true
            } catch(e: Exception) {
                Thread.sleep(200)
            }
        }

        return if (isReady) Result.success(Unit) else Result.failure(Throwable("Server ${server.serverInfo.name} failed to start (timeout)"))
    }

    private fun _reconcileServers(oldConfigs: List<ServerConfig>, newConfigs: List<ServerConfig>) {
        val oldServers = oldConfigs.map { it.name }
        val newServers = newConfigs.map { it.name }

        val toRemove = oldServers - newServers
        toRemove.forEach {
            val server = servers[it]
            server?.stopServer()?.onSuccess {
                server.removeServer().onFailure {
                    logger?.error("Failed to remove server $it")
                }
            }?.onFailure {
                logger?.error("Failed to stop server $it")
            }
        }
        toRemove.forEach { servers.remove(it) }

        val toAdd = newServers - oldServers
        toAdd.forEach { serverName ->
            val serverConfig = newConfigs.find { it.name == serverName }
            if (serverConfig == null) {
                logger?.error("Server $serverName not found in new configuration")
            } else {
                val server = ServerBroker.createFromConfig(serverConfig, logger)
                server.onSuccess {
                    servers[serverName] = it;
                }.onFailure { logger?.error("Failed to create server $serverName: $it") }
            }
        }

        val toUpdate = newServers intersect oldServers
        toUpdate.forEach { serverName ->
            val oldConfig = oldConfigs.find { it.name == serverName }
            val newConfig = newConfigs.find { it.name == serverName }
            if (oldConfig != newConfig && newConfig != null) {
                logger?.info("Updating server $serverName")
                val server = servers[serverName]
                server?.stopServer()?.onSuccess {
                    server.removeServer().onSuccess {
                        val newServer = ServerBroker.createFromConfig(newConfig, logger)
                        newServer.onSuccess {
                            servers[serverName] = it
                        }.onFailure { logger?.error("Failed to create server $serverName: $it") }
                    }.onFailure { logger?.error("Failed to remove server $serverName: $it") }
                }?.onFailure { logger?.error("Failed to stop server $serverName: $it")}
            }
        }
    }

    @Subscribe
    fun onConfigReloadEvent(event: ConfigReloadEvent) {
        logger?.debug("Reloading server configuration")
        if (!event.result.isAllowed) {
            return
        }
        _reconcileServers(event.oldConfig.servers, event.config.servers)
    }

}