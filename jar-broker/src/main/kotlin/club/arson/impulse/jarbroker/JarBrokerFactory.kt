package club.arson.impulse.jarbroker

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import club.arson.impulse.api.server.BrokerFactory
import org.slf4j.Logger

/**
 * Factory interface used to dynamically register and create jar brokers
 */
class JarBrokerFactory : BrokerFactory {
    /**
     * This broker is designed to run raw jar files on the server
     */
    override val NAME = "jar"

    /**
     * Create a jar broker from a ServerConfig Object
     *
     * We do a check to make sure that the ServerConfig contains a valid JarBrokerConfig.
     * @param config Server configuration to create a jar broker for
     * @param logger Logger ref for log messages
     * @return A result containing a jar broker if we were able to create one for the server, else an error
     */
    override fun createFromConfig(config: ServerConfig, logger: Logger?): Result<Broker> {
        return (config.config as? JarBrokerConfig)?.let { conf ->
            Result.success(JarBroker(config, logger))
        } ?: Result.failure(IllegalArgumentException("Invalid configuration for jar broker!!!"))
    }
}