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

package club.arson.impulse.api.config

import club.arson.impulse.api.config.ReconcileBehavior.FORCE
import club.arson.impulse.api.config.ReconcileBehavior.ON_STOP
import kotlinx.serialization.Serializable

/**
 * Lifecycle settings for a server
 *
 * These are settings that are used to control the basic lifecycle of a server. This allows a user to override the
 * default behavior of the server manager.
 * @property timeouts Various timeouts used for the server
 * @property allowAutoStart Whether the server should be allowed to start automatically
 * @property allowAutoStop Whether the server should be allowed to stop automatically
 * @property shutdownBehavior The action to take when the server receives a shutdown signal. Either "STOP" or "REMOVE"
 */
@Serializable
data class LifecycleSettings(
    var timeouts: Timeouts = Timeouts(),
    var allowAutoStart: Boolean = true,
    var allowAutoStop: Boolean = true,
    var reconciliationBehavior: ReconcileBehavior = ON_STOP,
    var shutdownBehavior: ShutdownBehavior = ShutdownBehavior.STOP
)

/**
 * Various timeouts used for the server
 *
 * @property startup The time in seconds before a server is considered to have failed to start
 * @property shutdown The time in seconds before a server is considered to have failed to stop
 * @property reconciliationGracePeriod The time in seconds to wait before reconciling servers if
 *   [LifecycleSettings.reconciliationBehavior] is set to [ReconcileBehavior.FORCE]
 * @property inactiveGracePeriod The time in seconds to wait before shutting down an inactive servers (no players)
 */
@Serializable
data class Timeouts(
    var startup: Long = 120,
    var shutdown: Long = 120,
    var reconciliationGracePeriod: Long = 60,
    var inactiveGracePeriod: Long = 300,
)

/**
 * The behavior to take when reconciling a server
 * @property FORCE Reconcile the server immediately
 * @property ON_STOP Reconcile the server on the next stop
 */
@Serializable
enum class ReconcileBehavior {
    FORCE,
    ON_STOP
}