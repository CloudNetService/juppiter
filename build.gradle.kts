/*
 * Copyright 2021 - 2025 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.nio.charset.StandardCharsets

plugins {
  `kotlin-dsl`
  `maven-publish`
  `java-gradle-plugin`
  id("com.diffplug.spotless") version "7.0.3"
  id("org.jetbrains.kotlin.jvm") version "2.0.21"
  id("com.gradle.plugin-publish") version "1.3.1"
}

version = "0.4.0"
group = "eu.cloudnetservice.gradle"
description = "A Gradle plugin that generates the module.json for CloudNet modules based on the Gradle project"

java {
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8

  toolchain {
    vendor = JvmVendorSpec.AZUL
    languageVersion = JavaLanguageVersion.of(8)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(gradleApi())
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0") {
    exclude(group = "org.jetbrains.kotlin")
  }
}

gradlePlugin {
  website = "https://cloudnetservice.eu"
  vcsUrl = "https://github.com/CloudNetService/juppiter"

  plugins {
    register("juppiter") {
      description = project.description
      id = "eu.cloudnetservice.juppiter"
      displayName = "Juppiter Gradle Plugin"
      tags = listOf("cloudnet", "cloudnet-module-util")
      implementationClass = "eu.cloudnetservice.gradle.juppiter.JuppiterPlugin"
    }
  }
}

spotless {
  encoding = StandardCharsets.UTF_8
  lineEndings = com.diffplug.spotless.LineEnding.UNIX

  kotlin {
    ktlint()
    licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
  }
}
