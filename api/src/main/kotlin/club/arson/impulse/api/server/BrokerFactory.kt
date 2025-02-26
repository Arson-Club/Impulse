/*
 * Impulse Server Manager for Velocity
 * Copyright (c) 2025  Dabb1e
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package club.arson.impulse.api.server

import club.arson.impulse.api.config.ServerConfig
import org.slf4j.Logger

/**
 * API used to implemnt a broker factory. This is used to create brokers from a configuration.
 */
interface BrokerFactory {
    /**
     * A list of the broker types this factory can create
     */
    val provides: List<String>

    /**
     * Create a broker from a configuration
     *
     * @param config The [ServerConfig] used to create the broker from
     * @param logger An optional logger to use for messages in the broker
     * @return A [Result] containing the created broker or an error
     */
    fun createFromConfig(config: ServerConfig, logger: Logger? = null): Result<Broker>
}