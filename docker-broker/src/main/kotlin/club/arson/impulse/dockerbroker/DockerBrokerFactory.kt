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
import club.arson.impulse.api.server.BrokerFactory
import org.slf4j.Logger

/**
 * Factory interface used to dynamically register and create docker brokers
 */
class DockerBrokerFactory : BrokerFactory {
    /**
     * The name for our broker and associated config class
     */
    override val NAME = "docker"

    /**
     * Create a docker broker from a ServerConfig Object
     *
     * We check to make sure that the ServerConfig contains a DockerServerConfig before
     * creation.
     * @param config Server configuration to create a docker broker for
     * @param logger Logger ref for log messages
     * @return A result containing a docker broker if we were able to create one for the server, else an error
     */
    override fun createFromConfig(config: ServerConfig, logger: Logger?): Result<Broker> {
        return (config.config as? DockerServerConfig)?.let { conf ->
            Result.success(DockerBroker(config, logger))
        } ?: Result.failure(IllegalArgumentException("Expected Docker specific config and got something else!"))
    }
}