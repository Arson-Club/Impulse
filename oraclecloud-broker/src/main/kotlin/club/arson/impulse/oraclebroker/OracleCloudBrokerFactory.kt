package club.arson.impulse.oraclebroker

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import club.arson.impulse.api.server.BrokerFactory
import org.slf4j.Logger

@Suppress("unused")
class OracleCloudBrokerFactory : BrokerFactory {
    override val provides = listOf("oracle-cloud")

    override fun createFromConfig(config: ServerConfig, logger: Logger?): Result<Broker> {
        return when (config.type) {
            "oracle-cloud" -> {
                (config.config as? OracleCloudConfig)?.let { conf ->
                    Result.success(OracleCloudBroker(config, logger))
                }
                    ?: Result.failure(IllegalArgumentException("Expected OracleCloud specific config and got something else!"))
            }

            else -> {
                Result.failure(Throwable("Invalid configuration for oracle-cloud broker"))
            }
        }
    }
}