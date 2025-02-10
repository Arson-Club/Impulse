package club.arson.impulse.inject.modules

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import club.arson.impulse.inject.providers.RegisteredServerProvider
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import org.slf4j.Logger

class ServerModule(
    private val proxy: ProxyServer,
    private val config: ServerConfig,
    private val broker: Broker,
    private val logger: Logger? = null
) : AbstractModule() {
    override fun configure() {
        bind(object : com.google.inject.TypeLiteral<RegisteredServer>() {})
            .toProvider(RegisteredServerProvider(proxy, config, broker, logger))
    }

    @Provides
    @Singleton
    fun provideBroker(): Broker {
        return broker
    }

    @Provides
    @Singleton
    fun provideServerConfig(): ServerConfig {
        return config
    }
}