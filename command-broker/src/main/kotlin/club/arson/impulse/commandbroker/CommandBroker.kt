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

/**
 * This broker is designed to run raw commands on the backend to create a server. The resulting PID is then managed as
 * if it were a valid server.
 *
 * @property serverConfig Server configuration to create a command broker for
 * @property logger Logger ref for log messages
 */
class CommandBroker(serverConfig: ServerConfig, private val logger: Logger? = null) : Broker {
    private var commandConfig: CommandBrokerConfig
    private var process: Process? = null

    init {
        commandConfig = serverConfig.config as CommandBrokerConfig
    }

    /**
     * Get the address of the server
     *
     * @return A result containing the address of the server if we were able to get it, else an error
     */
    override fun address(): Result<InetSocketAddress> {
        if (commandConfig.address == null) {
            return Result.failure(IllegalArgumentException("No address specified in config"))
        }
        val port = commandConfig.address?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 25565
        return runCatching { InetSocketAddress(commandConfig.address, port) }
    }

    /**
     * Check if the server is running
     * @return true if the server is running, else false
     */
    override fun isRunning(): Boolean {
        return getStatus() == Status.RUNNING
    }

    /**
     * Get the status of the server
     * @return The status of the server
     */
    override fun getStatus(): Status {
        return if (process?.isAlive == true) {
            Status.RUNNING
        } else {
            Status.STOPPED
        }
    }

    /**
     * Attempt to start the server if it is not already running
     *
     * @return success if the server was started, else an error
     */
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

    /**
     * Attempt to stop the server if it is running
     *
     * @return success if the server was stopped, else an error
     */
    override fun stopServer(): Result<Unit> {
        if (isRunning()) {
            process?.destroy()
        }

        return Result.success(Unit)
    }

    /**
     * Attempt to remove the server
     *
     * Removing the server is the same as stopping it for the command broker since we can't reliably suspend the
     * subprocess cross-platform.
     * @return success if the server was removed, else an error
     */
    override fun removeServer(): Result<Unit> {
        // For the command broker there is no real difference between stopping and removing the server
        return stopServer()
    }

    /**
     * Reconcile any changes to our configuration.
     *
     * Since we are a generic broker, assume all changes require a restart.
     * @param config Server configuration to reconcile
     * @return the closure to actually do the reconciliation
     */
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
