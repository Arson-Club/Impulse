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

package club.arson.impulse.dockerbroker

import club.arson.impulse.api.config.BrokerConfig
import kotlinx.serialization.Serializable

/**
 * Configuration for a Docker server
 *
 * This configuration is used when the server type is "docker".
 * @property image Docker image to use for the server
 * @property portBindings List of port mappings (i.e "25566:25565")
 * @property hostPath Path to docker socket (accepts network sockets)
 * @property volumes Volumes to mount into the container ("/host/path": "/container/path")
 * @property env Map of environment variables to set in the container
 */
@BrokerConfig("docker")
@Serializable
data class DockerServerConfig(
    var image: String = "itzg/minecraft-server",
    var portBindings: List<String> = listOf("25565:25565"),
    var hostPath: String = "unix:///var/run/docker.sock",
    var volumes: Map<String, String> = emptyMap(),
    var env: Map<String, String> = mapOf("ONLINE_MODE" to "false"),
)
