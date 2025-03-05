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
//    conventions.`impulse-publish`
    conventions.`shadow-jar`

    `maven-publish`
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
    dependsOn(combinedDistributionProjects.map { ":${it.first}:${it.second}" })
    from(combinedDistributionProjects.map { p ->
        project(p.first).tasks.named(p.second).map { (it as Jar).archiveFile.get().asFile }
    }.map { zipTree(it) })
    dependsOn(":api:jar")
}

//impulsePublish {
//    artifact = tasks.named("shadowJar").get().mustRunAfter(":api")
//    publicationName = "combinedImpulse"
//    description = "Impulse Server Manager for Velocity. Full distribution (all default brokers)."
//    licenses = listOf(
//        impulseLicense,
//        kamlLicense,
//        classGraphLicense,
//        dockerLicense
//    )
//}
publishing {
    publications {
        create<MavenPublication>("${project.name}-impulse") {
            //artifact(project.tasks.named("shadowJar").get())
            //artifact(project.tasks.named(extension.artifactType).get())


            groupId = "club.arson"
            artifactId = project.name
            version = project.version.toString()

            pom {
//                name = project.name
//                description = extension.description
//                url = "https://github.com/ArsonClub/Impulse"
//                licenses {
//                    extension.licenses.forEach {
//                        license {
//                            name = it.name
//                            url = it.url
//                            comments = it.comments
//                        }
//                    }
//                    developers {
//                        developer {
//                            id = "dabb1e"
//                            name = "Dabb1e"
//                            email = "dabb1e@arson.club"
//                        }
//                    }
//                }
            }
        }
        repositories {
//            extension.repositories.forEach {
//                maven {
//                    name = it.name
//                    url = uri(it.url)
//                    credentials {
//                        username = it.username
//                        password = it.password
//                    }
//                }
//            }
        }
    }
}
