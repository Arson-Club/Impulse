package club.arson.impulse;

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

/*
 * Things to do:
 */

@Plugin(
    id = "impulse",
    name = "Impulse",
    version = BuildConstants.VERSION,
    authors = ["Dabb1e"],
    url = "https://github.com/ArsonClub/impulse",
    description = "A plugin for managing servers in a Velocity network"
)
class Impulse @Inject constructor(val proxy: ProxyServer, val logger: Logger, @DataDirectory val dataDirectory: Path) {
    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Initializing Impulse")

        ServiceRegistry.instance.configManager = ConfigManager(proxy, this, dataDirectory, logger)
        ServiceRegistry.instance.serverManager = ServerManager(proxy, this, logger)
        proxy.eventManager.register(this, PlayerLifecycleListener(logger))
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        logger.info("Shutting down Impulse")
    }
}