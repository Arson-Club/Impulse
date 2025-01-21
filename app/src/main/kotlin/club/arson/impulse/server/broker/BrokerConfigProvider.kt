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

import club.arson.impulse.api.config.BrokerConfig
import com.google.inject.Provider
import io.github.classgraph.ClassGraph
import org.slf4j.Logger
import kotlin.reflect.KClass

/**
 * Guice provider for Broker configuration classes
 *
 * Broker configuration classes do not have a set interface, so we scan for the [BrokerConfig] annotation. If found we
 * use the associated brokerId to register the class to a specific Broker type.
 * @property logger the logger to use for log messages
 */
class BrokerConfigProvider(private val logger: Logger? = null) :
    Provider<Map<String, KClass<out Any>>> {
    /**
     * Scans the classpath for classes annotated with [BrokerConfig] and returns a map of brokerId to class
     * @return a map of brokerId to class
     */
    override fun get(): Map<String, KClass<out Any>> {
        val implementations = mutableMapOf<String, KClass<out Any>>()
        ClassGraph()
            .enableAnnotationInfo()
            .enableClassInfo()
            .acceptPackages()
            .scan()
            .use { scanResult ->
                scanResult
                    .getClassesWithAnnotation(BrokerConfig::class.java.name)
                    .forEach { classInfo ->
                        var brokerId = ""
                        runCatching {
                            val clazz = classInfo.loadClass()
                            brokerId = clazz.getAnnotation(BrokerConfig::class.java).brokerId
                            implementations.put(brokerId, clazz.kotlin as KClass<out Any>)
                        }
                            .onSuccess { logger?.debug("BrokerConfigProver: registered config for $brokerId") }
                            .onFailure { logger?.warn("BrokerConfigProvider: Found config for broker $brokerId but unable to register it: ${it.message}") }
                    }
            }
        return implementations
    }
}