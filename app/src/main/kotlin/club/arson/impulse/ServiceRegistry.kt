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

package club.arson.impulse

import club.arson.impulse.config.ConfigManager
import club.arson.impulse.server.ServerBroker
import club.arson.impulse.server.ServerManager
import com.google.inject.Injector

/**
 * Singleton registry for managing "global" resources
 *
 * This should be initialized by the Proxy on startup and as early as possible.
 */
class ServiceRegistry {
    /**
     * Holds the singleton instance for the parent class
     */
    companion object {
        /**
         * Singleton instance of the [ServiceRegistry]
         */
        val instance: ServiceRegistry by lazy { ServiceRegistry() }
    }

    /**
     * The [ServerManager] instance used to interact with managed servers.
     *
     * @throws IllegalStateException if the [ServerManager] is already registered
     */
    var serverManager: ServerManager? = null
        set(value) {
            if (serverManager != null) {
                throw IllegalStateException("ServerManager already registered")
            }
            field = value
        }

    /**
     * The [ConfigManager] instance used to interact with the configuration.
     *
     * @throws IllegalStateException if the [ConfigManager] is already registered
     */
    var configManager: ConfigManager? = null
        set(value) {
            if (configManager != null) {
                throw IllegalStateException("ConfigManager already registered")
            }
            field = value
        }

    /**
     * An [com.google.inject.Injector] instance used for retrieving available Brokers
     *
     * We use the Guice framework to dynamically inject Brokers. This allows you to access the set of currently
     * registered Brokers and Broker Configs.
     */
    var brokerInjector: Injector? = null
        set(value) {
            if (brokerInjector != null) {
                throw IllegalStateException("BrokerInjector already registered")
            }
            field = value
        }

    /**
     * Creates a new [ServerBroker] instance from the registered [Injector]
     * @return a new [ServerBroker] instance
     */
    fun getServerBroker(): ServerBroker {
        return brokerInjector?.getInstance(ServerBroker::class.java)
            ?: throw IllegalStateException("BrokerInjector not registered")
    }
}