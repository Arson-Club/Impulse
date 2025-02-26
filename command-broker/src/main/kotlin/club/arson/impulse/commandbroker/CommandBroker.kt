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
import java.io.File
import java.net.InetSocketAddress

class CommandBroker(serverConfig: ServerConfig, private val logger: Logger? = null) : Broker {
    private var commandConfig: CommandBrokerConfig
    private var process: Process? = null

    init {
        commandConfig = serverConfig.config as CommandBrokerConfig
    }

    override fun address(): Result<InetSocketAddress> {
        if (commandConfig.address == null) {
            return Result.failure(IllegalArgumentException("No address specified in config"))
        }
        val port = commandConfig.address?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 25565
        return runCatching { InetSocketAddress(commandConfig.address, port) }
    }

    override fun isRunning(): Boolean {
        return getStatus() == Status.RUNNING
    }

    override fun getStatus(): Status {
        return if (process?.isAlive == true) {
            Status.RUNNING
        } else {
            Status.STOPPED
        }
    }

    override fun startServer(): Result<Unit> {
        if (!isRunning()) {
            return runCatching {
                val commands = commandConfig.command
                logger?.debug("Starting server with command: ${commands.joinToString(" ")}")
                process = ProcessBuilder()
                    .command(commands)
                    .directory(File(commandConfig.workingDirectory))
                    .start()
            }
        }
        return Result.success(Unit)
    }

    override fun stopServer(): Result<Unit> {
        if (isRunning()) {
            process?.destroy()
        }

        return Result.success(Unit)
    }

    override fun removeServer(): Result<Unit> {
        // For the command broker there is no real difference between stopping and removing the server
        return stopServer()
    }

    override fun reconcile(config: ServerConfig): Result<Runnable?> {
        if (config.type != "cmd") {
            return Result.failure(IllegalArgumentException("Expected CommandBrokerConfig and got something else!"))
        }

        val newConfig = config.config as CommandBrokerConfig
        return if (newConfig != commandConfig) {
            Result.success(Runnable {
                stopServer()
                commandConfig = newConfig
                startServer()
            })
        } else {
            Result.success(Runnable {
                commandConfig = newConfig
            })
        }
    }
}
