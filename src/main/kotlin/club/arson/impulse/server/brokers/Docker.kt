package club.arson.impulse.server.brokers

import club.arson.impulse.config.DockerServerConfig
import club.arson.impulse.config.ServerConfig
import club.arson.impulse.server.ServerBroker
import club.arson.impulse.server.ServerStatus
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PullImageResultCallback
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

class Docker(config: ServerConfig, val logger: Logger? = null) : ServerBroker {
    var startupTimeout: Long
    var stopTimeout: Long
    private val name: String = config.name
    lateinit var client: DockerClient
    lateinit var dockerConfig: DockerServerConfig
    lateinit var dockerHost: String

    init {
        startupTimeout = config.startupTimeout
        stopTimeout = config.stopTimeout
        _configureDockerClient(config)
    }

    private fun _configureDockerClient(config: ServerConfig) {
        if (config.docker == null) {
            logger?.error("Configuration missing for docker broker ${config.name}")
            throw Exception("Unable to create docker broker")
        }
        dockerConfig = config.docker!!
        dockerHost = dockerConfig.hostPath
        val clientConfig = DefaultDockerClientConfig
            .createDefaultConfigBuilder()
            .withDockerHost(dockerHost)
            .build()

        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(clientConfig.dockerHost)
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build()

        client = DockerClientImpl.getInstance(clientConfig, httpClient)
        startupTimeout = config.startupTimeout
        stopTimeout = config.stopTimeout
    }

