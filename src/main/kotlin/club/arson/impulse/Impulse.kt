package club.arson.impulse

import club.arson.impulse.commands.createImpulseCommand
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import club.arson.impulse.config.ConfigManager
import club.arson.impulse.server.ServerManager
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(
    id = "impulse",
    name = "Impulse",
    version = BuildConstants.VERSION,
    authors = ["Dabb1e"],
    url = "https://github.com/ArsonClub/impulse",
    description = "Dynamically start, stop, and create servers with Velocity"
)
class Impulse @Inject constructor(val proxy: ProxyServer, val logger: Logger, @DataDirectory val dataDirectory: Path) {
    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Initializing Impulse")

        ServiceRegistry.instance.configManager = ConfigManager(proxy, this, dataDirectory, logger)
        ServiceRegistry.instance.serverManager = ServerManager(proxy, this, logger)
        proxy.eventManager.register(this, PlayerLifecycleListener(logger))

        // Register custom commands
        val commandManager = proxy.commandManager
        val impulseCommandMeta = commandManager.metaBuilder("impulse")
            .aliases("imp")
            .plugin(this)
            .build()
        commandManager.register(impulseCommandMeta, createImpulseCommand())
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        logger.info("Shutting down Impulse")
    }
}