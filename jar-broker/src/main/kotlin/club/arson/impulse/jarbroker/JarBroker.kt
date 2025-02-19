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

package club.arson.impulse.jarbroker

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import club.arson.impulse.api.server.Status
import org.slf4j.Logger
import java.io.File
import java.net.InetSocketAddress

class JarBroker(serverConfig: ServerConfig, private val logger: Logger? = null) : Broker {
    var jarConfig: JarBrokerConfig
    var process: Process? = null

    init {
        jarConfig = serverConfig.config as JarBrokerConfig
    }

    private fun getJarCommand(): List<String> {
        return mutableListOf(
            jarConfig.command,
            *jarConfig.javaFlags.toTypedArray(),
            "-jar", jarConfig.jarFile,
            *jarConfig.flags.toTypedArray()
        )
    }

    override fun address(): Result<InetSocketAddress> {
        if (jarConfig.address == null) {
            return Result.failure(IllegalArgumentException("No address specified in config"))
        }
        return runCatching { InetSocketAddress(jarConfig.address, 25565) }
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
                val commands = getJarCommand()
                logger?.debug("Starting server with command: ${commands.joinToString(" ")}")
                process = ProcessBuilder()
                    .command(commands)
                    .directory(File(jarConfig.workingDirectory))
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
        // For the jar broker there is no real difference between stopping and removing the server
        return stopServer()
    }

    override fun reconcile(config: ServerConfig): Result<Runnable?> {
        if (config.type != "jar") {
            return Result.failure(IllegalArgumentException("Expected JarServerConfig and got something else!"))
        }

        val newConfig = config.config as JarBrokerConfig
        if (newConfig != jarConfig) {
            return Result.success(Runnable {
                stopServer()
                jarConfig = newConfig
                startServer()
            })
        } else {
            return Result.success(Runnable {
                jarConfig = newConfig
            })
        }
    }
}