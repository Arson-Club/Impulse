/*
 *  Impulse Server Manager for Velocity
 *  Copyright (c) 2025  Dabb1e
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package club.arson.impulse

import club.arson.impulse.api.events.RegisterBrokerEvent
import club.arson.impulse.commands.createImpulseCommand
import club.arson.impulse.config.ConfigManager
import club.arson.impulse.inject.modules.BaseModule
import club.arson.impulse.inject.modules.BrokerModule
import club.arson.impulse.server.ServerManager
import com.google.inject.Guice
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path

/**
 * Main Velocity plugin class for Impulse
 *
 * @property proxy a ref to the Velocity proxy server
 * @property logger a ref to the Velocity logger
 * @property dataDirectory the path to the data directory
 * @constructor creates a new Impulse plugin instance
 */
@Plugin(
    id = "impulse",
    name = "Impulse",
    version = BuildConstants.VERSION,
    authors = ["Dabb1e"],
    url = "https://github.com/Arson-Club/Impulse",
    description = "Dynamically start, stop, and create servers with Velocity"
)
class Impulse @Inject constructor(
    private val proxy: ProxyServer,
    private val logger: Logger,
    @DataDirectory val dataDirectory: Path
) {
    private fun getLocalPlugins(): Set<File> {
        val pluginDirectory = dataDirectory.toFile()
        return if (pluginDirectory.isDirectory) {
            pluginDirectory.listFiles { file -> file.extension == "jar" }?.toSet() ?: emptySet()
        } else {
            emptySet()
        }
    }

    private fun loadPlugins(plugins: Set<File>) {
        plugins.forEach { plugin ->
            logger.info("Loading plugin $plugin")
            proxy.pluginManager.addToClasspath(this, plugin.toPath())
        }
    }

    /**
     * Handles the setup needed for Impulse
     *
     * This method is called when Impulse is initialized by Velocity. It primarily sets up the service registry as well
     * as registering the custom commands and top level event listeners.
     * @param event the ProxyInitializeEvent fired by Velocity
     */
    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Initializing Impulse")
        loadPlugins(getLocalPlugins())

        ServiceRegistry.instance.injector = Guice.createInjector(
            BrokerModule(logger), BaseModule(
                this, proxy, dataDirectory, logger
            )
        )

        ServiceRegistry.instance.configManager =
            ServiceRegistry.instance.injector!!.getInstance(ConfigManager::class.java)
        ServiceRegistry.instance.serverManager =
            ServiceRegistry.instance.injector!!.getInstance(ServerManager::class.java)
        proxy.eventManager.register(
            this,
            ServiceRegistry.instance.injector!!.getInstance(PlayerLifecycleListener::class.java)
        )

        // Register custom commands
        val commandManager = proxy.commandManager
        val impulseCommandMeta = commandManager.metaBuilder("impulse")
            .aliases("imp")
            .plugin(this)
            .build()
        commandManager.register(impulseCommandMeta, createImpulseCommand())
    }

    /**
     * Handles the cleanup needed for Impulse
     *
     * @param event the ProxyShutdownEvent fired by Velocity
     */
    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        logger.info("Shutting down Impulse")
    }

    /**
     * Handles when a broker is dynamically registered via the [RegisterBrokerEvent]
     *
     * @param event the RegisterBrokerEvent containing the jar ref
     */
    @Subscribe
    fun onRegisterBrokerEvent(event: RegisterBrokerEvent) {
        if (event.result.isAllowed) {
            loadPlugins(setOf(event.jarPath.toFile()))
        }
    }
}