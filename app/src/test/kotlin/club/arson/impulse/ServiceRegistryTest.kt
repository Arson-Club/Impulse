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

import club.arson.impulse.config.ConfigManager
import club.arson.impulse.server.ServerBroker
import club.arson.impulse.server.ServerManager
import com.google.inject.Injector
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ServiceRegistryTest {
    private lateinit var serverManager: ServerManager
    private lateinit var configManager: ConfigManager
    private lateinit var brokerInjector: Injector

    @BeforeEach
    fun setUp() {
        ServiceRegistry.instance.reset()
        serverManager = mockk()
        configManager = mockk()
        brokerInjector = mockk()
    }

    @Test
    fun testNullOnInit() {
        val registry = ServiceRegistry.instance
        assert(registry.serverManager == null)
        assert(registry.configManager == null)
        assert(registry.injector == null)
    }

    @Test
    fun testPreventDoubleRegistration() {
        val registry = ServiceRegistry.instance
        registry.serverManager = serverManager
        assertThrows<IllegalStateException> {
            registry.serverManager = serverManager
        }
        registry.configManager = configManager
        assertThrows<IllegalStateException> {
            registry.configManager = configManager
        }
        registry.injector = brokerInjector
        assertThrows<IllegalStateException> {
            registry.injector = brokerInjector
        }
    }

    @Test
    fun testGetServerBroker() {
        val registry = ServiceRegistry.instance
        assertThrows<IllegalStateException> {
            registry.getServerBroker()
        }

        every { brokerInjector.getInstance(ServerBroker::class.java) } returns mockk()
        registry.injector = brokerInjector
        registry.getServerBroker()
        verify(exactly = 1) { brokerInjector.getInstance(ServerBroker::class.java) }
    }
}