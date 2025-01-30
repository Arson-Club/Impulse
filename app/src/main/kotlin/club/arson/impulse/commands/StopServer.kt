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
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component

fun createStopServerCommand(): LiteralArgumentBuilder<CommandSource> {
    return createGenericServerCommand("stop") { context, serverName ->
        context.source.sendMessage(
            Component.text("Stopping $serverName")
        )
        val stopResult = ServiceRegistry.instance.serverManager?.getServer(serverName)?.stopServer()
        if (stopResult == null || stopResult.isFailure) {
            context.source.sendMessage(
                Component.text("Error: failed to stop server $serverName")
            )
        } else {
            context.source.sendMessage(
                Component.text("Server $serverName stopped successfully")
            )
        }
        return@createGenericServerCommand Command.SINGLE_SUCCESS
    }
}
