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

package club.arson.impulse.server.broker

import club.arson.impulse.api.server.BrokerFactory
import com.google.inject.Provider
import io.github.classgraph.ClassGraph
import org.slf4j.Logger

/**
 * Provider for all BrokerFactory implementations found on the classpath
 *
 * @property logger the logger to use for messages
 */
class BrokerProvider(private val logger: Logger? = null) :
    Provider<Set<BrokerFactory>> {
    /**
     * Scans the classpath for all implementations of [BrokerFactory]
     *
     * @return a set of all found implementations
     */
    override fun get(): Set<BrokerFactory> {
        val implementations = mutableSetOf<BrokerFactory>()
        ClassGraph()
            .enableClassInfo()
            .acceptPackages()
            .scan()
            .use { scanResult ->
                scanResult
                    .allClasses
                    .filter { c -> c.implementsInterface(BrokerFactory::class.java.name) }
                    .forEach { classInfo ->
                        runCatching {
                            val c = classInfo.loadClass(BrokerFactory::class.java)
                            implementations.add(c.getDeclaredConstructor().newInstance())
                        }
                            .onSuccess { logger?.info("BrokerProvider: Found Broker ${classInfo.name} on path") }
                            .onFailure { logger?.warn("Found Broker ${classInfo.name} on path, but failed in instantiate: ${it.message}") }
                    }
            }
        return implementations
    }
}