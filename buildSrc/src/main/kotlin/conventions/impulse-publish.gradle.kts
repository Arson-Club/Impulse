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

package conventions

plugins {
    `maven-publish`
}

data class LicenseInfo(
    val name: String,
    val url: String,
    val comments: String,
)

data class RepositoryInfo(
    val name: String,
    val url: String,
    val username: String,
    val password: String
)

open class ImpulsePublishExtension {
    // Project licenses
    val kamlLicense = LicenseInfo(
        "Apache License 2.0",
        "https://www.apache.org/licenses/LICENSE-2.0",
        "Kaml library license"
    )
    val impulseLicense = LicenseInfo(
        "GNU Affero General Public License",
        "https://www.gnu.org/licenses/",
        "License for all Impulse sources"
    )
    val classGraphLicense = LicenseInfo(
        "MIT License",
        "https://opensource.org/licenses/MIT",
        "ClassGraph library license"
    )
    val dockerLicense = LicenseInfo(
        "Apache License 2.0",
        "https://www.apache.org/licenses/LICENSE-2.0",
        "Docker library license"
    )

    // Project repositories
    val githubPackageRepo = RepositoryInfo(
        "GitHubPackages",
        "https://maven.pkg.github.com/Arson-Club/Impulse",
        System.getenv("GITHUB_ACTOR") ?: "",
        System.getenv("GITHUB_TOKEN") ?: ""
    )

    var artifact: Task? = null
    var groupId = "club.arson.impulse"
    var description = "Impulse Server Manager for Velocity"
    var licenses = listOf(impulseLicense, kamlLicense)
    var repositories = listOf(githubPackageRepo)
}

val extension = project.extensions.create<ImpulsePublishExtension>("impulsePublish")

project.afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("${project.name}-impulse") {
                artifact(extension.artifact)

                groupId = extension.groupId
                artifactId = project.name
                version = project.version.toString()

                pom {
                    name = project.name
                    description = extension.description
                    url = "https://github.com/ArsonClub/Impulse"
                    licenses {
                        extension.licenses.forEach {
                            license {
                                name = it.name
                                url = it.url
                                comments = it.comments
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
                extension.repositories.forEach {
                    maven {
                        name = it.name
                        url = uri(it.url)
                        credentials {
                            username = it.username
                            password = it.password
                        }
                    }
                }
            }
        }
    }
}