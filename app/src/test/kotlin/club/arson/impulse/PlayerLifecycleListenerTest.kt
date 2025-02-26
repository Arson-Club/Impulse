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

import club.arson.impulse.server.Server
import club.arson.impulse.server.ServerManager
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent.ServerResult
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.server.RegisteredServer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.kyori.adventure.text.Component
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import java.util.*

class PlayerLifecycleListenerTest {
    private lateinit var playerLifecycleListener: PlayerLifecycleListener
    private lateinit var logger: Logger

    @BeforeEach
    fun setUp() {
        logger = mockk<Logger>(relaxed = true)
        playerLifecycleListener = PlayerLifecycleListener(logger)
        ServiceRegistry.instance.reset()
    }

    @Test
    fun testDisconnectEventHandler() {
        val event = mockk<DisconnectEvent>()
        val player = mockk<Player>()
        val serverConnection = mockk<ServerConnection>()
        val server = mockk<RegisteredServer>()

        every { server.serverInfo.name } returns "test"

        every { serverConnection.server }
            .returnsMany(server, server, server)
            .andThenThrows(RuntimeException("Test"))

        every { player.currentServer }
            .returns(Optional.of(serverConnection))
        every { player.username }
            .returns("test")
        every { event.player }
            .returns(player)

        val serverManager = mockk<ServerManager>()
        every { serverManager.getServer(any()) } returnsMany listOf(
            null,
            mockk(relaxed = true)
        )

        // Run with a null serverManager
        playerLifecycleListener.onDisconnectEvent(event)

        // Run with a null server
        ServiceRegistry.instance.serverManager = serverManager
        playerLifecycleListener.onDisconnectEvent(event)
        verify(exactly = 1) { serverManager.getServer(any()) }

        // Run with a valid server
        playerLifecycleListener.onDisconnectEvent(event)
        verify(exactly = 2) { serverManager.getServer(any()) }

        // Run with internal throw getting connection
        playerLifecycleListener.onDisconnectEvent(event)
        verify(exactly = 1) { logger.debug(any()) }
    }

    @Test
    fun testServerPreConnectEvent() {
        val event = mockk<ServerPreConnectEvent>()

        every { event.player.username } returns "test"
        every { event.previousServer } returns null
        every { event.originalServer.serverInfo.name } returns "test"

        playerLifecycleListener.onServerPreConnectEvent(event)
    }

