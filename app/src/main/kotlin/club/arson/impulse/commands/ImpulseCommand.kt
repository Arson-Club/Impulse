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

import com.velocitypowered.api.command.BrigadierCommand

fun createImpulseCommand(): BrigadierCommand {
    val startNode = createStartServerCommand()
    val commandNode = BrigadierCommand.literalArgumentBuilder("impulse")
        .then(startNode)
        .then(BrigadierCommand.literalArgumentBuilder("warm").redirect(startNode.build()))
        .then(createStopServerCommand())
        .then(createRemoveServerCommand())
        .then(createReconcileCommand())
        .then(createServerStatusCommand())
    return BrigadierCommand(commandNode)
}