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

package club.arson.impulse.api.events

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import java.nio.file.Path

/**
 * An event to fire to dynamically register your broker
 *
 * If a broker is being provided as a full Velocity plugin, this event should be fired to register the jar with Impulse
 * @property jarPath the path to the jar file
 * @property result the result of the event
 * @constructor create a new broker registration event
 */
class RegisterBrokerEvent(
    var jarPath: Path,
    @JvmField var result: GenericResult = GenericResult.allowed()
) : ResultedEvent<GenericResult> {
    override fun getResult(): GenericResult {
        return result
    }

    override fun setResult(result: GenericResult) {
        this.result = result
    }
}