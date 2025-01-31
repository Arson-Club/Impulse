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

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents a server configuration
 *
 * @property name The name of the server. Must match the server name in Velocity's configuration
 * @property type The type of server. Must be either "docker" or "kubernetes"
 * @property lifecycleSettings The lifecycle settings for the server
 * @property config The broker specific configuration, this is not set directly, but rather injected by the config manager
 */
@Serializable
data class ServerConfig(
    var name: String,
    var type: String,
    var lifecycleSettings: LifecycleSettings = LifecycleSettings(),
    @Transient var config: Any? = null
)