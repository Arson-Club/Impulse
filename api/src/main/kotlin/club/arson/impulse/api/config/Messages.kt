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
 * Message text for user facing messages
 *
 * These messages are used when presenting information to players. They support [MiniMessage](https://docs.advntr.dev/minimessage/format)
 * format. Customize these messages to match your server's branding.
 *
 * @property startupError The message to display when the server times out during startup
 * @property reconcileRestartTitle The title of the message to display during the grace period before server reconciliation (restarts)
 * @property reconcileRestartMessage The message to display during the grace period before server reconciliation (restarts)
 */
@Serializable
data class Messages(
    var startupError: String = "<red>Server is starting, please try again in a moment...</red>\nIf this issue persists, please contact an administrator",
    var reconcileRestartTitle: String = "<red>Server is Restarting...</red>",
    var reconcileRestartMessage: String = "server restart imminent"
)
