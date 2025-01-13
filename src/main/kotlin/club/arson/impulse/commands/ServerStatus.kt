package club.arson.impulse.commands

import club.arson.impulse.ServiceRegistry
import club.arson.impulse.server.ServerStatus
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

fun getServerStatusMessage(name: String, status: ServerStatus, padTo: Int = 0): Component {
    return Component.text()
        .content("$name: ".padEnd(padTo + 2))
        .append(
            Component.text(status.toString())
                .color(when (status) {
                    ServerStatus.RUNNING -> NamedTextColor.GREEN
                    ServerStatus.STOPPED -> NamedTextColor.GRAY
                    ServerStatus.REMOVED -> NamedTextColor.YELLOW
                    ServerStatus.UNKNOWN -> NamedTextColor.RED
                })
        )
        .build()
}

fun foo(context: CommandContext<CommandSource>): Int {
    val servers = ServiceRegistry.instance.serverManager?.servers?.values ?: emptyList()
    var message = Component.text().content("Server Status")
    val maxLength = (servers.maxOf { it.config.name.length })
    for (server in servers) {
        val m = getServerStatusMessage(server.config.name, server.getStatus(), maxLength)
        message = message.appendNewline().append(m)
    }
    context.source.sendMessage(message.build())
    return Command.SINGLE_SUCCESS
}

fun createServerStatusCommand(): LiteralArgumentBuilder<CommandSource> {
    return BrigadierCommand.literalArgumentBuilder("status")
        .requires({ source -> source.hasPermission("impulse.server.status")})
        .executes({ context ->
            return@executes foo(context)
        })
        .then(BrigadierCommand
            .requiredArgumentBuilder("server", StringArgumentType.word())
            .suggests(serverSuggestionProvider())
            .executes({ context ->
                val serverName = context.getArgument("server", String::class.java)
                val status = ServiceRegistry.instance.serverManager?.getServer(serverName)?.getStatus() ?: ServerStatus.UNKNOWN
                val message = Component
                    .text()
                    .content("Server Status")
                    .appendNewline()
                    .append(getServerStatusMessage(serverName, status))
                    .build()
                context.source.sendMessage(message)
                return@executes Command.SINGLE_SUCCESS
            })
        )
}
