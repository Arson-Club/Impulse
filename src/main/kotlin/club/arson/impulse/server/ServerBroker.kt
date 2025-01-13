package club.arson.impulse.server

import club.arson.impulse.config.ServerConfig
import club.arson.impulse.server.brokers.Docker
import org.slf4j.Logger

interface ServerBroker {
    companion object {
        fun createFromConfig(config: ServerConfig, logger: Logger? = null) =
            when (config.type){
                "docker" -> Result.success(Docker(config, logger))
                else -> Result.failure(IllegalArgumentException("Invalid broker type: ${config.type}"))
            }
    }

    fun getStatus(): ServerStatus

    fun isRunning(): Boolean

    fun startServer(): Result<Unit>

    fun stopServer(): Result<Unit>

    fun removeServer(): Result<Unit>

    fun reconcile(config: ServerConfig): Result<Runnable?>
}