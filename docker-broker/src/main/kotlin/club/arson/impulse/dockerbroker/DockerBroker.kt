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

package club.arson.impulse.dockerbroker

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import club.arson.impulse.api.server.Status
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.InspectImageResponse
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.exception.NotFoundException
import com.github.dockerjava.api.exception.NotModifiedException
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import kotlinx.coroutines.*
import org.slf4j.Logger
import java.lang.Runnable
import java.net.InetSocketAddress
import java.time.Duration

/**
 * Implements a Docker based server broker for creating, updating, and removing servers.
 *
 * Connects to the Docker socket directly
 * @param serverConfig Server configuration that should contain a valid [DockerServerConfig]
 * @property logger Logger ref to write log message to
 * @constructor Creates a DockerBroker initialized with the provided configuration
 */
class DockerBroker(serverConfig: ServerConfig, private val logger: Logger? = null) : Broker {
    private var startupTimeout: Long
    private var stopTimeout: Long
    private val name: String = serverConfig.name
    private lateinit var client: DockerClient
    private lateinit var dockerConfig: DockerServerConfig
    private lateinit var dockerHost: String
    private var pullImageTask: Deferred<Result<Unit>>
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        startupTimeout = serverConfig.lifecycleSettings.timeouts.startup
        stopTimeout = serverConfig.lifecycleSettings.timeouts.shutdown
        configureDockerClient(serverConfig)
        pullImageTask = tryPullImageIfMissing(dockerConfig.image, scope)
    }

    private fun configureDockerClient(config: ServerConfig) {
        val dockerConfig = config.config as? DockerServerConfig
        if (dockerConfig == null) {
            logger?.error("Configuration missing for docker broker ${config.name}")
            throw Exception("Unable to create docker broker")
        }
        this.dockerConfig = dockerConfig
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
        startupTimeout = config.lifecycleSettings.timeouts.startup
        stopTimeout = config.lifecycleSettings.timeouts.shutdown
    }

    private fun tryPullImageIfMissing(image: String, scope: CoroutineScope): Deferred<Result<Unit>> {
        // Default to latest if no tag is specified
        val taggedImage = if (image.contains(":")) image else "$image:latest"

        return scope.async {
            when (dockerConfig.imagePullPolicy) {
                ImagePullPolicy.ALWAYS -> {
                    logger?.info("Docker Broker: Pulling image $taggedImage")
                    pullImage(taggedImage)
                }

                ImagePullPolicy.IF_NOT_PRESENT -> pullImageIfNotPresent(taggedImage)

                ImagePullPolicy.NEVER -> {
                    logger?.info("Docker Broker: Skipping image pull for $taggedImage")
                    Result.success(Unit)
                }
            }
        }
    }

    private fun pullImageIfNotPresent(taggedImage: String): Result<Unit> {
        val inspectionResult = inspectImage(taggedImage)
        return if (inspectionResult.isSuccess) {
            logger?.info("Docker Broker: image $taggedImage exists")
            Result.success(Unit)
        } else {
            logger?.info("Docker Broker: image $taggedImage does not exist, pulling (This may take a while)...")
            pullImage(taggedImage)
        }
    }

    class ProgressCallback(private val logger: Logger? = null) : PullImageResultCallback() {
        override fun onNext(item: PullResponseItem?) {
            super.onNext(item)
            logger?.debug("Docker Broker: Pulling image: ${item?.status} ${item?.progressDetail?.current}/${item?.progressDetail?.total}")
        }
    }

    private fun pullImage(image: String): Result<Unit> {
        return runCatching {
            client.pullImageCmd(image).exec(ProgressCallback(logger)).awaitCompletion()
        }.fold(
            onSuccess = {
                logger?.info("Docker Broker: image $image pulled successfully")
                Result.success(Unit)
            },
            onFailure = { exception ->
                logger?.info("Docker Broker: image $image failed to pull: ${exception.message}")
                Result.failure(exception)
            }
        )
    }

    private fun inspectImage(image: String): Result<InspectImageResponse> {
        return runCatching { client.inspectImageCmd(image).exec() }
    }

    private fun createContainer(autoStart: Boolean = dockerConfig.autoStartOnCreate): Result<Unit> {
        // Pull the image if it is missing
        runBlocking { pullImageTask.await() }
            .onFailure { return Result.failure(it) }

        // Create the container
        val binds = ArrayList<Bind>()
        dockerConfig.volumes.forEach {
            val (hostPath, mount) = it.split(":")
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
        return if (autoStart) startContainer() else Result.success(Unit)
    }

    private fun startContainer(): Result<Unit> {
        try {
            client
                .startContainerCmd(name)
                .exec()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return awaitContainerStart()
    }

    private fun stopContainer(): Result<Unit> {
        return runCatching {
            client.stopContainerCmd(name).exec()
        }.fold(
            onSuccess = {
                awaitContainerStop()
            },
            onFailure = { exception ->
                when (exception) {
                    is NotModifiedException -> Result.success(Unit) // Already stopped
                    is NotFoundException -> Result.success(Unit) // Container does not exist
                    else -> Result.failure(exception)
                }
            }
        )
    }

    private fun awaitContainerStart(): Result<Unit> {
        val startTime = System.currentTimeMillis()
        var isRunning = false
        while (!isRunning && System.currentTimeMillis() - startTime * 1000 < startupTimeout) {
            val status = getContainerStatus()
            if (status == "running") {
                isRunning = true
            } else if (status == "exited") {
                return Result.failure(Throwable("Server $name exited unexpectedly"))
            }
            Thread.sleep(500)
        }
        return if (isRunning) Result.success(Unit) else Result.failure(Throwable("Server $name failed to start (timeout)"))
    }

    private fun awaitContainerStop(): Result<Unit> {
        val startTime = System.currentTimeMillis()
        var isStopped = false
        while (!isStopped && System.currentTimeMillis() - startTime * 1000 < stopTimeout) {
            val status = getContainerStatus()
            if (status == "exited" || status == "dead" || status == null) {
                isStopped = true
            }
            Thread.sleep(500)
        }
        return if (isStopped) Result.success(Unit) else Result.failure(Throwable("Server $name failed to stop (timeout)"))
    }

    private fun getContainerStatus(): String? {
        return try {
            client.inspectContainerCmd(name).exec().state.status
        } catch (e: NotFoundException) {
            null
        }
    }

    /**
     * Gets a [Status] for the server from the Docker container status
     *
     * @return [Status] of the server
     */
    override fun getStatus(): Status {
        return when (getContainerStatus()) {
            "running" -> Status.RUNNING
            "restarting", "created", "paused", "exited", "dead", "removing" -> Status.STOPPED
            null -> Status.REMOVED
            else -> Status.UNKNOWN
        }
    }

    /**
     * Returns true if the server is running
     *
     * @return true if the server is running
     */
    override fun isRunning(): Boolean {
        return getStatus() == Status.RUNNING
    }

    /**
     * Attempts to start the server if it is not running
     *
     * If the server does not exist it will be created then started. If it does exist, but is not running it will be
     * started. If the server is already running, nothing will happen.
     * @return [Result] of success if the server started, else an error
     */
    override fun startServer(): Result<Unit> {
        return when (getContainerStatus()) {
            "running" -> Result.success(Unit)
            "restarting" -> awaitContainerStart()
            "created", "paused", "exited" -> startContainer()
            "removing" -> awaitContainerStop().let { if (it.isSuccess) startContainer() else it }
            "dead" -> removeServer().let { if (it.isSuccess) createContainer(true) else it }
            null -> createContainer(true)
            else -> Result.failure(Throwable("Server $name is in unknown state, aborting!"))
        }
    }

    /**
     * Attempts to stop the server if it is running
     *
     * If the server is running it will be stopped. If the server is not running, or does not exist, nothing will happen.
     * @return [Result] of success if the server stopped, else an error
     */
    override fun stopServer(): Result<Unit> {
        return when (getContainerStatus()) {
            "running", "restarting" -> stopContainer()
            "created", "paused", "exited" -> Result.success(Unit)
            "removing" -> awaitContainerStop()
            "dead" -> removeServer()
            null -> Result.success(Unit)
            else -> Result.failure(Throwable("Server $name is in unknown state, aborting!"))
        }
    }

    /**
     * Attempts to remove the server
     *
     * If the server is running it will be stopped before being removed. This broker will not attempt to clean up any
     * volumes that were mounted into the container.
     * @return [Result] of success if the server was removed, else an error
     */
    override fun removeServer(): Result<Unit> {
        return stopContainer().onSuccess {
            runCatching { client.removeContainerCmd(name).exec() }
                .onSuccess { return Result.success(Unit) }
                .onFailure { exception ->
                    if (exception is NotFoundException) {
                        return Result.success(Unit)
                    }
                    return Result.failure(exception)
                }
        }.onFailure { return Result.failure(it) }
    }

    /**
     * Gets the address of the server
     *
     * @return [Result] containing the server address or an error
     */
    override fun address(): Result<InetSocketAddress> {
        return dockerConfig.address?.let { address ->
            val (host, portStr) = address.split(":", limit = 2)
            try {
                Result.success(InetSocketAddress.createUnresolved(host, portStr.toInt()))
            } catch (e: NumberFormatException) {
                Result.failure(IllegalArgumentException("Invalid port number: $portStr", e))
            }
        } ?: Result.failure(IllegalArgumentException("Server address is not set"))
    }

    /**
     * Creates a closure that can be run to reconcile the server's state with the new configuration.
     *
     * When a server's configuration is updated this method is called generate a closure that can reconcile the
     * current state with the new configuration. This can either be called immediately, or scheduled as a future task.
     * For the docker broker executing the closure normally results in the server being stopped, removed, and recreated
     * with the new configuration.
     * @param config New server configuration to reconcile
     * @return [Result] of a [Runnable] that will perform the reconciliation, or an error.
     */
    override fun reconcile(config: ServerConfig): Result<Runnable?> {
        if (config.type != "docker") {
            return Result.failure(IllegalArgumentException("Invalid configuration type: ${config.type}"))
        }

        var response: Result<Runnable?> = Result.success(null)
        runCatching { client.inspectContainerCmd(name).exec() }
            .onSuccess { inspection ->
                val dockerConfig = config.config as? DockerServerConfig
                val hostChanged = dockerConfig?.hostPath != dockerHost
                val imageChanged = inspection.config.image != dockerConfig?.image
                val pullPolicyChanged = this.dockerConfig.imagePullPolicy != dockerConfig?.imagePullPolicy

                val currentEnv = dockerConfig?.env?.map { "${it.key}=${it.value}" }?.toSet() ?: emptySet()
                val liveEnv = inspection.config.env?.toSet() ?: emptySet()
                val envChanged = !liveEnv.containsAll(currentEnv)

                val desiredPortConfig = dockerConfig?.portBindings?.map {
                    val binding = PortBinding.parse(it)
                    Pair(binding.binding.hostPortSpec, binding.exposedPort.port.toString())
                }
                val livePortConfig = inspection.hostConfig.portBindings.bindings.map {
                    val p = Pair(it.key.port.toString(), it.value.map { binding -> binding.hostPortSpec })
                    p.second.map { value -> value to p.first }
                }.flatten()

                val portsChanged = !livePortConfig.containsAll(desiredPortConfig ?: emptyList())
                val volumesChanged = !inspection.hostConfig.binds.map { Pair(it.path, it.volume.path) }
                    .containsAll(dockerConfig?.volumes?.map {
                        val (hostPath, mount) = it.split(":")
                        Pair(hostPath, mount)
                    } ?: emptyList())
                if (imageChanged && dockerConfig?.image != null) {
                    pullImageTask = tryPullImageIfMissing(dockerConfig.image, scope)
                }
                if (imageChanged || portsChanged || volumesChanged || hostChanged || envChanged) {
                    response = Result.success(Runnable {
                        removeServer()
                        configureDockerClient(config)
                        stopTimeout = config.lifecycleSettings.timeouts.shutdown
                        startupTimeout = config.lifecycleSettings.timeouts.startup
                        if (pullPolicyChanged) {
                            pullImageTask = tryPullImageIfMissing(dockerConfig!!.image, scope)
                        }
                        createContainer(inspection.state.status == "running")
                    })
                } else {
                    response = Result.success(Runnable {
                        this.dockerConfig = config.config as DockerServerConfig
                        if (pullPolicyChanged) {
                            pullImageTask = tryPullImageIfMissing(dockerConfig!!.image, scope)
                        }
                    })
                }
            }
            .onFailure {
                response = Result.success(null)
            }
        return response
    }
}