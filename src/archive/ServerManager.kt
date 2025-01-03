package club.arson.ogsoVelocity.server

import club.arson.ogsoVelocity.OgsoVelocity
import club.arson.ogsoVelocity.ServiceRegistry
import club.arson.ogsoVelocity.config.ConfigReloadEvent
import club.arson.ogsoVelocity.server.broker.ServerBroker
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent
import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import org.slf4j.Logger
import java.util.concurrent.TimeUnit

class ServerManager(val proxyServer: ProxyServer, val plugin: OgsoVelocity, val broker: ServerBroker, val logger: Logger) {
    private var _reconcileTask: ScheduledTask
    private var _reconcileInterval: Long
    private var _servers: Map<String, Server> = mapOf()

    init {
        _reconcileInterval = ServiceRegistry.instance.configManager?.reconcileInterval ?: 300
        _reconcileTask = _scheduleTask(this::reconcile, _reconcileInterval)
        proxyServer.eventManager.register(plugin, this)
        reconcile()
    }

    @Subscribe
    fun onConfigReload(event: ConfigReloadEvent) {
        if (event.genericResult.isAllowed) {
            if (event.config.reconcileInterval != _reconcileInterval) {
                _reconcileTask.cancel()
                _reconcileInterval = event.config.reconcileInterval
                _reconcileTask = _scheduleTask(this::reconcile, _reconcileInterval)
                logger.trace("ServerManager: updated reconcile interval to $_reconcileInterval")
            }
            reconcile()
            logger.debug("ServerManager: configuration reloaded")
        }
    }

    fun isRunnning(serverName: String): Boolean {
        return _servers[serverName]?.status == ServerStatus.RUNNING
    }

    fun tryStartServer(serverName: String): Result<Unit> {
        val server = _servers[serverName]
        if (server == null) {
            return Result.failure(Exception("Server $serverName is not managed by ogso-velocity"))
        }
        val status = server.status
        var result = Result.success(Unit)
        if (status == ServerStatus.RUNNING) {
            // Verify that we are actually running
            val pingResult = kotlin.runCatching {
                server.server.ping().get()
            }
            pingResult.onFailure {
                logger.warn("server $serverName is marked as running but failed to ping! Trying to start")
                server.status = ServerStatus.UNKNOWN
                result = broker.startServer(server)
            }.onSuccess {
                logger.debug("server $serverName is running, no need to start")
                result = Result.success(Unit)
            }
        } else {
            result = broker.startServer(server)
        }
        return result
    }

    @Subscribe
    fun onServerRegisteredEvent(event: ServerRegisteredEvent) {
        reconcile()
    }

    @Subscribe
    fun onServerUnregisteredEvent(event: ServerUnregisteredEvent) {
        reconcile()
    }


    private fun _scheduleTask(runnable: Runnable, period: Long): ScheduledTask {
        return proxyServer.scheduler
            .buildTask(plugin, runnable)
            .repeat(period, TimeUnit.SECONDS)
            .schedule()
    }

    /*
    what I want to do on a player connect is:
    - check if the server they are connecting to is running
    - if it is not running, start it
    - if it is running, do nothing

    It would be good to maintain this state without having to talke to k8s constantly. Perhaps only reach out if the
    internal state is marked as unhealthy or down? I wonder if there is an on ping event.

    There is not so I am going to have a function that can resolve the server status by checking.
    - if marked as active, try and connect.
      - if it fails, mark as inactive by having a exception handler.
    - if marked as inactive, try and ping. If it fails, start the server (return non started)
     */

    fun reconcile() {
        // TODO: redo most of this
        val allServers = proxyServer.allServers
        val managedServers = ServiceRegistry.instance.configManager?.servers ?: emptyList()

        val servers = allServers.map { server ->
            val config = managedServers.find{ it.name == server.serverInfo.name }
            if (config != null) {
                //TODO: fetch the server status from kubernetes
                Server(server, ServerStatus.RUNNING, config)
            } else {
                null
            }
        }.filterNotNull().associateBy { it.config.name }
        _servers = servers
        _servers.forEach { (name, server) ->
            logger.trace("ServerManager: reconciled server $name")
            logger.trace("ServerManager: status: ${server.status}")
        }
        logger.debug("ServerManager: reconciled all servers")
    }
}