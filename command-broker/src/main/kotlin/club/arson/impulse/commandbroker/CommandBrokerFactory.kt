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

package club.arson.impulse.commandbroker

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import club.arson.impulse.api.server.BrokerFactory
import org.slf4j.Logger

/**
 * Factory interface used to dynamically register and create jar brokers
 */
class CommandBrokerFactory : BrokerFactory {
    /**
     * This broker is designed to run raw jar files on the server
     */
    override val provides: List<String> = listOf("jar", "cmd")

    /**
     * Create a jar broker from a ServerConfig Object
     *
     * We do a check to make sure that the ServerConfig contains a valid JarBrokerConfig.
     * @param config Server configuration to create a jar broker for
     * @param logger Logger ref for log messages
     * @return A result containing a jar broker if we were able to create one for the server, else an error
     */
    override fun createFromConfig(config: ServerConfig, logger: Logger?): Result<Broker> {
        return when (config.config) {
            is JarBrokerConfig -> Result.success(JarBroker(config, logger))
            is CommandBrokerConfig -> Result.success(CommandBroker(config, logger))
            else -> Result.failure(IllegalArgumentException("Invalid configuration for command/jar broker"))
        }
    }
}