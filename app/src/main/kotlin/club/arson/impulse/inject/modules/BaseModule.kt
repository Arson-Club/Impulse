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

package club.arson.impulse.inject.modules

import club.arson.impulse.Impulse
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import java.nio.file.Path
import javax.inject.Qualifier

@Qualifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PluginDir

class BaseModule(
    val plugin: Impulse,
    val proxyServer: ProxyServer,
    val pluginDirectory: Path,
    val logger: Logger? = null
) : AbstractModule() {
    @Provides
    fun providePlugin(): Impulse {
        return plugin
    }

    @Provides
    fun provideProxyServer(): ProxyServer {
        return proxyServer
    }

    @Provides
    fun provideLogger(): Logger? {
        return logger
    }

    @Provides
    @PluginDir
    fun providesPluginDirectory(): Path {
        return pluginDirectory
    }
}