package club.arson.impulse.oraclebroker

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import club.arson.impulse.api.server.Status
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.ComputeClient
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Instance
import com.oracle.bmc.core.model.Vcn
import com.oracle.bmc.core.requests.GetInstanceRequest
import com.oracle.bmc.core.requests.GetVcnRequest
import com.oracle.bmc.core.requests.InstanceActionRequest
import com.oracle.bmc.core.requests.ListVnicAttachmentsRequest
import com.oracle.bmc.http.client.jersey3.Jersey3HttpProvider
import com.oracle.bmc.identity.IdentityClient
import com.oracle.bmc.resourcesearch.ResourceSearchClient
import com.oracle.bmc.resourcesearch.model.StructuredSearchDetails
import com.oracle.bmc.resourcesearch.requests.SearchResourcesRequest
import org.slf4j.Logger
import java.net.InetSocketAddress

class OracleCloudBroker(val config: ServerConfig, val logger: Logger? = null) : Broker {
    val identityClient: IdentityClient
    val computeClient: ComputeClient
    val vcnClient: VirtualNetworkClient
    val searchClient: ResourceSearchClient
    val ociConfig: OracleCloudConfig

    // TODO: Fix the config so we have access to this
    val impulseInstanceName = "FOO"

    var instanceId: String? = null
    fun instance(): Result<Instance> {
        if (instanceId == null) {
            return Result.failure(IllegalArgumentException("Instance ID is null!"))
        }
        val getInstanceRequest = GetInstanceRequest.builder().instanceId(instanceId).build()
        val instanceResponse = computeClient.getInstance(getInstanceRequest)
        return Result.success(instanceResponse.instance)
    }

    init {
        ociConfig = config.config as OracleCloudConfig
        val authDetailsProvider = ConfigFileAuthenticationDetailsProvider(ociConfig.advanced.ociProfile)
        identityClient = IdentityClient.builder().httpProvider(Jersey3HttpProvider()).build(authDetailsProvider)
        computeClient = ComputeClient.builder().httpProvider(Jersey3HttpProvider()).build(authDetailsProvider)
        vcnClient = VirtualNetworkClient.builder().httpProvider(Jersey3HttpProvider()).build(authDetailsProvider)
        searchClient = ResourceSearchClient.builder().httpProvider(Jersey3HttpProvider()).build(authDetailsProvider)

        // Set regions if specified
        if (ociConfig.advanced.region != null) {
            identityClient.setRegion(ociConfig.advanced.region)
            vcnClient.setRegion(ociConfig.advanced.region)
        }

        instanceId = getInstance(config.name).getOrNull()
    }

    private fun getInstance(serverName: String): Result<String> {
        val instanceSearchQuery = """
            query
                instance resources
            where
                (freeformTags.key = 'impulseInstance' && freeformTags.value = '${impulseInstanceName}')
                && (freeformTags.key = 'impulseServer' && freeformTags.value = '${serverName}')
        """.trimIndent()

        val searchRequest = SearchResourcesRequest.builder().searchDetails(
            StructuredSearchDetails.builder()
                .query(instanceSearchQuery)
                .build()
        ).build()
        val searchResponse = searchClient.searchResources(searchRequest)
        val instances = searchResponse.resourceSummaryCollection.items
        if (instances.isEmpty()) {
            return Result.failure(IllegalArgumentException("No instances found for server $serverName"))
        } else if (instances.size > 1) {
            logger?.warn("Found more than one instance for server $serverName!")
            logger?.warn("You may be leaking resources, please double check your configuration.")
        }

        return Result.success(instances[0].identifier)
    }

    private fun getVCN(vcnId: String): Vcn {
        val getVcnRequest = GetVcnRequest.builder().vcnId(vcnId).build()
        val getVcnResponse = vcnClient.getVcn(getVcnRequest)
        return getVcnResponse.vcn
    }

    private fun createInstance(startAfterCreate: Boolean = false): Result<Unit> {
        return if (startAfterCreate) {
            startInstance()
        } else {
            Result.success(Unit)
        }
    }

    private fun startInstance(): Result<Unit> {
        if (instanceId == null) {
            return Result.failure(IllegalArgumentException("Instance is null!"))
        }

        val startInstanceRequest = InstanceActionRequest.builder()
            .instanceId(instanceId)
            .action("START")
            .build()

        val response = computeClient.instanceAction(startInstanceRequest)
        return if (response.instance.lifecycleState == Instance.LifecycleState.Starting) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Failed to start instance!"))
        }
    }

    private fun stopInstance(): Result<Unit> {
        if (instanceId == null) {
            return Result.failure(IllegalArgumentException("Instance is null!"))
        }
        val stopInstanceRequest = InstanceActionRequest.builder()
            .instanceId(instanceId)
            .action("STOP")
            .build()

        val response = computeClient.instanceAction(stopInstanceRequest)
        return if (response.instance.lifecycleState == Instance.LifecycleState.Stopping) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Failed to stop instance!"))
        }
    }

    override fun getStatus(): Status {
        return when (instance().getOrNull()?.lifecycleState) {
            Instance.LifecycleState.Running -> Status.RUNNING
            Instance.LifecycleState.Stopped -> Status.STOPPED
            Instance.LifecycleState.Stopping -> Status.RUNNING
            Instance.LifecycleState.Starting -> Status.STARTING
            Instance.LifecycleState.Terminated -> Status.REMOVED
            Instance.LifecycleState.Terminating -> Status.REMOVED
            null -> Status.REMOVED
            else -> Status.UNKNOWN
        }
    }

    override fun isRunning(): Boolean {
        return getStatus() == Status.RUNNING
    }

    override fun address(): Result<InetSocketAddress> {
        if (instanceId == null) {
            return Result.failure(IllegalArgumentException("Instance ID is null!"))
        } else if (ociConfig.overrideAddress != null) {
            ociConfig.overrideAddress?.let {
                val parts = it.split(":")
                val port = parts.getOrNull(1)?.toIntOrNull() ?: 25565
                return Result.success(InetSocketAddress(parts[0], port))
            }
        }

        val vnicListResponse = computeClient.listVnicAttachments(
            ListVnicAttachmentsRequest.builder()
                .instanceId(instanceId)
                .build()
        )

        var address: String? = null
        for (vnic in vnicListResponse.items) {
            if
        }

        return instance().fold(
            onSuccess = { instance -> Result.success(InetSocketAddress(instance, 25565)) },
            onFailure = { Result.failure(it) }
        )
    }

    override fun startServer(): Result<Unit> {
        if (instanceId == null) {
            return createInstance(true)
        }
        return startInstance()
    }

    override fun stopServer(): Result<Unit> {
        if (instanceId == null) {
            return Result.success(Unit) // Nothing to stop
        }
        return stopInstance()
    }

    override fun removeServer(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun reconcile(config: ServerConfig): Result<Runnable?> {
        TODO("Not yet implemented")
    }
}