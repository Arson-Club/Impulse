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
import club.arson.impulse.dockerbroker.ImagePullPolicy.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Configuration for a Docker server
 *
 * This configuration is used when the server type is "docker".
 * @property image Docker image to use for the server
 * @property portBindings List of port mappings (i.e "25566:25565")
 * @property hostPath Path to docker socket (accepts network sockets)
 * @property volumes Volumes to mount into the container ("/host/path:/container/path")
 * @property env Map of environment variables to set in the container
 */
@BrokerConfig("docker")
@Serializable
data class DockerServerConfig(
    var address: String? = null,
    var image: String = "itzg/minecraft-server",
    var imagePullPolicy: ImagePullPolicy = IF_NOT_PRESENT,
    var autoStartOnCreate: Boolean = false,
    var portBindings: List<String> = listOf("25565:25565"),
    var hostPath: String = "unix:///var/run/docker.sock",
    var volumes: List<String> = emptyList(),
    var env: Map<String, String> = mapOf("ONLINE_MODE" to "false"),
)

/**
 * Image pull policy for a Docker container images
 *
 * @property ALWAYS Always pull the image
 * @property IF_NOT_PRESENT Pull the image if it is not present
 * @property NEVER Never pull the image
 */
@Serializable(with = ImagePullPolicySerializer::class)
enum class ImagePullPolicy(private val value: String) {
    ALWAYS("Always"),
    IF_NOT_PRESENT("IfNotPresent"),
    NEVER("Never");

    override fun toString(): String {
        return value
    }
}

/**
 * Serializer for [ImagePullPolicy]
 */
object ImagePullPolicySerializer : KSerializer<ImagePullPolicy> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ImagePullPolicy", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ImagePullPolicy) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): ImagePullPolicy {
        val value = decoder.decodeString()
        return ImagePullPolicy.entries.first { it.toString() == value }
    }
}