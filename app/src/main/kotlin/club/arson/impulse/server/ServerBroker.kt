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

package club.arson.impulse.server

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import club.arson.impulse.api.server.BrokerFactory
import com.google.inject.Inject
import org.slf4j.Logger
import kotlin.reflect.KClass

/**
 * A class for managing server broker factories and configuration classes. These should be injected from the [club.arson.impulse.inject.modules.BrokerModule].
 * Guice module.
 *
 * @property brokers a set of broker factories
 * @property configClasses a map of broker types to configuration classes
 * @constructor creates a new server broker instance
 */
class ServerBroker @Inject constructor(
    private val brokers: Set<BrokerFactory>,
    val configClasses: Map<String, KClass<out Any>>
) {
    /**
     * Creates a broker from a given configuration.
     *
     * Attempts to use the config "type" filed to determine the correct broker factory to forward the request to.
     * @param config the server configuration
     * @param logger the logger to use for the broker
     * @return a result containing the broker or an error
     */
    fun createFromConfig(config: ServerConfig, logger: Logger? = null): Result<Broker> {
        val brokerFactory = brokers.find { it.provides.contains(config.type) }
        if (brokerFactory == null) {
            return Result.failure(IllegalArgumentException("Invalid broker type: ${config.type}"))
        }

        return brokerFactory.createFromConfig(config, logger)
    }
}