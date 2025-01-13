package club.arson.impulse.config

import kotlinx.serialization.Serializable

@Serializable
data class Configuration(
    var instanceName: String = "velocity",
    var servers: List<ServerConfig> = listOf(),
    var serverMaintenanceInterval: Long = 300,
    var messages: Messages = Messages()
)

@Serializable
data class ServerConfig(
    var name: String,
    var type: String,
    var inactiveTimeout: Long = 0,
    var startupTimeout: Long = 120,
    var stopTimeout: Long = 120,
    var forceServerReconciliation: Boolean = true,
    var serverReconciliationGracePeriod: Long = 60,
    val shutdownBehavior: ShutdownBehavior = ShutdownBehavior.STOP,
    var docker: DockerServerConfig? = null,
    var kubernetes: KubernetesServerConfig? = null,
)

@Serializable
data class Messages(
    var startupError: String = "<red>Server is starting, please try again in a moment...</red>\nIf this issue persists, please contact an administrator",
    var reconcileRestartTitle: String = "<red>Server is Restarting...</red>",
    var reconcileRestartMessage: String = "server restart imminent"
)

@Serializable
data class DockerServerConfig(
    var image: String = "itzg/minecraft-server",
    var portBindings: List<String> = listOf("25565:25565"),
    var hostPath: String = "unix:///var/run/docker.sock",
    var volumes: Map<String, String> = emptyMap(),
    var env: Map<String, String> = mapOf("ONLINE_MODE" to "false"),
)

@Serializable
data class KubernetesServerConfig(
    var kind: String = "PaperMC",
    var apiVersion: String = "ogso.arson.club/v1alpha1",
    var namespace: String = "default",
)

@Serializable
enum class ShutdownBehavior {
    STOP,
    REMOVE
}