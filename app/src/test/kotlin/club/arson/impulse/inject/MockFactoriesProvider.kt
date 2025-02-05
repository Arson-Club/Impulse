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

package club.arson.impulse.inject

import club.arson.impulse.api.server.BrokerFactory
import com.google.inject.Provider
import io.mockk.every
import io.mockk.mockk

class MockFactoriesProvider : Provider<Set<BrokerFactory>> {
    override fun get(): Set<BrokerFactory> {
        val mockFactory = mockk<BrokerFactory>()
        every { mockFactory.NAME } returns "test"
        every { mockFactory.createFromConfig(any(), any()) } returns Result.success(mockk())
        return setOf(mockFactory)
    }
}
