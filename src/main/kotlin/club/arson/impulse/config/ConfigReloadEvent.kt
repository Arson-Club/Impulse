package club.arson.impulse.config

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult

class ConfigReloadEvent(var config: Configuration, var oldConfig: Configuration, var genericResult: GenericResult = GenericResult.allowed()): ResultedEvent<GenericResult> {
    override fun getResult(): GenericResult {
        return genericResult
    }

    override fun setResult(result: GenericResult) {
        genericResult = result
    }
}