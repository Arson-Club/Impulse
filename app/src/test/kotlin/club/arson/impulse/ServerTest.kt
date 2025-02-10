package club.arson.impulse

import club.arson.impulse.api.config.ReconcileBehavior
import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.api.server.Broker
import club.arson.impulse.api.server.Status
import club.arson.impulse.inject.MockServerModule
import club.arson.impulse.inject.modules.BaseModule
import club.arson.impulse.server.Server
import com.google.inject.Guice
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import com.velocitypowered.api.proxy.server.ServerPing
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ServerTest {
    private fun getServer(
        registeredServer: RegisteredServer = mockk(relaxed = true),
        proxyServer: ProxyServer = mockk(relaxed = true),
        broker: Broker = mockk(relaxed = true),
        config: ServerConfig = mockk(relaxed = true),
        logger: Logger = mockk(relaxed = true)
    ): Server {
        val eventManager = mockk<EventManager>(relaxed = true)
        every { proxyServer.eventManager } returns eventManager
        val injector = Guice.createInjector(
            BaseModule(mockk(), proxyServer, mockk(), logger),
            MockServerModule(registeredServer, broker, config)
        )
        return injector.getInstance(Server::class.java)
    }

    @Test
    fun testServerInit() {
        val proxy = mockk<ProxyServer>()
        assertDoesNotThrow { getServer(proxyServer = proxy) }
        verify(exactly = 1) { proxy.eventManager }
    }

    @Test
    fun testGetStatus() {
        val broker = mockk<Broker>()
        every { broker.getStatus() } returns Status.RUNNING
        val server = getServer(broker = broker)
        assertEquals(Status.RUNNING, server.getStatus())
    }

    @Test
    fun testStartServerSuccess() {
        val broker = mockk<Broker>()
        every { broker.startServer() } returns Result.success(Unit)
        val server = getServer(broker = broker)

        val result = server.startServer()
        assertTrue { result.isSuccess }
        assertEquals(server, result.getOrNull())
        verify(exactly = 1) { broker.startServer() }
    }

    @Test
    fun testStartServerFailure() {
        val broker = mockk<Broker>()
        every { broker.startServer() } returns Result.failure(RuntimeException("Test"))
        val server = getServer(broker = broker)

        val result = server.startServer()
        assertTrue { result.isFailure }
        verify(exactly = 1) { broker.startServer() }
    }

    @Test
    fun testStopServerSuccess() {
        val broker = mockk<Broker>()
        every { broker.stopServer() } returns Result.success(Unit)

        val config = mockk<ServerConfig>()
        every { config.lifecycleSettings.reconciliationBehavior } returns ReconcileBehavior.FORCE

        val server = getServer(broker = broker, config = config)

        val result = server.stopServer()
        assertTrue { result.isSuccess }
        assertEquals(server, result.getOrNull())
        verify(exactly = 1) { broker.stopServer() }
    }

    @Test
    fun testStopServerFailure() {
        val broker = mockk<Broker>()
        every { broker.stopServer() } returns Result.failure(RuntimeException("Test"))

        val server = getServer(broker = broker)

        val result = server.stopServer()
        assertTrue { result.isFailure }
        verify(exactly = 1) { broker.stopServer() }
    }

    @Test
    fun testStopPinnedServer() {
        val broker = mockk<Broker>()
        every { broker.stopServer() } returns Result.success(Unit)

        val server = getServer(broker = broker)
        server.pinned = true

        val result = server.stopServer()
        assertTrue { result.isFailure }
        verify(exactly = 0) { broker.stopServer() }
    }

    @Test
    fun testIsRunning() {
        val broker = mockk<Broker>()
        every { broker.isRunning() } returnsMany listOf(true, false)

        val server = getServer(broker = broker)
        assertTrue { server.isRunning() }
        assertFalse { server.isRunning() }
        verify(exactly = 2) { broker.isRunning() }
    }

    @Test
    fun testRemoveServerSuccess() {
        val broker = mockk<Broker>()
        every { broker.removeServer() } returns Result.success(Unit)

        val server = getServer(broker = broker)

        val result = server.removeServer()
        assertTrue { result.isSuccess }
        assertEquals(server, result.getOrNull())
        verify(exactly = 1) { broker.removeServer() }
    }

    @Test
    fun testRemoveServerFailure() {
        val broker = mockk<Broker>()
        every { broker.removeServer() } returns Result.failure(RuntimeException("Test"))

        val server = getServer(broker = broker)

        val result = server.removeServer()
        assertTrue { result.isFailure }
        verify(exactly = 1) { broker.removeServer() }
    }

    @Test
    fun testRemovePinnedServer() {
        val broker = mockk<Broker>()
        val server = getServer(broker = broker)
        server.pinned = true

        val result = server.removeServer()
        assertTrue { result.isFailure }
        verify(exactly = 0) { broker.removeServer() }
    }

    @Test
    fun testAwaitReadySuccess() {
        val config = mockk<ServerConfig>()
        every { config.lifecycleSettings.timeouts.startup } returns 1

        val serverRef = mockk<RegisteredServer>()
        val future = mockk<CompletableFuture<ServerPing>>()
        every { future.get() } throwsMany listOf(RuntimeException("Test")) andThen (mockk<ServerPing>())
        every { serverRef.ping() } returns future

        val server = getServer(registeredServer = serverRef, config = config)
        assertTrue { server.awaitReady().isSuccess }
        verify(exactly = 2) { serverRef.ping() }
    }

    @Test
    fun testAwaitReadyFailure() {
        val config = mockk<ServerConfig>()
        every { config.lifecycleSettings.timeouts.startup } returns 0

        val serverRef = mockk<RegisteredServer>()
        val future = mockk<CompletableFuture<ServerPing>>()
        every { future.get() } throws RuntimeException("Test")
        every { serverRef.ping() } returns future
        every { serverRef.serverInfo } returns mockk(relaxed = true)

        val server = getServer(registeredServer = serverRef, config = config)
        assertTrue { server.awaitReady().isFailure }
        verify(exactly = 0) { serverRef.ping() }
    }

    @Test
    fun testHandleDisconnectLastPlayer() {
        val player = mockk<Player>()
        every { player.username } returns "test"

        val serverRef = mockk<RegisteredServer>(relaxed = true)
        every { serverRef.playersConnected } returns mutableListOf(player)

        val config = mockk<ServerConfig>(relaxed = true)
        every { config.lifecycleSettings.allowAutoStop } returns true

        val server = getServer(registeredServer = serverRef, config = config)
        assertDoesNotThrow { server.handleDisconnect("test") }
    }

    @Test
    fun testHandleDisconnectLastPlayerPinned() {
        val player = mockk<Player>()
        every { player.username } returns "test"

        val serverRef = mockk<RegisteredServer>(relaxed = true)
        every { serverRef.playersConnected } returns mutableListOf(player)

        val config = mockk<ServerConfig>(relaxed = true)
        every { config.lifecycleSettings.allowAutoStop } returns true

        val server = getServer(registeredServer = serverRef, config = config)
        server.pinned = true
        assertDoesNotThrow { server.handleDisconnect("test") }
    }


    @Test
    fun testHandleDisconnectNotLastPlayer() {
        val players = mutableListOf(mockk<Player>(), mockk<Player>())
        players.forEachIndexed { index, value -> every { value.username } returns "test-$index" }

        val serverRef = mockk<RegisteredServer>(relaxed = true)
        every { serverRef.playersConnected } returns players

        val config = mockk<ServerConfig>(relaxed = true)
        every { config.lifecycleSettings.allowAutoStop } returns true

        val server = getServer(registeredServer = serverRef, config = config)
        assertDoesNotThrow { server.handleDisconnect("test-0") }
    }

    @Test
    fun testHandleDisconnectInvalidPlayer() {
        val player = mockk<Player>()
        every { player.username } returns "test"

        val serverRef = mockk<RegisteredServer>(relaxed = true)
        every { serverRef.playersConnected } returns mutableListOf(player)

        val config = mockk<ServerConfig>(relaxed = true)
        every { config.lifecycleSettings.allowAutoStop } returns true

        val server = getServer(registeredServer = serverRef, config = config)
        assertDoesNotThrow { server.handleDisconnect("test-bad") }
    }

    @Test
    fun testHandleDisconnectAutoStopDisabled() {
        val player = mockk<Player>()
        every { player.username } returns "test"

        val serverRef = mockk<RegisteredServer>(relaxed = true)
        every { serverRef.playersConnected } returns mutableListOf(player)

        val config = mockk<ServerConfig>(relaxed = true)
        every { config.lifecycleSettings.allowAutoStop } returns false

        val server = getServer(registeredServer = serverRef, config = config)
        assertDoesNotThrow { server.handleDisconnect("test") }
    }

    @Test
    fun testReconcileSuccessNoHandler() {
        val newConfig = mockk<ServerConfig>()
        every { newConfig.lifecycleSettings.reconciliationBehavior } returns ReconcileBehavior.FORCE
        val broker = mockk<Broker>()
        every { broker.reconcile(any()) } returns Result.success(null)

        val server = getServer(broker = broker)
        assertNotEquals(newConfig, server.config)
        val result = server.reconcile(newConfig)
        assertTrue { result.isSuccess }
        assertEquals(newConfig, server.config)
        verify(exactly = 1) { broker.reconcile(newConfig) }
    }

    @Test
    fun testReconcileSuccessWithHandler() {
        val newConfig = mockk<ServerConfig>()
        every { newConfig.lifecycleSettings.reconciliationBehavior } returns ReconcileBehavior.ON_STOP
        val broker = mockk<Broker>()
        every { broker.reconcile(any()) } returns Result.success(Runnable { })

        val server = getServer(broker = broker)
        val result = server.reconcile(newConfig)
        assertTrue { result.isSuccess }
        assertNotEquals(newConfig, server.config)
        verify(exactly = 1) { broker.reconcile(newConfig) }
    }

    @Test
    fun testReconcileSuccessScheduleHandler() {
        val newConfig = mockk<ServerConfig>(relaxed = true)
        every { newConfig.lifecycleSettings.reconciliationBehavior } returns ReconcileBehavior.FORCE
        val broker = mockk<Broker>()
        every { broker.reconcile(any()) } returns Result.success(Runnable { })
        val proxyServer = mockk<ProxyServer>(relaxed = true)

        val server = getServer(broker = broker, proxyServer = proxyServer)
        val result = server.reconcile(newConfig)
        assertTrue { result.isSuccess }
        assertNotEquals(newConfig, server.config)
        verify(exactly = 1) { broker.reconcile(newConfig) }
        verify(exactly = 1) { proxyServer.scheduler }
    }

    @Test
    fun testReconcileFailure() {
        val newConfig = mockk<ServerConfig>()
        val broker = mockk<Broker>()
        every { broker.reconcile(any()) } returns Result.failure(RuntimeException("Test"))

        val server = getServer(broker = broker)
        val result = server.reconcile(newConfig)
        assertTrue { result.isFailure }
        verify(exactly = 1) { broker.reconcile(newConfig) }
    }

    @Test
    fun testStopServerWithPendingReconciliation() {
        val broker = mockk<Broker>()
        every { broker.stopServer() } returns Result.success(Unit)
        every { broker.reconcile(any()) } returns Result.success(Runnable { })

        val config = mockk<ServerConfig>(relaxed = true)
        every { config.lifecycleSettings.reconciliationBehavior } returns ReconcileBehavior.ON_STOP

        val newConfig = mockk<ServerConfig>(relaxed = true)
        val server = getServer(broker = broker, config = config)

        val recResult = server.reconcile(newConfig)
        assertTrue { recResult.isSuccess }
        assertNotEquals(newConfig, server.config)
        verify(exactly = 1) { broker.reconcile(newConfig) }

        val stopResult = server.stopServer()
        assertTrue { stopResult.isSuccess }
        assertEquals(newConfig, server.config)
        verify(exactly = 2) { broker.stopServer() }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun testOnPlayerJoinDifferentServer() {
        val serverInfo = mockk<ServerInfo>(relaxed = true)
        every { serverInfo.name } returns "test-other"

        val serverConnection = mockk<ServerConnection>(relaxed = true)
        every { serverConnection.serverInfo } returns serverInfo

        val player = mockk<Player>()
        every { player.currentServer } returnsMany listOf(Optional.empty(), Optional.of(serverConnection))

        val event = mockk<ServerPostConnectEvent>(relaxed = true)
        every { event.player } returns player

        val config = mockk<ServerConfig>(relaxed = true)
        every { config.name } returns "test"

        val server = getServer(config = config)
        assertDoesNotThrow { server.onPlayerJoin(event) }
        assertDoesNotThrow { server.onPlayerJoin(event) }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun testOnPlayerJoinWithPendingTask() {
        val broker = mockk<Broker>()
        every { broker.reconcile(any()) } returns Result.success(Runnable { })

        val serverInfo = mockk<ServerInfo>(relaxed = true)
        every { serverInfo.name } returns "test"

        val serverConnection = mockk<ServerConnection>(relaxed = true)
        every { serverConnection.serverInfo } returns serverInfo

        val player = mockk<Player>(relaxed = true)
        every { player.currentServer } returns Optional.of(serverConnection)

        val event = mockk<ServerPostConnectEvent>(relaxed = true)
        every { event.player } returns player

        val config = mockk<ServerConfig>(relaxed = true)
        every { config.lifecycleSettings.reconciliationBehavior } returns ReconcileBehavior.FORCE
        every { config.name } returns "test"

        val server = getServer(broker = broker, config = config)

        assertDoesNotThrow { server.onPlayerJoin(event) }
        val recResult = server.reconcile(config)
        assertTrue { recResult.isSuccess }
        verify(exactly = 1) { broker.reconcile(config) }
        assertDoesNotThrow { server.onPlayerJoin(event) }
    }

    @Test
    fun testScheduleShutdownInvalidDelay() {
        val server = getServer()
        val result = server.scheduleShutdown(-1)
        assertTrue { result.isSuccess }
        assertEquals(result.getOrNull(), server)
    }

    @Test
    fun testScheduleShutdownAlreadyStopped() {
        val broker = mockk<Broker>()
        every { broker.isRunning() } returns false
        val server = getServer(broker = broker)
        val result = server.scheduleShutdown(0)
        assertTrue { result.isSuccess }
        assertEquals(result.getOrNull(), server)
    }

    @Test
    fun testScheduleShutdown() {
        val broker = mockk<Broker>(relaxed = true)
        every { broker.isRunning() } returns true
        val proxyServer = mockk<ProxyServer>(relaxed = true)
        val server = getServer(broker = broker, proxyServer = proxyServer)
        val result = server.scheduleShutdown(0)
        assertTrue { result.isSuccess }
        assertEquals(result.getOrNull(), server)
        verify(exactly = 1) { proxyServer.scheduler }
    }

    @Test
    fun testScheduleShutdownAlreadyScheduled() {
        val broker = mockk<Broker>(relaxed = true)
        every { broker.isRunning() } returns true
        val proxyServer = mockk<ProxyServer>(relaxed = true)
        val server = getServer(broker = broker, proxyServer = proxyServer)
        server.scheduleShutdown(0)
        val result = server.scheduleShutdown(0)
        assertTrue { result.isSuccess }
        assertEquals(result.getOrNull(), server)
        verify(exactly = 1) { proxyServer.scheduler }
    }
}
