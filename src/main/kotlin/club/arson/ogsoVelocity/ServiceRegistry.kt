package club.arson.ogsoVelocity

import club.arson.ogsoVelocity.config.ConfigManager
import club.arson.ogsoVelocity.server.ServerManager

//import club.arson.ogsoVelocity.server.ServerManager

class ServiceRegistry {
    companion object {
        val instance: ServiceRegistry by lazy { ServiceRegistry() }
    }

    private var _configManager: ConfigManager? = null
    var serverManager: ServerManager? = null
        get() = field
        set(value) {
            if (_configManager != null) {
                throw IllegalStateException("ServerManager already registered")
            }
            field = value
        }

    var configManager: ConfigManager? = null
        get() = field
        set(value) {
            if (_configManager != null) {
                throw IllegalStateException("ConfigManager already registered")
            }
            field = value
        }
}