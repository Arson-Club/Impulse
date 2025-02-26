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
import java.net.InetSocketAddress

/**
 * Common interface for server brokers.
 */
interface Broker {
    /**
     * Should return the current [Status] of the server
     */
    fun getStatus(): Status

    /**
     * Should return the address used to connect players to the server.
     *
     * This is only used when the server is dynamically registered with Velocity.
     */
    fun address(): Result<InetSocketAddress>

    /**
     * Should return true if the server is running, false otherwise.
     */
    fun isRunning(): Boolean

    /**
     * Should start the server, and kick off any actions needed to get it ready to accept players.
     */
    fun startServer(): Result<Unit>

    /**
     * Should stop the server.
     *
     * Semantically, this should "pause" the server leaving the resources in a state that can be resumed. If this does
     * not make sense for the broker type then it should act the same as "remove".
     */
    fun stopServer(): Result<Unit>

    /**
     * Should remove the server.
     *
     * This should stop the server and clean up any runtime resources. It should NOT delete any persistent data volumes.
     */
    fun removeServer(): Result<Unit>

    /**
     * Trigger a reconciliation to resolve any differences between the server configuration and the runtime state of
     * the server.
     *
     * This should return a [Runnable] that can be used to perform the reconciliation if there is no work to be done.
     */
    fun reconcile(config: ServerConfig): Result<Runnable?>
}