/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("gradlebuild.distribution.api-java")
}

description = """Gradle Module Metadata (GMM)-specific publishing functionality. This project "extends" the base publish
project by adding many GMM-specific abstractions or implementations.  This project is a implementation dependency of the maven and 
ivy subprojects in the Gradle build, as they write GMM as well as Ivy/Maven metadata.
"""

dependencies {
    implementation(project(":base-services"))
    implementation(project(":base-services-groovy"))
    implementation(project(":core"))
    implementation(project(":core-api"))
    implementation(project(":dependency-management"))
    implementation(project(":file-collections"))
    implementation(project(":functional"))
    implementation(project(":logging"))
    implementation(project(":model-core"))
    implementation(project(":publish"))

    implementation(libs.commonsLang)
    implementation(libs.groovy)
    implementation(libs.guava)
    implementation(libs.inject)

    integTestDistributionRuntimeOnly(project(":distributions-core"))
}