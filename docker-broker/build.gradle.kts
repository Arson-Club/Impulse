import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`shadow-jar`
    buildsrc.convention.dokka

    id("club.arson.impulse.base")
}
group = "club.arson.impulse"

dependencies {
    implementation(libs.bundles.docker)
    implementation(libs.kotlinxCoroutines)
    implementation(project(":api"))
}

tasks.withType<ShadowJar>().configureEach {
    relocate("com.github.docker-java", "club.arson.impulse.docker-java")
    relocate("org.jetbrains.kotlinx", "club.arson.impulse.kotlinx")
}