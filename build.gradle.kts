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
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization") version "2.1.20-Beta1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `dokka-convention`
    `maven-publish`
}

dependencies {
    sequenceOf(
        "api",
        "app",
        "docker-broker",
    ).forEach {
        dokka(project(":$it:"))
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "maven-publish")

    dependencies {
        compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
        kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

        implementation("com.charleskorn.kaml:kaml:0.67.0")
        testImplementation("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    }

    val targetJavaVersion = 17
    kotlin {
        jvmToolchain(targetJavaVersion)
    }

    publishing {
        publications {
            create<MavenPublication>("shadowJarPublication") {
                artifact(tasks.named("shadowJar").get())

                groupId = "club.arson.impulse"
                artifactId = project.name
                version = project.version.toString()

                pom {
                    name = project.name
                    url = "https://github.com/Arson-Club/Impulse"
                    licenses {
                        license {
                            name = "GNU Affero General Public License"
                            url = "https://www.gnu.org/licenses/"
                        }
                    }
                    developers {
                        developer {
                            id = "dabb1e"
                            name = "Dabb1e"
                            email = "dabb1e@arson.club"
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/Arson-Club/Impulse")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}

val combinedDistributionProjects = listOf(
    "app",
    "docker-broker",
)

tasks.register<Jar>("combinedDistributionShadowJar") {
    group = "build"
    description = "Builds the release jar combining the base app and core brokers"
    archiveBaseName.set("impulse")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    dependsOn(combinedDistributionProjects.map { ":${it}:shadowJar" })
    from(combinedDistributionProjects.map { p ->
        project(p).tasks.named("shadowJar").map { (it as Jar).archiveFile.get().asFile }
    }.map { zipTree(it) })
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.named<Jar>("combinedDistributionShadowJar").get())
            groupId = "club.arson"
            artifactId = project.name
            version = project.version.toString()

            pom {
                name = project.name
                description = "Impulse Server Manager for Velocity"
                url = "https://github.com/Arson-Club/Impulse"
                licenses {
                    license {
                        name = "GNU Affero General Public License"
                        url = "https://www.gnu.org/licenses/"
                    }
                }
                developers {
                    developer {
                        id = "dabb1e"
                        name = "Dabb1e"
                        email = "dabb1e@arson.club"
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Arson-Club/Impulse")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
