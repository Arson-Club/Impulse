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

package club.arson.impulse.config

import club.arson.impulse.Impulse
import club.arson.impulse.ServiceRegistry
import club.arson.impulse.api.config.Configuration
import club.arson.impulse.api.config.Messages
import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.events.ConfigReloadEvent
import com.charleskorn.kaml.*
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializerOrNull
import org.slf4j.Logger
import java.nio.file.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.reflect.KClass

/**
 * Manages the configuration for Impulse
 *
 * This class will watch for changes to Impulse's configuration file. It will trigger reload events as when a change is
 * detected.
 * @property proxy a ref to the Velocity proxy server
 * @param plugin a ref to the Impulse plugin
 * @property configDirectory the path to the configuration directory
 * @property logger a ref to the Velocity logger
 * @constructor creates a new ConfigManager instance
 */
class ConfigManager(
    private val proxy: ProxyServer,
    plugin: Impulse,
    private val configDirectory: Path,
    private val logger: Logger
) {
    private val watchTask: ScheduledTask
    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private var liveConfig: Configuration = Configuration()
    private val yaml = Yaml(configuration = Yaml.default.configuration.copy(strictMode = false))

    /**
     * Holds static defines for the ConfigManager
     */
    companion object {
        /**
         * The name of the configuration file
         */
        const val CONFIG_FILE_NAME = "config.yaml"
    }

    /**
     * Register a watcher on the config directory, and load the initial configuration
     */
    init {
        watchTask = proxy.scheduler.buildTask(plugin, this::watchTask).repeat(5, TimeUnit.SECONDS).schedule()
        if (!configDirectory.exists()) {
            configDirectory.createDirectories()
        }
        configDirectory.register(
            watchService,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_CREATE
        )
        fireAndReload()
    }

    /**
     * The list of servers defined in the current "live" config
     */
    var servers: List<ServerConfig>
        get() = liveConfig.servers
        private set(value) {
            liveConfig.servers = value
        }

    /**
     * The interval in seconds for server maintenance tasks
     */
    var maintenanceInterval: Long
        get() = liveConfig.serverMaintenanceInterval
        private set(value) {
            liveConfig.serverMaintenanceInterval = value
        }

    /**
     * The name of this Impulse instance in the live config
     */
    var instanceName: String
        get() = liveConfig.instanceName
        private set(value) {
            liveConfig.instanceName = value
        }

    /**
     * The map of messages to send to players
     */
    var messages: Messages
        get() = liveConfig.messages
        private set(value) {
            liveConfig.messages = value
        }

    private fun watchTask() {
        logger.trace("ConfigManager: Running watch task")
        var key: WatchKey?
        var gotUpdate = false
        while (watchService.poll().also { key = it } != null) {
            key?.pollEvents()?.forEach { event ->
                val changedFile = event.context() as Path
                if (changedFile.name == CONFIG_FILE_NAME) {
                    gotUpdate = true
                }
            }
            if (key?.reset() == false) {
                key?.cancel()
                watchService.close()
            }
        }
        if (gotUpdate) {
            fireAndReload()
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun <T : Any> configHelper(clazz: KClass<T>): Result<KSerializer<T>> {
        clazz.serializerOrNull()?.let {
            return Result.success(it)
        }
        return Result.failure(IllegalArgumentException("class is not serializable"))
    }

    private fun fireAndReload() {
        var configEvent = ConfigReloadEvent(liveConfig, liveConfig, GenericResult.denied())
        var yamlNode: YamlNode? = null
        var config = Configuration()

        runCatching {
            // First we load in the base "global" configuration
            yamlNode = yaml.parseToYamlNode(configDirectory.resolve(CONFIG_FILE_NAME).inputStream())
            config = yaml.decodeFromYamlNode<Configuration>(yamlNode!!)
        }.onSuccess {
            val brokerConfigClasses = ServiceRegistry.instance.getServerBroker().configClasses
            // For each server we attempt to get it's "type" and use that to look up an appropriate Broker config class.
            // If we find one, we attempt to deserialize from the yamlNode AST and attach it as the "config" property of the ServerConfig
            yamlNode!!.yamlMap.get<YamlList>("servers")?.items?.forEach { server ->
                (server as YamlMap).get<YamlScalar>("type")?.content?.let { type ->
                    brokerConfigClasses[type]?.let { configClass ->
                        server.get<YamlMap>(type)?.let { rawBrokerConfig ->
                            configHelper(configClass).onSuccess { brokerConfigSerializer ->
                                val brokerConfig = yaml.decodeFromYamlNode(brokerConfigSerializer, rawBrokerConfig)
                                server.get<YamlScalar>("name")?.content?.let { name ->
                                    config.servers.find { it.name == name }?.config = brokerConfig
                                }
                            }.onFailure {
                                logger.warn("ConfigManager: Unable to deserialize broker config: ${it.message}")
                                configEvent = ConfigReloadEvent(liveConfig, liveConfig, GenericResult.denied())
                                return@onSuccess
                            }
                        }
                    }
                }
            }
            configEvent = ConfigReloadEvent(config, liveConfig, GenericResult.allowed())
        }.onFailure {
            logger.error("ConfigManager: Failed to parse config file: ${it.message}")
            configEvent = ConfigReloadEvent(liveConfig, liveConfig, GenericResult.denied())
        }


        proxy.eventManager.fire(configEvent).thenAccept { event ->
            if (event.result.isAllowed) {
                liveConfig = event.config
                logger.info("Configuration reloaded")
            } else {
                logger.warn("Configuration failed to reload, keeping old config")
            }
        }
    }
}