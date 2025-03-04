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

plugins {
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`shadow-jar`
    buildsrc.convention.dokka

    id("club.arson.impulse.base")

    id("eclipse")
}

group = "club.arson.impulse"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(libs.bundles.app)
    implementation(project(":api"))

    testImplementation(project(":api"))
    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.bundles.test)
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

tasks.withType<ShadowJar>().configureEach {
    relocate("org.jetbrains.kotlin", "club.arson.impulse.kotlin")
    relocate("io.github.classgraph", "club.arson.impulse.classgraph")
    archiveBaseName = "impulse-lite"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    dependsOn(":api:jar")
}