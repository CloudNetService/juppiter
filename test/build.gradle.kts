import eu.cloudnetservice.gradle.juppiter.data.*

/*
 * Copyright 2019-present CloudNetService team & contributors
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
  id("eu.cloudnetservice.juppiter")
  `java-library`
  id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT" apply false
//  id("net.fabricmc.fabric-loom-remap") apply false
}

data class Cls(@Input val x: String)

moduleJson {
  entrypoint = "abc"
  id = "test"
  name = "Test Module"
  artifacts.add(ModuleArtifact(objects).apply {
    this.source = ModuleArtifactSource.CLASSPATH
    this.sourcePath = "test/source"
    this.targetPath = "test/target"
    this.environments.add("wrapper env ??")
  })
  artifacts.add(ModuleArtifact(objects).apply {
    this.source = ModuleArtifactSource.FILESYSTEM
    this.sourcePath = "test/source"
    this.targetPath = "test/target"
    this.environments.add("wrapper env ??")
  })
//  dependencies.add(ModuleDependency(objects).apply {
//    this.id = "bridge"
//    this.versionRange = "69+"
//    this.dependencyType = ModuleDependencyType.REQUIRED
//  })
//  externalDependencies.add(ModuleExternalDependency(objects).apply {
//    this.environments.add("* or sth")
//    this.loader = "was auch immer loader sein soll"
//    this.optional = true
//    this.properties.putComplex("test1", Cls("val1"))
//    this.properties.putComplex("test2", provider { Cls("val2") })
//  })
  contributors.add(ModuleContributor(objects).apply {
    this.name = "se big bad noob"
    this.properties.putSimple("test1", "val1")
    this.properties.putSimple("test2", provider { "val2" })
  })
  this.properties.putSimple("test1", "val1")
  this.properties.putSimple("test2", provider { "val2" })
}

flavors {
  registerJavaSourceSets()
  val common = addFlavor("common")
  common.dependOn(main)
  val fabric = addFlavor("fabric/base")
  fabric.dependOn(common)
  val forge = addFlavor("forge")
  forge.dependOn(common)
  val fabric1211 = addFlavor("fabric/1_21_1")
  fabric1211.dependOn(fabric)

  val merged = addFlavor("merged")
  merged.dependOn(forge)
  merged.dependOn(fabric1211)
}

tasks.assemble {
  dependsOn(tasks.named("mergedJar"))
}

repositories {
  mavenCentral()
  maven("https://central.sonatype.com/repository/maven-snapshots/")
  maven {
    name = "papermc"
    url = uri("https://repo.papermc.io/repository/maven-public/")
  }

  maven("https://repository.derklaro.dev/releases/")
  maven("https://repository.derklaro.dev/snapshots/")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))
//java.disableAutoTargetJvm()

dependencies {
  "moduleDependency"("eu.cloudnetservice.cloudnet:bridge-impl:[4.0.0-RC14, 4.0.0-RC16]")
//  api("com.google.code.gson:gson:2.13.2")
  moduleLibrary("com.google.code.gson:gson:[2.13.0, 2.13.5]")
//  compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-20260109.190012-50") {
//    this.isTransitive = false
//  }
  moduleLibrary("com.google.guava:guava:33.5.0-jre")
//  moduleLibrary("io.papermc.paper:paper-api:1.21.11-R0.1-20260310.030221-86:javadoc")
//  "commonApi"("com.google.code.gson:gson:2.13.2")
}

val c2 = configurations.resolvable("abc") {
  this.extendsFrom(configurations.compileOnly)
}
//c2.get().run {
//  this.allDependencies.forEach {
//    println(it.version)
//  }
//  resolvedConfiguration.resolvedArtifacts.forEach {
//    val version = it.moduleVersion.id.version
//    println(version)
//    println(it.id.componentIdentifier is MavenUniqueSnapshotComponentIdentifier)
//  }
//}