    private fun _createContainer(): Result<Unit> {
        // Pull the image if it is missing
        kotlin.runCatching { client.inspectImageCmd(dockerConfig.image).exec() }.onSuccess {
            logger?.debug("Docker Broker: image ${dockerConfig.image} exists")
        }.onFailure {
            logger?.info("Docker Broker: image ${dockerConfig.image} does not exist, pulling...")
            kotlin.runCatching { client.pullImageCmd(dockerConfig.image).exec(PullImageResultCallback()).awaitCompletion() }.onSuccess {
                logger?.info("Docker Broker: image ${dockerConfig.image} pulled successfully")
            }.onFailure {
                logger?.info("Docker Broker: image ${dockerConfig.image} failed to pull ${it.message}")
                return Result.failure(it)
            }
        }

        // Create the container
        val binds = ArrayList<Bind>()
        for ((hostPath, mount) in dockerConfig.volumes) {
            binds.add(Bind(hostPath, Volume(mount)))
        }
        val hostConfig = HostConfig
            .newHostConfig()
            .withRestartPolicy(RestartPolicy.unlessStoppedRestart())
            .withPortBindings(
                dockerConfig.portBindings.map { PortBinding.parse(it) }
            )
            .withBinds(binds)
        try {
            client
                .createContainerCmd(dockerConfig.image)
                .withName(name)
                .withHostConfig(hostConfig)
                .withTty(true)
                .withStdinOpen(true)
                .withEnv(dockerConfig.env.map { "${it.key}=${it.value}" })
                .exec()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return _startContainer()
    }

    private fun _startContainer(): Result<Unit> {
        try {
            client
                .startContainerCmd(name)
                .exec()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return _awaitContainerStart()
    }

    private fun _stopContainer(): Result<Unit> {
        try {
            client.stopContainerCmd(name).exec()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return _awaitContainerStop()
    }

    private fun _awaitContainerStart(): Result<Unit> {
        val startTime = System.currentTimeMillis()
        var isRunning = false
        while (!isRunning && System.currentTimeMillis() - startTime * 1000 < startupTimeout) {
            val status = _getContainerStatus()
            if (status == "running") {
                isRunning = true
            } else if (status == "exited") {
                return Result.failure(Throwable("Server $name exited unexpectedly"))
            }
            Thread.sleep(500)
        }
        return if (isRunning) Result.success(Unit) else Result.failure(Throwable("Server $name failed to start (timeout)"))
    }

    private fun _awaitContainerStop(): Result<Unit> {
        val startTime = System.currentTimeMillis()
        var isStopped = false
        while (!isStopped && System.currentTimeMillis() - startTime * 1000 < stopTimeout) {
            val status = _getContainerStatus()
            if (status == "exited" || status == "dead" || status == null) {
                isStopped = true
            }
            Thread.sleep(500)
        }
        return if (isStopped) Result.success(Unit) else Result.failure(Throwable("Server $name failed to stop (timeout)"))
    }

    private fun _getContainerStatus(): String? {
        return try {
            client.inspectContainerCmd(name).exec().state.status
        } catch (e: NotFoundException) {
            null
        }
    }

    override fun getStatus(): ServerStatus {
        return when (_getContainerStatus()) {
            "running" -> ServerStatus.RUNNING
            "restarting", "created", "paused", "exited", "dead", "removing" -> ServerStatus.STOPPED
            null -> ServerStatus.REMOVED
            else -> ServerStatus.UNKNOWN
        }
    }

    override fun isRunning(): Boolean {
        return getStatus() == ServerStatus.RUNNING
    }

    override fun startServer(): Result<Unit> {
        return when (_getContainerStatus()) {
            "running" -> Result.success(Unit)
            "restarting" -> _awaitContainerStart()
            "created", "paused", "exited" -> _startContainer()
            "removing" -> _awaitContainerStop().let { if (it.isSuccess) _startContainer() else it }
            "dead" -> removeServer().let { if (it.isSuccess) _createContainer() else it }
            null, -> _createContainer()
            else -> Result.failure(Throwable("Server $name is in unknown state, aborting!"))
        }
    }

    override fun stopServer(): Result<Unit> {
        return when (_getContainerStatus()) {
            "running", "restarting" -> _stopContainer()
            "created", "paused", "exited" -> Result.success(Unit)
            "removing" -> _awaitContainerStop()
            "dead" -> removeServer()
            null -> Result.success(Unit)
            else -> Result.failure(Throwable("Server $name is in unknown state, aborting!"))
        }
    }

    override fun removeServer(): Result<Unit> {
        return _stopContainer().onSuccess {
            runCatching { client.removeContainerCmd(name).exec() }
                .onSuccess { return Result.success(Unit) }
                .onFailure { return Result.failure(it) }
        }.onFailure { return Result.failure(it) }
    }

    override fun reconcile(config: ServerConfig): Result<Runnable?> {
        if (config.type != "docker") {
            return Result.failure(IllegalArgumentException("Invalid configuration type: ${config.type}"))
        }

        var response: Result<Runnable?> = Result.success(null)
        runCatching { client.inspectContainerCmd(name).exec() }
            .onSuccess {
                val hostChanged = config.docker?.hostPath != dockerHost
                val imageChanged = it.config.image != config.docker?.image

                val currentEnv = config.docker?.env?.map { "${it.key}=${it.value}" }?.toSet() ?: emptySet()
                val liveEnv = it.config.env?.toSet() ?: emptySet()
                val envChanged = !liveEnv.containsAll(currentEnv)

                val desiredPortConfig = config.docker?.portBindings?.map {
                    val binding = PortBinding.parse(it)
                    Pair(binding.binding.hostPortSpec, binding.exposedPort.port.toString())
                }
                val livePortConfig = it.hostConfig.portBindings.bindings.map {
                    val p = Pair(it.key.port.toString(), it.value.map { binding -> binding.hostPortSpec })
                    p.second.map { value -> value to p.first }
                }.flatten()

                val portsChanged = !livePortConfig.containsAll(desiredPortConfig ?: emptyList())
                val volumesChanged = !it.hostConfig.binds.map { Pair(it.path, it.volume.path) }
                    .containsAll(config.docker?.volumes?.map { Pair(it.key, it.value) } ?: emptyList())
                if (imageChanged || portsChanged || volumesChanged || hostChanged || envChanged) {
                    response = Result.success(Runnable {
                        removeServer()
                        _configureDockerClient(config)
                        stopTimeout = config.stopTimeout
                        startupTimeout = config.startupTimeout
                        _createContainer()
                        if (it.state.status == "running" ) {
                            _startContainer()
                        }
                    })
                }
            }
            .onFailure {
                response = Result.failure(it)
            }
        return response
    }
}