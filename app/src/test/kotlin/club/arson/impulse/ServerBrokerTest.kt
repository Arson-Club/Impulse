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
import club.arson.impulse.inject.TestModule
import club.arson.impulse.server.ServerBroker
import com.google.inject.Guice
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ServerBrokerTest {
    private lateinit var serverBroker: ServerBroker

    @BeforeEach
    fun setUp() {
        val injector = Guice.createInjector(TestModule())
        serverBroker = injector.getInstance(ServerBroker::class.java)
    }

    @Test
    fun testCreateFromConfig() {
        val serverConfig = mockk<ServerConfig>()
        every { serverConfig.type } returnsMany listOf("test", "invalid")

        // We should find something
        var result = serverBroker.createFromConfig(serverConfig)
        assert(result.isSuccess)

        // No broker should be found
        result = serverBroker.createFromConfig(serverConfig)
        assert(result.isFailure)
    }
}