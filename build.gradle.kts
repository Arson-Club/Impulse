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
}

//subprojects {
//    publishing {
//        publications {
//            create<MavenPublication>("shadowJarPublication") {
//                artifact(tasks.named("shadowJar").get())
//
//                groupId = "club.arson.impulse"
//                artifactId = project.name
//                version = project.version.toString()
//
//                pom {
//                    name = project.name
//                    url = "https://github.com/Arson-Club/Impulse"
//                    licenses {
//                        license {
//                            name = "GNU Affero General Public License"
//                            url = "https://www.gnu.org/licenses/"
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
//            }
//        }
//        repositories {
//            maven {
//                name = "GitHubPackages"
//                url = uri("https://maven.pkg.github.com/Arson-Club/Impulse")
//                credentials {
//                    username = System.getenv("GITHUB_ACTOR")
//                    password = System.getenv("GITHUB_TOKEN")
//                }
//            }
//        }
//    }
//}


//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            artifact(tasks.named<Jar>("combinedDistributionShadowJar").get())
//            groupId = "club.arson"
//            artifactId = project.name
//            version = project.version.toString()
//
//            pom {
//                name = project.name
//                description = "Impulse Server Manager for Velocity"
//                url = "https://github.com/Arson-Club/Impulse"
//                licenses {
//                    license {
//                        name = "GNU Affero General Public License"
//                        url = "https://www.gnu.org/licenses/"
//                    }
//                }
//                developers {
//                    developer {
//                        id = "dabb1e"
//                        name = "Dabb1e"
//                        email = "dabb1e@arson.club"
//                    }
//                }
//            }
//        }
//    }
//    repositories {
//        maven {
//            name = "GitHubPackages"
//            url = uri("https://maven.pkg.github.com/Arson-Club/Impulse")
//            credentials {
//                username = System.getenv("GITHUB_ACTOR")
//                password = System.getenv("GITHUB_TOKEN")
//            }
//        }
//    }
//}
