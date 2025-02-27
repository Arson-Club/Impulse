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

import club.arson.impulse.api.config.Configuration
import club.arson.impulse.api.events.ConfigReloadEvent
import club.arson.impulse.config.ConfigManager
import club.arson.impulse.inject.modules.BaseModule
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import com.velocitypowered.api.proxy.ProxyServer
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.test.assertEquals

class ConfigManagerTestModule(val reloadOnInit: Boolean) : AbstractModule() {
    override fun configure() {
        bind(Boolean::class.java).toInstance(reloadOnInit)
    }
}

class ConfigManagerTest {
    private fun getConfigManager(
        proxy: ProxyServer = mockk(relaxed = true),
        plugin: Impulse = mockk(relaxed = true),
        configPath: Path = mockk(relaxed = true),
        reloadOnInit: Boolean = false,
        logger: Logger = mockk(relaxed = true)
    ): ConfigManager {
        val injector =
            Guice.createInjector(BaseModule(plugin, proxy, configPath, logger), ConfigManagerTestModule(reloadOnInit))
        return injector.getInstance(ConfigManager::class.java)
    }

    @Test
    fun testConfigManagerCreateDirectorySuccess() {
        val configPath = mockk<Path>(relaxed = true).apply {
            every { toFile().exists() } returns false
        }
        assertDoesNotThrow { getConfigManager(configPath = configPath) }
        verify(exactly = 1) { configPath.toFile().exists() }
        verify(exactly = 1) { configPath.createDirectories() }
    }

    @Test
    fun testConfigManagerCreateConfigFileSuccess() {
        mockkStatic(File::writeText)

        val configToFile = mockk<File>(relaxed = true).apply {
            every { exists() } returns false
            every { writeText(any()) } returns Unit
        }
        val configFile = mockk<Path>(relaxed = true).apply {
            every { toFile() } returns configToFile
        }
        val configPath = mockk<Path>(relaxed = true).apply {
            every { toFile().exists() } returns true
            every { resolve(any<String>()) } returns configFile
        }

        assertDoesNotThrow { getConfigManager(configPath = configPath) }
        verify(exactly = 1) { configPath.toFile().exists() }
        verify(exactly = 0) { configPath.createDirectories() }
        verify(exactly = 1) { configToFile.writeText(any()) }
    }

    @Test
    fun testConfigManagerCreateConfigFileFailure() {
        mockkStatic(File::writeText)

        val configToFile = mockk<File>(relaxed = true).apply {
            every { exists() } returns false
            every { writeText(any()) } throws IOException("Test")
        }
        val configFile = mockk<Path>(relaxed = true).apply {
            every { toFile() } returns configToFile
        }
        val configPath = mockk<Path>(relaxed = true).apply {
            every { toFile().exists() } returns true
            every { resolve(any<String>()) } returns configFile
        }

        assertDoesNotThrow { getConfigManager(configPath = configPath) }
        verify(exactly = 1) { configPath.toFile().exists() }
        verify(exactly = 0) { configPath.createDirectories() }
        verify(exactly = 1) { configToFile.writeText(any()) }
    }

    @Test
    fun testConfigManagerWatcherFails() {
        val configPath = mockk<Path>(relaxed = true).apply {
            every { register(any(), *anyVararg()) } throws Exception("Test")
        }
        val logger = mockk<Logger>(relaxed = true)
        assertDoesNotThrow { getConfigManager(configPath = configPath, logger = logger) }
        verify { logger.error(any()) }
    }

    @Test
    fun testConfigManagerEmptyConfig() {
        val config = ""
        val configFile = mockk<Path>(relaxed = true).apply {
            every { inputStream() } returns config.byteInputStream()
        }
        val configPath = mockk<Path>(relaxed = true).apply {
            every { resolve(any<String>()) } returns configFile
        }
        val generatedEvent = slot<ConfigReloadEvent>()
        val proxy = mockk<ProxyServer>(relaxed = true).apply {
            every { eventManager } returns mockk<EventManager>(relaxed = true).apply {
                every { fire(capture(generatedEvent)) } answers { CompletableFuture.completedFuture(generatedEvent.captured) }
            }
        }
        assertDoesNotThrow { getConfigManager(proxy = proxy, configPath = configPath, reloadOnInit = true) }
        assertEquals(GenericResult.allowed(), generatedEvent.captured.result)
    }

