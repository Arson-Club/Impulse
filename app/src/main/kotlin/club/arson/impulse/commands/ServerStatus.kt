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
import club.arson.impulse.api.server.Status
import club.arson.impulse.server.Server
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

fun getServerStatusMessage(name: String, status: Status, padTo: Int = 0): Component {
    return Component.text()
        .content("$name: ".padEnd(padTo + 2))
        .append(
            Component.text(status.toString())
                .color(
                    when (status) {
                        Status.RUNNING -> NamedTextColor.GREEN
                        Status.STOPPED -> NamedTextColor.GRAY
                        Status.REMOVED -> NamedTextColor.YELLOW
                        Status.UNKNOWN -> NamedTextColor.RED
                    }
                )
        )
        .build()
}

fun serverStatusTable(servers: List<Server>): Component {
    val table = Component.text()
        .content("Server Status")
    var maxLength = 0;
    if (servers.isNotEmpty()) {
        maxLength = servers.maxOf { it.config.name.length }
    }

    servers.forEach { server ->
        table.appendNewline().append(getServerStatusMessage(server.config.name, server.getStatus(), maxLength))
    }

    return table.build()
}

fun createServerStatusCommand(): LiteralArgumentBuilder<CommandSource> {
    return BrigadierCommand.literalArgumentBuilder("status")
        .requires { source -> source.hasPermission("impulse.server.status") }
        .executes { context ->
            val table =
                serverStatusTable(ServiceRegistry.instance.serverManager?.servers?.values?.toList() ?: emptyList())
            context.source.sendMessage(table)
            return@executes Command.SINGLE_SUCCESS
        }
        .then(
            BrigadierCommand
                .requiredArgumentBuilder("server", StringArgumentType.word())
                .suggests(serverSuggestionProvider())
                .executes { context ->
                    val serverName = context.getArgument("server", String::class.java)
                    val server = ServiceRegistry.instance.serverManager?.getServer(serverName)
                    if (server != null) {
                        val table = serverStatusTable(listOf(server))
                        context.source.sendMessage(table)
                    } else {
                        val message = Component
                            .text()
                            .content("Server not found $serverName")
                            .build()
                        context.source.sendMessage(message)
                    }
                    return@executes Command.SINGLE_SUCCESS
                })
}
