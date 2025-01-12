package club.arson.impulse.commands

import club.arson.impulse.ServiceRegistry
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component

fun createStopServerCommand(): LiteralArgumentBuilder<CommandSource> {
    return createGenericServerCommand("stop", { context, serverName ->
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
    })
}