    @Test
    fun testConfigManagerMissingConfig() {
        val configFile = mockk<Path>(relaxed = true).apply {
            every { inputStream() } throws IOException("Test")
        }
        val configPath = mockk<Path>(relaxed = true).apply {
            every { resolve(any<String>()) } returns configFile
        }
        val generatedEvent = slot<ConfigReloadEvent>()
        val proxy = mockk<ProxyServer>(relaxed = true).apply {
            every { eventManager } returns mockk<EventManager>(relaxed = true).apply {
                every { fire(capture(generatedEvent)) } answers { CompletableFuture.completedFuture(generatedEvent.captured) }
            }
        }
        assertDoesNotThrow { getConfigManager(proxy = proxy, configPath = configPath, reloadOnInit = true) }
        assertEquals(GenericResult.allowed(), generatedEvent.captured.result)
    }

    @Test
    fun testConfigManagerUnknownReadError() {
        val configFile = mockk<Path>(relaxed = true).apply {
            every { inputStream() } throws Exception("Test")
        }
        val configPath = mockk<Path>(relaxed = true).apply {
            every { resolve(any<String>()) } returns configFile
        }
        val generatedEvent = slot<ConfigReloadEvent>()
        val proxy = mockk<ProxyServer>(relaxed = true).apply {
            every { eventManager } returns mockk<EventManager>(relaxed = true).apply {
                every { fire(capture(generatedEvent)) } answers { CompletableFuture.completedFuture(generatedEvent.captured) }
            }
        }
        assertDoesNotThrow { getConfigManager(proxy = proxy, configPath = configPath, reloadOnInit = true) }
        assertEquals(GenericResult.denied(), generatedEvent.captured.result)
    }

    @Test
    fun testConfigManagerMinimalConfig() {
        val configYaml = """
            instanceName: Test
        """.trimIndent()
        val configFile = mockk<Path>(relaxed = true).apply {
            every { inputStream() } returns configYaml.byteInputStream()
        }
        val configPath = mockk<Path>(relaxed = true).apply {
            every { resolve(any<String>()) } returns configFile
        }
        val generatedEvent = slot<ConfigReloadEvent>()
        val proxy = mockk<ProxyServer>(relaxed = true).apply {
            every { eventManager } returns mockk<EventManager>(relaxed = true).apply {
                every { fire(capture(generatedEvent)) } answers { CompletableFuture.completedFuture(generatedEvent.captured) }
            }
        }
        val configManager = getConfigManager(proxy = proxy, configPath = configPath, reloadOnInit = true)
        assertEquals(GenericResult.allowed(), generatedEvent.captured.result)
        assertEquals("Test", configManager.instanceName)
    }

    @Test
    fun testConfigManagerMinimalConfigFailuer() {
        val configYaml = """
            badKey: Test
        """.trimIndent()
        val configFile = mockk<Path>(relaxed = true).apply {
            every { inputStream() } returns configYaml.byteInputStream()
        }
        val configPath = mockk<Path>(relaxed = true).apply {
            every { resolve(any<String>()) } returns configFile
        }
        val generatedEvent = slot<ConfigReloadEvent>()
        val proxy = mockk<ProxyServer>(relaxed = true).apply {
            every { eventManager } returns mockk<EventManager>(relaxed = true).apply {
                every { fire(capture(generatedEvent)) } answers { CompletableFuture.completedFuture(generatedEvent.captured) }
            }
        }
        val configManager = getConfigManager(proxy = proxy, configPath = configPath, reloadOnInit = true)
        assertEquals(GenericResult.denied(), generatedEvent.captured.result)
        assertEquals(Configuration().instanceName, configManager.instanceName) // assert default value
    }
}
