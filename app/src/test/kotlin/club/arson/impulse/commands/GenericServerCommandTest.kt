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

package club.arson.impulse.commands

import club.arson.impulse.ServiceRegistry
import club.arson.impulse.api.config.ServerConfig
import club.arson.impulse.config.ConfigManager
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CommandTests {
    @BeforeEach
    fun setUp() {
        ServiceRegistry.instance.reset()
    }

    @Test
    fun testServerSuggestionsProvider() {
        val mockConfigManager = mockk<ConfigManager>()
        val mockServer = mockk<ServerConfig>()

        every { mockServer.name } returns "Server1"
        every { mockConfigManager.servers } returns listOf(mockServer)
        ServiceRegistry.instance.configManager = mockConfigManager

        val suggestionProvider = serverSuggestionProvider()
        val mockBuilder = mockk<SuggestionsBuilder>(relaxed = true)

        suggestionProvider.getSuggestions(mockk(), mockBuilder)
        verify { mockBuilder.suggest("Server1") }
    }

    @Test
    fun testServerSuggestionsProviderEmpty() {
        val mockConfigManager = mockk<ConfigManager>()
        every { mockConfigManager.servers } returns emptyList()
        ServiceRegistry.instance.configManager = mockConfigManager

        val suggestionProvider = serverSuggestionProvider()
        val mockBuilder = mockk<SuggestionsBuilder>(relaxed = true)

        suggestionProvider.getSuggestions(mockk(), mockBuilder)
        verify(exactly = 0) { mockBuilder.suggest(any<String>()) }
        verify(exactly = 0) { mockBuilder.suggest(any<Int>()) }
    }

    @Test
    fun testServerSuggestionsProviderNoConfigManager() {
        ServiceRegistry.instance.configManager = null
        val suggestionProvider = serverSuggestionProvider()
        val mockBuilder = mockk<SuggestionsBuilder>(relaxed = true)

        suggestionProvider.getSuggestions(mockk(), mockBuilder)
        verify(exactly = 0) { mockBuilder.suggest(any<String>()) }
        verify(exactly = 0) { mockBuilder.suggest(any<Int>()) }
    }
}
