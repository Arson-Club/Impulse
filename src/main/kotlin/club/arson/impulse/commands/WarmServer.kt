package club.arson.impulse.commands

import club.arson.impulse.ServiceRegistry
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component

fun createWarmServerCommand(proxy: ProxyServer): BrigadierCommand {
    val serverSuggestionProvider = SuggestionProvider<CommandSource> { context, builder ->
        val serverNames = ServiceRegistry.instance.configManager?.servers?.map { it.name } ?: emptyList()
        serverNames.forEach { serverName ->
            builder.suggest(serverName)
        }
        builder.buildFuture()
    }

    val warmNode = BrigadierCommand.literalArgumentBuilder("warm-server")
        .then(BrigadierCommand
            .requiredArgumentBuilder("server", StringArgumentType.word())
            .suggests(serverSuggestionProvider)
            .executes({ context ->
                val serverName = context.getArgument("server", String::class.java)
                context.source.sendMessage(
                    Component.text("Warming $serverName")
                )
                val startResult = ServiceRegistry.instance.serverManager?.getServer(serverName)?.startServer()
                if (startResult == null || startResult.isFailure) {
                    context.source.sendMessage(
                        Component.text("Error: failed to start server $serverName")
                    )
                } else {
                    startResult.getOrNull()?.awaitReady()
                    context.source.sendMessage(
                        Component.text("Server $serverName started successfully")
                    )
                }
                return@executes Command.SINGLE_SUCCESS
            })
        )
    return BrigadierCommand(warmNode)
}