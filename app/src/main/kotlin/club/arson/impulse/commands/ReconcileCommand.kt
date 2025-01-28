package club.arson.impulse.commands

import club.arson.impulse.ServiceRegistry
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component

fun createReconcileCommand(): LiteralArgumentBuilder<CommandSource> {
    return createGenericServerCommand("reconcile") { context, serverName ->
        context.source.sendMessage(
            Component.text("Reconciling $serverName")
        )
        ServiceRegistry.instance.configManager?.servers?.find { it.name == serverName }?.let { server ->
            val res = ServiceRegistry.instance.serverManager?.getServer(serverName)?.reconcile(server)
            if (res?.isSuccess == true) {
                context.source.sendMessage(
                    Component.text("Server $serverName reconciled successfully")
                )
            } else {
                context.source.sendMessage(
                    Component.text("Error: failed to reconcile server $serverName")
                )
            }
        }
        return@createGenericServerCommand Command.SINGLE_SUCCESS
    }
}