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
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.10"
    id("eclipse")
    id("maven-publish")

    `dokka-convention`
}

group = "club.arson.impulse"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.classgraph:classgraph:4.8.179")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.20-RC")
    implementation(project(":api"))

    testImplementation(project(":api"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("com.google.inject:guice:7.0.0")
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf("version" to project.version)
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.main.configure { java.srcDir(generateTemplates.map { it.outputs }) }

project.eclipse.synchronizationTasks(generateTemplates)

dokka {
    dokkaSourceSets.configureEach {}
}

tasks {
    shadowJar {
        manifest {}
        dependsOn(":api:shadowJar")
        from(sourceSets.main.get().output)
        relocate("com.charleskorn.kaml", "club.arson.impulse.kaml")
        relocate("com.github.docker.java", "club.arson.impulse.docker.java")
        archiveClassifier.set("")
        archiveBaseName.set("impulse-lite")
    }
    test {
        dependsOn(":api:shadowJar")
        useJUnitPlatform()
    }
}