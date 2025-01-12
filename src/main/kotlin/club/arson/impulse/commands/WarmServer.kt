package club.arson.impulse.commands

import club.arson.impulse.ServiceRegistry
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component

fun createWarmServerCommand(): LiteralArgumentBuilder<CommandSource> {
    return createGenericServerCommand("warm", { context, serverName ->
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
        return@createGenericServerCommand Command.SINGLE_SUCCESS
    })
}