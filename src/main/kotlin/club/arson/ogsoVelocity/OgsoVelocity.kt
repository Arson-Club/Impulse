package club.arson.ogsoVelocity;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import club.arson.ogsoVelocity.config.ConfigManager
import club.arson.ogsoVelocity.server.ServerManager
import org.slf4j.Logger
import java.nio.file.Path

/*
 * Things to do:
 * - Fix checks to immediately reconcile if server is offline
 * - Add mechanism for offline reconciliation
 * - Add player count reconciliation and shutdown scheduling
 * - Add server locks
 * - change banner logic slightly
 */

@Plugin(
    id = "ogso-velocity", name = "ogso-velocity", version = BuildConstants.VERSION
)
class OgsoVelocity @Inject constructor(val proxy: ProxyServer, val logger: Logger, @DataDirectory val dataDirectory: Path) {
    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Initializing ogso-velocity")

        ServiceRegistry.instance.configManager = ConfigManager(proxy, this, dataDirectory, logger)
        ServiceRegistry.instance.serverManager = ServerManager(proxy, this, logger)
        proxy.eventManager.register(this, PlayerLifecycleListener(logger))
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        logger.info("Shutting down ogso-velocity")
    }
}