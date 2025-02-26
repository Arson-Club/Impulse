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
import club.arson.impulse.api.server.Status
import org.slf4j.Logger
import java.net.InetSocketAddress

class JarBroker(serverConfig: ServerConfig, logger: Logger? = null) : Broker {
    private val commandBroker: CommandBroker

    init {
        val cmdConfig = toCommandBrokerConfig(serverConfig.config as JarBrokerConfig)
        serverConfig.config = cmdConfig
        commandBroker = CommandBroker(serverConfig, logger)
    }


    override fun address(): Result<InetSocketAddress> = commandBroker.address()

    override fun isRunning(): Boolean = commandBroker.isRunning()

    override fun getStatus(): Status = commandBroker.getStatus()

    override fun startServer(): Result<Unit> = commandBroker.startServer()

    override fun stopServer(): Result<Unit> = commandBroker.stopServer()

    override fun removeServer(): Result<Unit> = commandBroker.removeServer()

    override fun reconcile(config: ServerConfig): Result<Runnable?> {
        if (config.type != "jar") {
            return Result.failure(IllegalArgumentException("Expected JarBrokerConfig and got something else!"))
        }

        val cmdConfig = toCommandBrokerConfig(config.config as JarBrokerConfig)
        config.config = cmdConfig
        config.type = "cmd"

        return commandBroker.reconcile(config)
    }
}