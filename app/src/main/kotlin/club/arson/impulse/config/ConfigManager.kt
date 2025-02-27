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
import club.arson.impulse.inject.modules.PluginDir
import com.charleskorn.kaml.*
import com.google.inject.Inject
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializerOrNull
import org.slf4j.Logger
import java.io.IOException
import java.nio.file.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
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
class ConfigManager @Inject constructor(
    private val proxy: ProxyServer,
    plugin: Impulse,
    @PluginDir private val configDirectory: Path,
    private val logger: Logger,
    reloadOnInit: Boolean = true
) {
    private val watchTask: ScheduledTask
    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private var liveConfig: Configuration = Configuration()
    private val yaml = Yaml(configuration = Yaml.default.configuration.copy())

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
        createDefaultConfigFileIfMissing()
        runCatching {
            configDirectory.register(
                watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE
            )
        }.onFailure {
            logger.error("ConfigManager: Failed to register watcher: ${it.message}")
            logger.error("ConfigManager: This probably means that we do not have permission to create or read the config directory ($configDirectory)")
            logger.error("ConfigManager: Please correct this and restart the proxy!")
        }.onSuccess {
            if (reloadOnInit) {
                fireAndReload()
            }
        }
    }

    private fun createDefaultConfigFileIfMissing() {
        try {
            // toFile to work around mocking issues with the Path API
            if (!configDirectory.toFile().exists()) {
                configDirectory.createDirectories()
            }
        } catch (e: IOException) {
            logger.error("ConfigManager: Unable to create the config directory: ${e.message}")
            logger.error("ConfigManager: Please make sure that Impulse has permission to create the config directory.")
            return
        }

        val configFile = configDirectory.resolve(CONFIG_FILE_NAME)
        try {
            // toFile to work around mocking issues with the Path API
            if (!configFile.toFile().exists()) {
                val defaultConfig = Configuration()
                val yamlString = yaml.encodeToString(Configuration.serializer(), defaultConfig)
                configFile.toFile().writeText(yamlString)
            }
        } catch (e: IOException) {
            logger.warn("ConfigManager: Unable to create the default config file: ${e.message}")
            logger.warn("ConfigManager: Please either create the file manually or give Impulse write permissions to $configDirectory")
        }
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
    private fun <T : Any> getBrokerConfigSerializer(clazz: KClass<T>): Result<KSerializer<T>> {
        clazz.serializerOrNull()?.let {
            return Result.success(it)
        }
        return Result.failure(IllegalArgumentException("class is not serializable"))
    }

    private inline fun <reified T> tryDecodeFromYamlNode(yamlNode: YamlNode): Result<T> {
        return runCatching {
            yaml.decodeFromYamlNode(yamlNode)
        }
    }

    private fun getServerConfig(server: YamlNode): Result<ServerConfig> {
        val brokerConfigClasses = ServiceRegistry.instance.getServerBroker().configClasses
        // Get the type of the server so we can get the correct broker config key
        return (server as YamlMap).get<YamlScalar>("type")?.content?.let { type ->
            brokerConfigClasses[type]?.let { brokerConfigClass ->
                // Partition out the broker config from the base server config
                val rawServerConfig: MutableMap<YamlScalar, YamlNode> = mutableMapOf()
                var rawBrokerConfig: YamlNode? = null
                server.entries.forEach {
                    if (it.key.content != type) {
                        rawServerConfig[it.key] = it.value
                    } else {
                        rawBrokerConfig = it.value
                    }
                }
                if (rawBrokerConfig == null) {
                    return Result.failure(Throwable("No broker config found for server type $type"))
                }
                // Try and decode the server config, then the broker config into that
                tryDecodeFromYamlNode<ServerConfig>(YamlMap(rawServerConfig, server.path))
                    .onSuccess { serverConfig ->
                        getBrokerConfigSerializer(brokerConfigClass).onSuccess { serializer ->
                            runCatching { yaml.decodeFromYamlNode(serializer, rawBrokerConfig!!) }
                                .onSuccess { serverConfig.config = it }
                        }
                    }
            } ?: Result.failure(Throwable("No broker config registered for type $type"))
        } ?: Result.failure(IllegalArgumentException("Server type not specified"))
    }

    /**
     * Parses the new config, fires a [club.arson.impulse.api.events.ConfigReloadEvent] and reloads the config if
     * allowed.
     *
     * This function is more complex to allow us to keep strict parsing on even while deserializing into arbitrary
     * broker config classes. This provides much better error reporting to the user.
     */
    private fun fireAndReload() {
        var configEvent = ConfigReloadEvent(liveConfig, liveConfig, GenericResult.denied())
        var yamlNode: YamlNode? = null
        var config: Configuration

        runCatching {
            // First we get the config as a node tree
            yamlNode = yaml.parseToYamlNode(configDirectory.resolve(CONFIG_FILE_NAME).inputStream())
        }.onSuccess {
            // Then we parse the servers into a list
            val servers = mutableListOf<ServerConfig>()
            yamlNode!!.yamlMap.get<YamlList>("servers")?.items?.forEach { server ->
                getServerConfig(server)
                    .onSuccess { servers.add(it) }
                    .onFailure {
                        logger.error("ConfigManager: Failed to parse server config: ${it.message}")
                        configEvent = ConfigReloadEvent(liveConfig, liveConfig, GenericResult.denied())
                    }
            }
            // Next we remove the servers from the raw config
            val rawConfig = mutableMapOf<YamlScalar, YamlNode>()
            yamlNode!!.yamlMap.entries.forEach {
                if (it.key.content != "servers") {
                    rawConfig[it.key] = it.value
                }
            }
            // We then parse the global config and add back in the servers
            tryDecodeFromYamlNode<Configuration>(YamlMap(rawConfig, yamlNode!!.path))
                .onSuccess {
                    config = it
                    config.servers = servers
                    configEvent = ConfigReloadEvent(config, liveConfig, GenericResult.allowed())
                }
                .onFailure {
                    logger.error("ConfigManager: Failed to parse global config: ${it.message}")
                    configEvent = ConfigReloadEvent(liveConfig, liveConfig, GenericResult.denied())
                }
        }.onFailure {
            if (it is EmptyYamlDocumentException || it is IOException) {
                logger.warn("ConfigManager: Config file is empty or missing, using default configuration")
                logger.warn("ConfigManager: Please make sure that Impulse has permission to read the config file.")
                logger.warn("ConfigManager: For more information on setting up Impulse see our documentation: https://arson-club.github.io/Impulse/getting_started/index.html")
                configEvent = ConfigReloadEvent(Configuration(), liveConfig, GenericResult.allowed())
            } else {
                logger.error("ConfigManager: Failed to parse config file: ${it.message}")
                configEvent = ConfigReloadEvent(liveConfig, liveConfig, GenericResult.denied())
            }
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