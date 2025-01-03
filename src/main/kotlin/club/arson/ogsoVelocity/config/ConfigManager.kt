package club.arson.ogsoVelocity.config

import club.arson.ogsoVelocity.OgsoVelocity
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.decodeFromStream
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import org.slf4j.Logger
import java.nio.file.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.inputStream
import kotlin.io.path.name

class ConfigManager(val proxy: ProxyServer, val plugin: OgsoVelocity, val configDirectory: Path, val logger: Logger) {
    private val _watchTask: ScheduledTask
    private val _watchService: WatchService = FileSystems.getDefault().newWatchService()
    private var _liveConfig: Configuration = Configuration()

    companion object {
        const val CONFIG_FILE_NAME = "config.yaml"
    }

    init {
        _watchTask = proxy.scheduler.buildTask(plugin, this::_watchTask).repeat(5, TimeUnit.SECONDS).schedule()
        configDirectory.register(
            _watchService,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_CREATE
        )
        _fireAndReload()
    }

    fun getServers(): List<ServerConfig> {
        return _liveConfig.servers
    }

    fun getReconcileInterval(): Long {
        return _liveConfig.reconcileInterval
    }

    private fun _watchTask() {
        logger.trace("ConfigManager: Running watch task")
        var key: WatchKey?
        var gotUpdate = false
        while (_watchService.poll().also { key = it } != null) {
            key?.pollEvents()?.forEach { event ->
                val changedFile = event.context() as Path
                if (changedFile.name == CONFIG_FILE_NAME) {
                    gotUpdate = true
                }
            }
            if (key?.reset() == false) {
                key?.cancel()
                _watchService.close()
            }
        }
        if (gotUpdate) {
            _fireAndReload()
        }
    }

    private fun _fireAndReload() {
        var configEvent: ConfigReloadEvent
        try {
            val config = Yaml.default.decodeFromStream<Configuration>(
                configDirectory.resolve(CONFIG_FILE_NAME).inputStream()
            )
            configEvent = ConfigReloadEvent(config, ResultedEvent.GenericResult.allowed())
        } catch (e: YamlException) {
            logger.error("Failed to parse config file: ${e.message}")
            configEvent = ConfigReloadEvent(Configuration(), ResultedEvent.GenericResult.denied())
        }
        proxy.eventManager.fire(configEvent).thenAccept({ event ->
            if (event.result.isAllowed) {
                _liveConfig = event.config
                logger.info("Configuration reloaded")
            } else {
                logger.warn("Configuration failed to reload, keeping old config")
            }
        })
    }
}

//    companion object {
//        val INSTANCE: ConfigManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ConfigManager() }
//    }