    @Test
    fun testConnectToInvalidServer() {
        val event = mockk<ServerPreConnectEvent>(relaxed = true)
        every { event.originalServer.serverInfo.name } returns "test"

        val serverManager = mockk<ServerManager>()
        every { serverManager.getServer("test") } returns null
        ServiceRegistry.instance.serverManager = serverManager

        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 1) { serverManager.getServer("test") }
        verify(exactly = 1) { logger.debug(any()) }
    }

    @Test
    fun testSuccessfulConnectionToRunningServer() {
        val player = mockk<Player>(relaxed = true)

        val event = mockk<ServerPreConnectEvent>(relaxed = true)
        every { event.originalServer.serverInfo.name } returns "test"
        every { event.player } returns player

        val server = mockk<Server>()
        every { server.isRunning() } returns true
        every { server.awaitReady() } returns Result.success(Unit)

        val serverManager = mockk<ServerManager>()
        every { serverManager.getServer(any()) } returns mockk(relaxed = true)
        every { serverManager.getServer("test") } returns server

        ServiceRegistry.instance.serverManager = serverManager

        // New connection
        every { event.previousServer } returns null
        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 1) { serverManager.getServer("test") }
        verify(exactly = 0) { server.startServer() }
        verify(exactly = 0) { player.disconnect(any<Component>()) }

        // Transferring player
        every { event.previousServer } returns mockk(relaxed = true)
        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 2) { serverManager.getServer("test") }
        verify(exactly = 0) { server.startServer() }
        verify(exactly = 0) { player.disconnect(any<Component>()) }
    }

    @Test
    fun testFailedConnectionToRunningServer() {
        val player = mockk<Player>(relaxed = true)

        val event = mockk<ServerPreConnectEvent>(relaxed = true)
        every { event.originalServer.serverInfo.name } returns "test"
        every { event.player } returns player

        val server = mockk<Server>()
        every { server.isRunning() } returns true
        every { server.awaitReady() } returns Result.failure(RuntimeException("Test"))

        val serverManager = mockk<ServerManager>()
        every { serverManager.getServer(any()) } returns mockk(relaxed = true)
        every { serverManager.getServer("test") } returns server

        ServiceRegistry.instance.serverManager = serverManager

        // New connection
        every { event.previousServer } returns null
        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 0) { server.startServer() }
        verify(exactly = 1) { player.disconnect(any<Component>()) }
        verify(exactly = 0) { player.sendMessage(any<Component>()) }
        verify { event.result = ServerResult.denied() }

        // Transferring player
        every { event.previousServer } returns mockk(relaxed = true)
        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 0) { server.startServer() }
        verify(exactly = 1) { player.disconnect(any<Component>()) }
        verify(exactly = 1) { player.sendMessage(any<Component>()) }
        verify { event.result = ServerResult.denied() }
    }

    @Test
    fun testConnectToNonAutoStartServer() {
        val player = mockk<Player>(relaxed = true)

        val event = mockk<ServerPreConnectEvent>(relaxed = true)
        every { event.originalServer.serverInfo.name } returns "test"
        every { event.player } returns player

        val server = mockk<Server>()
        every { server.isRunning() } returns false
        every { server.config.lifecycleSettings.allowAutoStart } returns false
        every { server.awaitReady() } returns Result.failure(RuntimeException("Test"))

        val serverManager = mockk<ServerManager>()
        every { serverManager.getServer(any()) } returns mockk(relaxed = true)
        every { serverManager.getServer("test") } returns server

        ServiceRegistry.instance.serverManager = serverManager

        // New connection
        every { event.previousServer } returns null
        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 0) { server.startServer() }
        verify(exactly = 0) { player.sendMessage(any()) }
        verify(exactly = 1) { player.disconnect(any()) }
        verify { event.result = ServerResult.denied() }

        // Transferring player
        every { event.previousServer } returns mockk(relaxed = true)
        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 0) { server.startServer() }
        verify(exactly = 1) { player.sendMessage(any()) }
        verify(exactly = 1) { player.disconnect(any()) }
        verify { event.result = ServerResult.denied() }
    }

    @Test
    fun testUnknownConnectionError() {
        val player = mockk<Player>(relaxed = true)

        val event = mockk<ServerPreConnectEvent>(relaxed = true)
        every { event.previousServer } returns null
        every { event.originalServer.serverInfo.name } returns "test"
        every { event.player } returns player

        val server = mockk<Server>()
        every { server.isRunning() } returns false
        // Do this to force an unknown error
        every { server.config.lifecycleSettings.allowAutoStart } returnsMany listOf(false, true)
        every { server.awaitReady() } returns Result.failure(RuntimeException("Test"))

        val serverManager = mockk<ServerManager>()
        every { serverManager.getServer("test") } returns server

        ServiceRegistry.instance.serverManager = serverManager

        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 0) { server.startServer() }
        verify(exactly = 1) { player.disconnect(any()) }
        verify { event.result = ServerResult.denied() }
    }

    @Test
    fun testSuccessfulConnectionToStoppedServer() {
        val player = mockk<Player>(relaxed = true)

        val event = mockk<ServerPreConnectEvent>(relaxed = true)
        every { event.originalServer.serverInfo.name } returns "test"
        every { event.player } returns player

        val server = mockk<Server>()
        every { server.isRunning() } returns false
        every { server.config.lifecycleSettings.allowAutoStart } returns true
        every { server.startServer() } returns Result.success(server)
        every { server.awaitReady() } returns Result.success(Unit)

        val serverManager = mockk<ServerManager>()
        every { serverManager.getServer(any()) } returns mockk(relaxed = true)
        every { serverManager.getServer("test") } returns server

        ServiceRegistry.instance.serverManager = serverManager

        // New connection
        every { event.previousServer } returns null
        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 1) { server.startServer() }
        verify(exactly = 0) { player.disconnect(any()) }

        // Transferring connection
        every { event.previousServer } returns mockk(relaxed = true)
        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 2) { server.startServer() }
        verify(exactly = 0) { player.disconnect(any()) }
    }

    @Test
    fun testFailedConnectionToStoppedServer() {
        val player = mockk<Player>(relaxed = true)

        val event = mockk<ServerPreConnectEvent>(relaxed = true)
        every { event.originalServer.serverInfo.name } returns "test"
        every { event.player } returns player

        val server = mockk<Server>()
        every { server.isRunning() } returns false
        every { server.config.lifecycleSettings.allowAutoStart } returns true
        every { server.startServer() } returns Result.failure(RuntimeException("Test"))

        val serverManager = mockk<ServerManager>()
        every { serverManager.getServer(any()) } returns mockk(relaxed = true)
        every { serverManager.getServer("test") } returns server

        ServiceRegistry.instance.serverManager = serverManager

        // New connection
        every { event.previousServer } returns null
        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 1) { server.startServer() }
        verify(exactly = 0) { server.awaitReady() }
        verify(exactly = 1) { player.disconnect(any()) }
        verify(exactly = 0) { player.sendMessage(any()) }
        verify { event.result = ServerResult.denied() }

        // Transferring connection
        every { event.previousServer } returns mockk(relaxed = true)
        playerLifecycleListener.handlePlayerConnectEvent(event)
        verify(exactly = 2) { server.startServer() }
        verify(exactly = 0) { server.awaitReady() }
        verify(exactly = 1) { player.disconnect(any()) }
        verify(exactly = 1) { player.sendMessage(any()) }
        verify { event.result = ServerResult.denied() }
    }
}