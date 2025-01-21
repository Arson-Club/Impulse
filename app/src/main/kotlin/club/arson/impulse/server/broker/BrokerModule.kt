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
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import org.slf4j.Logger
import kotlin.reflect.KClass

/**
 * Guice module for the broker system
 *
 * @property logger the logger to use for the broker system
 * @constructor creates a new broker module
 */
class BrokerModule(private val logger: Logger? = null) : AbstractModule() {
    /**
     * Binds the BrokerFactory and Broker Configuration providers for the injector
     */
    override fun configure() {
        bind(object : com.google.inject.TypeLiteral<Set<BrokerFactory>>() {})
            .toProvider(BrokerProvider(logger))
            .`in`(Scopes.SINGLETON)
        bind(object : com.google.inject.TypeLiteral<Map<String, KClass<out Any>>>() {})
            .toProvider(BrokerConfigProvider(logger))
            .`in`(Scopes.SINGLETON)
    }
}