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

import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.events.ConfigReloadEvent
import club.arson.impulse.api.server.Broker
import club.arson.impulse.config.ConfigManager
import club.arson.impulse.inject.modules.BaseModule
import club.arson.impulse.server.Server
import club.arson.impulse.server.ServerBroker
import club.arson.impulse.server.ServerManager
import com.google.inject.Guice
import com.google.inject.Injector
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.slf4j.Logger
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ServerManagerTest {
    private fun getServerManager(
        proxyServer: ProxyServer = mockk(relaxed = true),
        impulse: Impulse = mockk(relaxed = true),
        logger: Logger = mockk(relaxed = true),
        registeredServer: RegisteredServer? = mockk<RegisteredServer>(relaxed = true),
        server: Server = mockk(relaxed = true),
        broker: Result<Broker> = Result.success(mockk(relaxed = true)),
        childInjector: Injector? = null,
        serverBroker: ServerBroker = mockk<ServerBroker>(relaxed = true).apply {
            every {
                createFromConfig(
                    any(),
                    any()
                )
            } returns broker
        },
    ): ServerManager {
        every { proxyServer.getServer(any()) } returns Optional.ofNullable(registeredServer)
        val injector = Guice.createInjector(
            BaseModule(impulse, proxyServer, mockk(relaxed = true), logger),
        )

        val ci: Injector = childInjector ?: mockk<Injector>().apply {
            every { getInstance(Server::class.java) } returns server
        }

        val mockInjector = mockk<Injector>()
        every { mockInjector.createChildInjector(*anyVararg()) } returns ci
        every { mockInjector.getInstance(ServerBroker::class.java) } returns serverBroker

        ServiceRegistry.instance.injector = mockInjector
        return injector.getInstance(ServerManager::class.java)
    }

    private fun createEvent(
        oldServers: List<ServerConfig> = emptyList(),
        newServers: List<ServerConfig> = emptyList(),
        result: GenericResult = GenericResult.allowed()
    ): ConfigReloadEvent {
        val event = mockk<ConfigReloadEvent>(relaxed = true)
        every { event.oldConfig.servers } returns oldServers
        every { event.config.servers } returns newServers
        event.result = result
        return event
    }

    @BeforeEach
    fun setUp() {
        ServiceRegistry.instance.reset()
    }

    @Test
    fun testServerManagerInit() {
        val proxyServer = mockk<ProxyServer>(relaxed = true)
        assertDoesNotThrow { getServerManager(proxyServer) }
        verify(exactly = 1) { proxyServer.eventManager }
        verify(exactly = 1) { proxyServer.scheduler }
    }

    @Test
    fun testServerManagerInitWithServers() {
        val proxyServer = mockk<ProxyServer>(relaxed = true)

        val serverConfig = mockk<ServerConfig>(relaxed = true)
        every { serverConfig.name } returns "test"
        every { serverConfig.type } returns "test"

        val configManager = mockk<ConfigManager>(relaxed = true)
        every { configManager.servers } returns listOf(serverConfig)
        ServiceRegistry.instance.configManager = configManager

        val serverManager = getServerManager(proxyServer)
        verify(exactly = 1) { proxyServer.eventManager }
        verify(exactly = 1) { proxyServer.scheduler }
        assertEquals(listOf("test"), serverManager.servers.keys.toList())
    }

    @Test
    fun testConfigReloadEventReturnsHandler() {
        val serverManager = getServerManager()

        val event = createEvent()
        val task = serverManager.onConfigReloadEvent(event)
        assertNotNull(task)
    }

    @Test
    fun testHandleDeniedEvent() {
        val serverManager = getServerManager()

        val serverConfig = mockk<ServerConfig>(relaxed = true)
        every { serverConfig.name } returns "test"
        every { serverConfig.type } returns "test"

        val event = createEvent(newServers = listOf(serverConfig), result = GenericResult.denied())
        serverManager.handleConfigReloadEvent(event)
        assertEquals(emptyList(), serverManager.servers.keys.toList())
    }

    @Test
    fun testReconcileAddServer() {
        val serverManager = getServerManager()

        val serverConfig = mockk<ServerConfig>(relaxed = true)
        every { serverConfig.name } returns "test"
        every { serverConfig.type } returns "test"

        val event = createEvent(newServers = listOf(serverConfig))

        serverManager.handleConfigReloadEvent(event)
        assertEquals(listOf("test"), serverManager.servers.keys.toList())
    }

    @Test
    fun testReconcileAddServerNoBroker() {
        val broker = Result.failure<Broker>(RuntimeException("test"))
        val serverManager = getServerManager(broker = broker)

        val serverConfig = mockk<ServerConfig>(relaxed = true)
        every { serverConfig.name } returns "test"
        every { serverConfig.type } returns "test"

        val event = createEvent(newServers = listOf(serverConfig))

        serverManager.handleConfigReloadEvent(event)
        assertEquals(listOf(), serverManager.servers.keys.toList())
    }

    @Test
    fun testReconcileAddServerBadInjection() {
        val injector = mockk<Injector>().apply {
            every { getInstance(Server::class.java) } throws RuntimeException("test")
        }
        val serverManager = getServerManager(childInjector = injector)

        val serverConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
        }

        val event = createEvent(newServers = listOf(serverConfig))

        serverManager.handleConfigReloadEvent(event)
        assertEquals(listOf(), serverManager.servers.keys.toList())
        verify(exactly = 1) { injector.getInstance(Server::class.java) }
    }

    @Test
    fun testReconcileAddServerBrokerFactoryException() {
        val injector = mockk<Injector>().apply {
            every { getInstance(Server::class.java) } throws RuntimeException("test")
        }
        val serverBroker = mockk<ServerBroker>(relaxed = true).apply {
            every { createFromConfig(any(), any()) } throws RuntimeException("test")
        }
        val serverManager = getServerManager(serverBroker = serverBroker)

        val serverConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
        }

        val event = createEvent(newServers = listOf(serverConfig))

        serverManager.handleConfigReloadEvent(event)
        assertEquals(listOf(), serverManager.servers.keys.toList())
        verify(exactly = 0) { injector.getInstance(Server::class.java) }
    }

    @Test
    fun testReconcileRemoveServer() {
        val proxyServer = mockk<ProxyServer>(relaxed = true)

        val serverConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
        }

        val configManager = mockk<ConfigManager>(relaxed = true)
        every { configManager.servers } returns listOf(serverConfig)
        ServiceRegistry.instance.configManager = configManager

        val serverManager = getServerManager(proxyServer)
        assertEquals(listOf("test"), serverManager.servers.keys.toList())

        val event = createEvent(oldServers = listOf(serverConfig))
        serverManager.handleConfigReloadEvent(event)
        assertEquals(emptyList(), serverManager.servers.keys.toList())
        verify(exactly = 1) { proxyServer.unregisterServer(any()) }
    }

    @Test
    fun testReconcileRemoveServerFailure() {
        val serverConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
        }

        val configManager = mockk<ConfigManager>(relaxed = true)
        every { configManager.servers } returns listOf(serverConfig)
        ServiceRegistry.instance.configManager = configManager

        val logger = mockk<Logger>(relaxed = true)
        val server = mockk<Server>(relaxed = true)
        every { server.removeServer() } returns Result.failure(RuntimeException("test"))

        val serverManager = getServerManager(server = server, logger = logger)
        assertEquals(listOf("test"), serverManager.servers.keys.toList())

        val event = createEvent(oldServers = listOf(serverConfig))
        serverManager.handleConfigReloadEvent(event)
        assertEquals(emptyList(), serverManager.servers.keys.toList())
        verify(exactly = 1) { logger.error(any()) }
    }

    @Test
    fun testReconcileNoConfigChangeForServer() {
        val server = mockk<Server>(relaxed = true)
        val serverManager = getServerManager(server = server)

        val serverConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
        }

        // inject the server
        var event = createEvent(oldServers = listOf(), newServers = listOf(serverConfig))
        serverManager.handleConfigReloadEvent(event)

        // Call it again with no config changes
        event = createEvent(oldServers = listOf(serverConfig), newServers = listOf(serverConfig))
        serverManager.handleConfigReloadEvent(event)
        verify(exactly = 0) { server.reconcile(any()) }
    }

    @Test
    fun testReconcileUpdatesServerSuccess() {
        val logger = mockk<Logger>(relaxed = true)
        val server = mockk<Server>(relaxed = true).apply {
            every { reconcile(any()) } returns Result.success(this)
        }
        val serverManager = getServerManager(server = server, logger = logger)

        val oldServerConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
        }

        // inject the server
        var event = createEvent(oldServers = listOf(), newServers = listOf(oldServerConfig))
        serverManager.handleConfigReloadEvent(event)

        // "Update" the config and call again
        val newServerConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
            every { lifecycleSettings.allowAutoStop } returns true
        }
        event = createEvent(oldServers = listOf(oldServerConfig), newServers = listOf(newServerConfig))
        serverManager.handleConfigReloadEvent(event)
        verify(exactly = 1) { server.reconcile(any()) }
        verify(exactly = 0) { logger.error(any()) }
    }

    @Test
    fun testReconcileUpdatesServerFailure() {
        val logger = mockk<Logger>(relaxed = true)
        val server = mockk<Server>(relaxed = true).apply {
            every { reconcile(any()) } returns Result.failure(RuntimeException("test"))
        }
        val serverManager = getServerManager(server = server, logger = logger)

        val oldServerConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
        }

        // inject the server
        var event = createEvent(oldServers = listOf(), newServers = listOf(oldServerConfig))
        serverManager.handleConfigReloadEvent(event)

        // "Update" the config and call again
        val newServerConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
            every { lifecycleSettings.allowAutoStop } returns true
        }
        event = createEvent(oldServers = listOf(oldServerConfig), newServers = listOf(newServerConfig))
        serverManager.handleConfigReloadEvent(event)
        verify(exactly = 1) { server.reconcile(any()) }
        verify(exactly = 1) { logger.error(any()) }
    }

    @Test
    fun testGetServerDoesNotExist() {
        val serverManager = getServerManager()
        val server = serverManager.getServer("test")
        assertNull(server)
    }

    @Test
    fun testGetServerExists() {
        val serverManager = getServerManager()
        val serverConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
        }

        val event = createEvent(newServers = listOf(serverConfig))
        serverManager.handleConfigReloadEvent(event)

        val server = serverManager.getServer("test")
        assertNotNull(server)
    }

    @Test
    fun testServerMaintenanceSuccess() {
        val serverConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
            every { lifecycleSettings.allowAutoStop } returns true
        }
        val server = mockk<Server>(relaxed = true).apply {
            every { serverRef } returns mockk<RegisteredServer>(relaxed = true).apply {
                every { playersConnected } returns emptyList()
            }
            every { config } returns serverConfig
        }

        val serverManager = getServerManager(server = server)
        val event = createEvent(newServers = listOf(serverConfig))
        serverManager.handleConfigReloadEvent(event)
        serverManager.serverMaintenance()
        verify(exactly = 1) { server.scheduleShutdown(any()) }
    }

    @Test
    fun testServerMaintenanceServerPinned() {
        val serverConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
            every { lifecycleSettings.allowAutoStop } returns true
        }
        val server = mockk<Server>(relaxed = true).apply {
            every { serverRef } returns mockk<RegisteredServer>(relaxed = true).apply {
                every { playersConnected } returns emptyList()
            }
            every { config } returns serverConfig
            every { pinned } returns true
        }

        val serverManager = getServerManager(server = server)
        val event = createEvent(newServers = listOf(serverConfig))
        serverManager.handleConfigReloadEvent(event)
        serverManager.serverMaintenance()
        verify(exactly = 0) { server.scheduleShutdown(any()) }
    }

    @Test
    fun testServerMaintenancePlayersConnected() {
        val serverConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
            every { lifecycleSettings.allowAutoStop } returns true
        }
        val server = mockk<Server>(relaxed = true).apply {
            every { serverRef } returns mockk<RegisteredServer>(relaxed = true).apply {
                every { playersConnected } returns listOf(mockk())
            }
            every { config } returns serverConfig
        }

        val serverManager = getServerManager(server = server)
        val event = createEvent(newServers = listOf(serverConfig))
        serverManager.handleConfigReloadEvent(event)
        serverManager.serverMaintenance()
        verify(exactly = 0) { server.scheduleShutdown() }
    }

    @Test
    fun testServerMaintenanceAutoStopDisabled() {
        val serverConfig = mockk<ServerConfig>(relaxed = true).apply {
            every { name } returns "test"
            every { type } returns "test"
            every { lifecycleSettings.allowAutoStop } returns false
        }
        val server = mockk<Server>(relaxed = true).apply {
            every { serverRef } returns mockk<RegisteredServer>(relaxed = true).apply {
                every { playersConnected } returns emptyList()
            }
            every { config } returns serverConfig
        }

        val serverManager = getServerManager(server = server)
        val event = createEvent(newServers = listOf(serverConfig))
        serverManager.handleConfigReloadEvent(event)
        serverManager.serverMaintenance()
        verify(exactly = 0) { server.scheduleShutdown(any()) }
    }
}