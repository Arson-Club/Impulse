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
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource

fun serverSuggestionProvider(): SuggestionProvider<CommandSource> {
    return SuggestionProvider<CommandSource> { _, builder ->
        val serverNames = ServiceRegistry.instance.configManager?.servers?.map { it.name } ?: emptyList()
        serverNames.forEach { serverName ->
            builder.suggest(serverName)
        }
        builder.buildFuture()
    }
}


fun createGenericServerCommand(
    name: String,
    task: (CommandContext<CommandSource>, String) -> Int
): LiteralArgumentBuilder<CommandSource> {

    return BrigadierCommand.literalArgumentBuilder(name)
        .requires { source -> source.hasPermission("impulse.server.$name") }
        .then(
            BrigadierCommand
                .requiredArgumentBuilder("server", StringArgumentType.word())
                .suggests(serverSuggestionProvider())
                .executes { context ->
                    val serverName = context.getArgument("server", String::class.java)
                    task(context, serverName)
                }
        )
}