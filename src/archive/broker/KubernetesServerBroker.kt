package club.arson.ogsoVelocity.server.broker

import club.arson.ogso.minecraft.models.V1alpha1ServerInstance
import club.arson.ogso.minecraft.models.V1alpha1ServerInstanceList
import club.arson.ogso.minecraft.models.V1alpha1ServerInstanceSpec
import club.arson.ogsoVelocity.ServiceRegistry
import club.arson.ogsoVelocity.server.Server
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.openapi.models.V1ObjectMeta
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.Watch
import io.kubernetes.client.util.generic.GenericKubernetesApi

class KubernetesServerBroker: ServerBroker {
    private val _serverInstanceApi: GenericKubernetesApi<V1alpha1ServerInstance, V1alpha1ServerInstanceList>
    private val _apiClient: ApiClient
    init {
        _apiClient = ClientBuilder.cluster().build()
        Configuration.setDefaultApiClient(_apiClient)
        _serverInstanceApi = GenericKubernetesApi(
            V1alpha1ServerInstance::class.java,
            V1alpha1ServerInstanceList::class.java,
            "minecraft.ogso.arson.club",
            "v1alpha1",
            "serverinstances",
            _apiClient
        )
    }

    private fun _awaitStart(name: String, namespace: String): Result<Unit> {
        return Result.success(Unit)
    }

    private fun _restartServer(name: String, namespace: String, template: String): Result<Unit> {
        // TODO: get the template labels to copy over ownership
        // TODO make instance name come from config file

        val createResult = _serverInstanceApi.create(
            V1alpha1ServerInstance()
                .metadata(V1ObjectMeta()
                    .name(name)
                    .namespace(namespace)
                    .labels(mapOf("ogso.arson.club/managed-by" to (ServiceRegistry.instance.configManager?.instanceName ?: "velocity")))
                )
                .spec(
                    V1alpha1ServerInstanceSpec()
                        .serverTemplate(template)
                )
        )

        return Result.success(Unit)

    }

    override fun startServer(server: Server): Result<Unit> {
        val name = server.config.name
        val namespace = server.config.namespace
        val instanceResponse = _serverInstanceApi.get(
            name,
            namespace
        )

        val instanceState = if (instanceResponse.isSuccess) instanceResponse.`object`.status?.currentState else null
        when (instanceState) {
            "Running" -> return Result.success(Unit)
            "Starting" -> return _awaitStart(name, namespace)
            "Stopping" -> return Result.failure(Exception("Server is stopping"))
            "Stopped" -> return Result.failure(Exception("Server is stopped"))
            else -> {
                return Result.failure(Exception("Server is in unknown state"))
            }
        }
    }
}