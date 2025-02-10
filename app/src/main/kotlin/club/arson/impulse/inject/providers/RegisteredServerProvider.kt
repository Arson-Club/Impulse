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

package club.arson.impulse.inject.providers

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import com.google.inject.Provider
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import org.slf4j.Logger
import kotlin.jvm.optionals.getOrNull

/**
 * Provider for registered servers
 *
 * Returns a registered server if one exists in Velocity. creates one if it does not
 * @param proxy reference to the proxy instance
 * @param config the server configuration
 */
class RegisteredServerProvider(
    private val proxy: ProxyServer,
    private val config: ServerConfig,
    private val broker: Broker,
    private val logger: Logger? = null
) :
    Provider<RegisteredServer> {
    /**
     * Gets a registered server if it exists for the provided server name
     *
     * @return the existing server instance or a newly created one
     */
    override fun get(): RegisteredServer {
        val existingServer = proxy.getServer(config.name).getOrNull()
        if (existingServer != null) {
            return existingServer
        }
        val address = broker.address().getOrNull()
        if (address == null) {
            logger?.error("Failed to get address for server ${config.name}")
            throw IllegalStateException("Failed to get address for server ${config.name}")
        }
        return proxy.registerServer(ServerInfo(config.name, address))
    }
}