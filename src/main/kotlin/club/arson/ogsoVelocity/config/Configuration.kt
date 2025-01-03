package club.arson.ogsoVelocity.config

import kotlinx.serialization.Serializable

@Serializable
data class Configuration(
    var servers: List<ServerConfig> = listOf(),
    var reconcileInterval: Long = 300
)

@Serializable
data class ServerConfig(
    var name: String,
    var kind: String = "PaperMC",
    var apiVersion: String = "ogso.arson.club/v1alpha1",
    var namespace: String = "default",
    var inactiveTimeout: Long = 0
)