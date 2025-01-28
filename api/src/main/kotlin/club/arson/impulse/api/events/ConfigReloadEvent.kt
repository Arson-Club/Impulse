/*
 * Impulse Server Manager for Velocity
 * Copyright (c) 2025  Dabb1e
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package club.arson.impulse.api.events

import club.arson.impulse.api.config.Configuration
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult

/**
 * Event triggered before the Impulse configuration is reloaded.
 *
 * When impulse detects a change in the configuration file, this event is fired. After resolution, the config manager
 * will update its internal state to match the value of "config". You can set the result to "denied" to prevent downstream
 * listeners from updating their configuration.
 *
 * @property config the new configuration
 * @property oldConfig the current live configuration
 * @property result the result of the event
 * @constructor create a new config reload event from and old and new configuration
 */
class ConfigReloadEvent(
    var config: Configuration,
    var oldConfig: Configuration,
    @JvmField var result: GenericResult = GenericResult.allowed()
) : ResultedEvent<GenericResult> {
    /**
     * Get the current event result
     *
     * @return the current event result
     */
    override fun getResult(): GenericResult {
        return result
    }

    /**
     * Set the event result
     *
     * @param result the new event result
     */
    override fun setResult(result: GenericResult) {
        this.result = result
    }
}