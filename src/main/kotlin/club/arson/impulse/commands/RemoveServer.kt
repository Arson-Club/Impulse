package club.arson.impulse.commands

import club.arson.impulse.ServiceRegistry
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component

fun createRemoveServerCommand(): LiteralArgumentBuilder<CommandSource> {
    return createGenericServerCommand("remove", { context, serverName ->
        context.source.sendMessage(
            Component.text("Removing $serverName")
        )
        val removeResult = ServiceRegistry.instance.serverManager?.getServer(serverName)?.removeServer()
        if (removeResult == null || removeResult.isFailure) {
            context.source.sendMessage(
                Component.text("Error: failed to remove server $serverName")
            )
        } else {
            context.source.sendMessage(
                Component.text("Server $serverName removed successfully")
            )
        }
        return@createGenericServerCommand Command.SINGLE_SUCCESS
    })
}