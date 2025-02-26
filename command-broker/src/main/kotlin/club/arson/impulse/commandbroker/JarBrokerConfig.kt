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

import club.arson.impulse.api.config.BrokerConfig
import kotlinx.serialization.Serializable

@BrokerConfig("jar")
@Serializable
data class JarBrokerConfig(
    var workingDirectory: String,
    var address: String? = null,
    var jarFile: String,
    var javaFlags: List<String> = emptyList(),
    var flags: List<String> = emptyList()
)

fun toCommandBrokerConfig(config: JarBrokerConfig): CommandBrokerConfig {
    return CommandBrokerConfig(
        config.workingDirectory,
        listOf("java") + config.javaFlags + listOf("-jar", config.jarFile) + config.flags,
        config.address
    )
}