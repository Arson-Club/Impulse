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

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"

    `dokka-convention`
    `java-library`
}
group = "club.arson.impulse"

dokka {
    dokkaSourceSets.configureEach {}
}

java {
    withSourcesJar()
}

tasks {
    shadowJar {
        manifest {}
        from(sourceSets.main.get().output)
        archiveClassifier.set("")
    }
}