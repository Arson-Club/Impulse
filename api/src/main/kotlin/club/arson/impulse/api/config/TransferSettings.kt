package club.arson.impulse.api.config

import kotlinx.serialization.Serializable

@Serializable
data class TransferSettings(
    var enableDisplays: Boolean = true,
)