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

group = "club.arson"

plugins {
    conventions.`impulse-base`
    conventions.`impulse-publish`
    conventions.jar
}

dependencies {
    sequenceOf(
        "api",
        "app",
        "docker-broker",
        "command-broker",
        "oraclecloud-broker",
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

tasks.withType<Jar>().configureEach {
    archiveBaseName = "impulse"
    description = "Impulse Server Manager for Velocity. Full distribution (all default brokers)."
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(combinedDistributionProjects.map { (projectName, target) -> ":${projectName}:${target}" })

    from(combinedDistributionProjects.map { (projectName, target) ->
        provider { project(projectName).tasks.named(target).map { (it as Jar).archiveFile.get().asFile } }
    }.map { zipTree(it) })
}

impulsePublish {
    artifact = tasks.named("jar").get()
    groupId = "club.arson"
    description = "Impulse Server Manager for Velocity. Full distribution (all default brokers)."
    licenses = listOf(
        impulseLicense,
        kamlLicense,
        classGraphLicense,
        dockerLicense
    )
}