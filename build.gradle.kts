/*
 * Copyright 2021 CloudNetService team & contributors
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

plugins {
  `kotlin-dsl`
  `maven-publish`
  `java-gradle-plugin`
  id("org.cadixdev.licenser") version "0.6.1"
  id("com.gradle.plugin-publish") version "0.15.0"
}

version = "0.1.1"
group = "eu.cloudnetservice.gradle"

// we set the kotlin jmvTarget specifically to 1.8 to prevent issues in case the default values changes
val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}

repositories {
  gradlePluginPortal()
}

java {
  withSourcesJar()
}

dependencies {
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.4") {
    exclude(group = "org.jetbrains.kotlin")
  }
}

gradlePlugin {
  plugins {
    register("juppiter") {
      description = project.description
      id = "eu.cloudnetservice.juppiter"
      displayName = "Juppiter Gradle Plugin"
      implementationClass = "eu.cloudnetservice.gradle.juppiter.JuppiterPlugin"
    }
  }
}

pluginBundle {
  description = project.description
  website = "https://cloudnetservice.eu"
  vcsUrl = "https://github.com/CloudNetService/juppiter"
  tags = listOf("cloudnet", "cloudnet-module-util")
}

publishing {
  publications {
    register<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }
}

license {
  include("**/*.kt")
  header(project.file("LICENSE_HEADER"))
}
