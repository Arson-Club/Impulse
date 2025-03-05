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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "club.arson"

plugins {
    conventions.`impulse-base`
    conventions.`impulse-publish`
    conventions.`shadow-jar`
}

dependencies {
    sequenceOf(
        "api",
        "app",
        "docker-broker",
        "command-broker",
    ).forEach {
        dokka(project(":$it:"))
    }
}

val combinedDistributionProjects = listOf(
    Pair("api", "jar"),
    Pair("app", "shadowJar"),
    Pair("docker-broker", "shadowJar"),
    Pair("command-broker", "jar"),
)

tasks.withType<ShadowJar>().configureEach {
    archiveBaseName = "impulse"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    combinedDistributionProjects.forEach { (projectName, taskName) ->
        dependsOn(":$projectName:$taskName")
        from(provider { project(":$projectName").tasks.named(taskName).get().outputs.files })
    }
}

impulsePublish {
    artifact = tasks.named("shadowJar").get()
    groupId = "club.arson"
    description = "Impulse Server Manager for Velocity. Full distribution (all default brokers)."
    licenses = listOf(
        impulseLicense,
        kamlLicense,
        classGraphLicense,
        dockerLicense
    )
}