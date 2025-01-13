package club.arson.impulse.commands

import com.velocitypowered.api.command.BrigadierCommand

fun createImpulseCommand(): BrigadierCommand {
    val commandNode = BrigadierCommand.literalArgumentBuilder("impulse")
        .then(createWarmServerCommand())
        .then(createStopServerCommand())
        .then(createRemoveServerCommand())
        .then(createServerStatusCommand())
    return BrigadierCommand(commandNode)
}