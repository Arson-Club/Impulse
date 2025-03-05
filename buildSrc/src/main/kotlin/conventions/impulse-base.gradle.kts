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

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.invoke
import utils.libs

plugins {
    id("jacoco")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

dependencies {
    add("compileOnly", libs.velocity)
    add("implementation", libs.kaml)
    add("testImplementation", libs.bundles.test)
}

kotlin {
    jvmToolchain(17)
}

dokka {
    dokkaSourceSets.configureEach {}
}

jacoco {
    toolVersion = "${libs.plugins.jacoco.get().version}"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required = true
        html.required = false
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }

    reports {
        junitXml.required = true
        html.required = false
    }
}
