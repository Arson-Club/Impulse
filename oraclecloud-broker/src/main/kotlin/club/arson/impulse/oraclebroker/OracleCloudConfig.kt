package club.arson.impulse.oraclebroker

import club.arson.impulse.api.config.BrokerConfig
import kotlinx.serialization.Serializable

@BrokerConfig("oracle-cloud")
@Serializable
data class OracleCloudConfig(
    var compartmentId: String? = null,
    var vcnId: String,
    var preferPublicIp: Boolean = false,
    var overrideAddress: String? = null,
    var advanced: AdvancedOracleCloudConfig = AdvancedOracleCloudConfig()
)

@Serializable
data class AdvancedOracleCloudConfig(
    var kmsKeyId: String? = null,
    var ociProfile: String = "DEFAULT",
    var region: String? = null
)