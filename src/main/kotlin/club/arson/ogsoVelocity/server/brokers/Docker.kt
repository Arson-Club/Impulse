package club.arson.ogsoVelocity.serverBrokers

import club.arson.ogsoVelocity.config.DockerServerConfig
import club.arson.ogsoVelocity.config.ServerConfig
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.exception.NotFoundException
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.RestartPolicy
import com.github.dockerjava.api.model.Volume
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import org.slf4j.Logger
import java.time.Duration

class Docker(config: ServerConfig, logger: Logger? = null) : ServerBroker {
    val client: DockerClient
    val dockerConfig: DockerServerConfig
    val name: String
    val startupTimeout: Long
    val stopTimeout: Long

    init {
        if (config.docker == null) {
            logger?.error("Configuration missing for docker broker ${config.name}")
            throw Exception("Unable to create docker broker")
        }
        dockerConfig = config.docker!!
        val clientConfig = DefaultDockerClientConfig
            .createDefaultConfigBuilder()
            .withDockerHost(dockerConfig.hostPath)
            .build()

        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(clientConfig.dockerHost)
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build()

        client = DockerClientImpl.getInstance(clientConfig, httpClient)
        name = config.name
        startupTimeout = config.startupTimeout
        stopTimeout = config.stopTimeout
    }

    private fun _createServer(): Result<Unit> {
        val binds = ArrayList<Bind>()
        for ((hostPath, mount) in dockerConfig.volumes) {
            binds.add(Bind(hostPath, Volume(mount)))
        }
        val hostConfig = HostConfig
            .newHostConfig()
            .withRestartPolicy(RestartPolicy.unlessStoppedRestart())
            .withPortBindings(
                PortBinding.parse("${dockerConfig.port}:25565/tcp"),
                PortBinding.parse("${dockerConfig.port}:25565/udp")
            )
            .withBinds(binds)
        try {
            client
                .createContainerCmd(dockerConfig.image)
                .withName(name)
                .withHostConfig(hostConfig)
                .withTty(true)
                .withStdinOpen(true)
                .exec()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return _startServer()
    }

    private fun _startServer(): Result<Unit> {
        try {
            client
                .startContainerCmd(name)
                .exec()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return _awaitStart()
    }

    private fun _stopServer(): Result<Unit> {
        try {
            client.stopContainerCmd(name).exec()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return _awaitStop()
    }

    private fun _awaitStart(): Result<Unit> {
        var startTime = System.currentTimeMillis()
        var isRunning = false
        while (!isRunning && System.currentTimeMillis() - startTime * 1000 < startupTimeout) {
            val status = _getServerStatus()
            if (status == "running") {
                isRunning = true
            } else if (status == "exited") {
                return Result.failure(Throwable("Server $name exited unexpectedly"))
            }
            Thread.sleep(500)
        }
        return if (isRunning) Result.success(Unit) else Result.failure(Throwable("Server $name failed to start (timeout)"))
    }

    private fun _awaitStop(): Result<Unit> {
        val startTime = System.currentTimeMillis()
        var isStopped = false
        while (!isStopped && System.currentTimeMillis() - startTime * 1000 < stopTimeout) {
            val status = _getServerStatus()
            if (status == "exited" || status == "dead" || status == null) {
                isStopped = true
            }
            Thread.sleep(500)
        }
        return if (isStopped) Result.success(Unit) else Result.failure(Throwable("Server $name failed to stop (timeout)"))
    }

    private fun _getServerStatus(): String? {
        return try {
            client.inspectContainerCmd(name).exec().state.status
        } catch (e: NotFoundException) {
            null
        }
    }

    override fun startServer(): Result<Unit> {
        return when (_getServerStatus()) {
            "running" -> Result.success(Unit)
            "restarting" -> _awaitStart()
            "created", "paused", "exited" -> _startServer()
            "removing" -> _awaitStop().let { if (it.isSuccess) _startServer() else it }
            "dead" -> removeServer().let { if (it.isSuccess) _createServer() else it }
            null, -> _createServer()
            else -> Result.failure(Throwable("Server $name is in unknown state, aborting!"))
        }
    }

    override fun stopServer(): Result<Unit> {
        return when (_getServerStatus()) {
            "running", "restarting" -> _stopServer()
            "created", "paused", "exited" -> Result.success(Unit)
            "removing" -> _awaitStop()
            "dead" -> removeServer()
            null -> Result.success(Unit)
            else -> Result.failure(Throwable("Server $name is in unknown state, aborting!"))
        }
    }

    override fun removeServer(): Result<Unit> {
        val stopped = _awaitStop()
        if (stopped.isFailure) {
            return stopped
        }
        try {
            client.removeContainerCmd(name).exec()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
}