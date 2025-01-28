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

package club.arson.impulse.api.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents a server configuration
 *
 * @property name The name of the server. Must match the server name in Velocity's configuration
 * @property type The type of server. Must be either "docker" or "kubernetes"
 * @property inactiveTimeout The time in seconds, after the last player leaves, before a server is considered inactive
 * @property startupTimeout The time in seconds before a server is considered to have failed to start
 * @property stopTimeout The time in seconds before a server is considered to have failed to stop
 * @property forceServerReconciliation Whether to force server reconciliation immediately or wait for the next restart
 * @property serverReconciliationGracePeriod The time in seconds to wait before reconciling servers if [forceServerReconciliation] is true
 * @property shutdownBehavior The action to take when the server receives a shutdown signal. Either "STOP" or "REMOVE"
 */
@Serializable
data class ServerConfig(
    var name: String,
    var type: String,
    var inactiveTimeout: Long = 0,
    var startupTimeout: Long = 120,
    var stopTimeout: Long = 120,
    var forceServerReconciliation: Boolean = false,
    var serverReconciliationGracePeriod: Long = 60,
    var shutdownBehavior: ShutdownBehavior = ShutdownBehavior.STOP,
    @Transient var config: Any? = null
)
