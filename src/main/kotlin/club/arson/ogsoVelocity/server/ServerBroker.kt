package club.arson.ogsoVelocity.serverBrokers

import club.arson.ogsoVelocity.config.ServerConfig
import org.slf4j.Logger

interface ServerBroker {
    companion object {
        fun createFromConfig(config: ServerConfig, logger: Logger? = null) =
            when (config.type){
                "docker" -> Result.success(Docker(config, logger))
                else -> Result.failure(IllegalArgumentException("Invalid broker type: ${config.type}"))
            }
    }

    fun startServer(): Result<Unit>

    fun stopServer(): Result<Unit>

    fun removeServer(): Result<Unit>
}