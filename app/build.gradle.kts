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
    conventions.`impulse-base`
    conventions.`impulse-publish`
    conventions.`shadow-jar`

    id("eclipse")
}

group = "club.arson.impulse"

dependencies {
    implementation(libs.kotlinReflect)
    implementation(libs.kotlinStdLib)
    implementation(libs.classGraph)
    implementation(project(":api"))

    testImplementation(project(":api"))
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

impulsePublish {
    artifact = tasks.named("shadowJar").get()
    description = "Lite distribution of Impulse without any bundled brokers."
    licenses = listOf(
        kamlLicense,
        impulseLicense,
        classGraphLicense
    )
}