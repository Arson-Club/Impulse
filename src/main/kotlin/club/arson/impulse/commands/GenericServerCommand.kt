package club.arson.impulse.commands

import club.arson.impulse.ServiceRegistry
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource

fun createGenericServerCommand(name: String, task: (CommandContext<CommandSource>, String) -> Int): LiteralArgumentBuilder<CommandSource> {
    val serverSuggestionProvider = SuggestionProvider<CommandSource> { context, builder ->
        val serverNames = ServiceRegistry.instance.configManager?.servers?.map { it.name } ?: emptyList()
        serverNames.forEach { serverName ->
            builder.suggest(serverName)
        }
        builder.buildFuture()
    }

    return BrigadierCommand.literalArgumentBuilder(name)
        .requires({ source -> source.hasPermission("impulse.server.$name")})
        .then(BrigadierCommand
            .requiredArgumentBuilder("server", StringArgumentType.word())
            .suggests(serverSuggestionProvider)
            .executes({ context ->
                val serverName = context.getArgument("server", String::class.java)
                task(context, serverName)
            })
        )
}

