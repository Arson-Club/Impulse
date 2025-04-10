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

/**
 * The behavior to take when a server receives a shutdown signal
 *
 */
@Serializable
enum class ShutdownBehavior {
    /**
     * Stop the server.
     *
     * Normally this means leaving the resources for the server intact in a "paused" state,
     * but the implementation may vary depending on the server broker
     */
    STOP,

    /**
     * Remove the server.
     *
     * This will delete the server resources and free up the resources (excluding persistent
     * volumes), but the implementation may vary depending on the server broker
     */
    REMOVE
}
