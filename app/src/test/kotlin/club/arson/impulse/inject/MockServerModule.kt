package club.arson.impulse.inject

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.velocitypowered.api.proxy.server.RegisteredServer

class MockServerModule(
    private val registeredServer: RegisteredServer,
    private val broker: Broker,
    private val config: ServerConfig,
) : AbstractModule() {
    override fun configure() {
        bind(object : com.google.inject.TypeLiteral<RegisteredServer>() {})
            .toInstance(registeredServer)
    }

    @Provides
    fun provideBroker(): Broker {
        return broker
    }

    @Provides
    fun provideConfig(): ServerConfig {
        return config
    }
}