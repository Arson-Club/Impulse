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

interface ImpulsePublishExtension {
    //TODO: Refactor these into maven POM license providers
    // Project licenses
    val kamlLicense
        get() = LicenseInfo(
            "Apache License 2.0",
            "https://www.apache.org/licenses/LICENSE-2.0",
            "Kaml library license"
        )
    val impulseLicense
        get() = LicenseInfo(
            "GNU Affero General Public License",
            "https://www.gnu.org/licenses/",
            "License for all Impulse sources"
        )
    val classGraphLicense
        get() = LicenseInfo(
            "MIT License",
            "https://opensource.org/licenses/MIT",
            "ClassGraph library license"
        )
    val dockerLicense
        get() = LicenseInfo(
            "Apache License 2.0",
            "https://www.apache.org/licenses/LICENSE-2.0",
            "Docker library license"
        )

    // Project repositories
    val githubPackageRepo
        get() = RepositoryInfo(
            "GitHubPackages",
            "https://maven.pkg.github.com/Arson-Club/Impulse",
            System.getenv("GITHUB_ACTOR") ?: "",
            System.getenv("GITHUB_TOKEN") ?: ""
        )

    val artifact: Property<Task?>
    val groupId: Property<String>
    val description: Property<String>
    val licenses: ListProperty<LicenseInfo>
    val repositories: ListProperty<RepositoryInfo>
}

val extension = project.extensions.create<ImpulsePublishExtension>("impulsePublish")
extension.artifact.convention(null)
extension.groupId.convention("club.arson.impulse")
extension.description.convention("Impulse Server Manager for Velocity")
extension.licenses.convention(listOf(extension.impulseLicense, extension.kamlLicense))
extension.repositories.convention(listOf(extension.githubPackageRepo))

project.afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("impulse") {
                artifact(extension.artifact)

                artifactId = project.name
                version = project.version.toString()
                groupId = extension.groupId.get()
                pom {
                    name = project.name
                    description = extension.description
                    url = "https://github.com/ArsonClub/Impulse"

                    licenses {
                        extension.licenses.get().forEach {
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
        }
        repositories {
            extension.repositories.get().forEach {
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