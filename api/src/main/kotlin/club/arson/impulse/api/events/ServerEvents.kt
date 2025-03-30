package club.arson.impulse.api.events

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer

data class ServerPreStartEvent(val server: RegisteredServer)
data class ServerStartedEvent(val server: RegisteredServer, val success: Boolean)
data class AwaitingServerReadyEvent(val server: RegisteredServer)
data class ImpulsePlayerTransferEvent(
    var previousServer: RegisteredServer?,
    var nextServer: RegisteredServer,
    var player: Player
)

data class ImpulsePlayerTransferErrorEvent(
    var previousServer: RegisteredServer?,
    var nextServer: RegisteredServer,
    var player: Player
)

class PreImpulsePlayerTransferEvent(
    var previousServer: RegisteredServer?,
    var nextServer: RegisteredServer,
    var player: Player,
    @JvmField var result: GenericResult = GenericResult.allowed()
) : ResultedEvent<GenericResult> {
    override fun getResult(): GenericResult {
        return result
    }

    override fun setResult(result: GenericResult) {
        this.result = result
    }
}