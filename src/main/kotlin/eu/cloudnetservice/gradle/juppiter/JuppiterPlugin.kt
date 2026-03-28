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

package eu.cloudnetservice.gradle.juppiter

import eu.cloudnetservice.gradle.juppiter.data.*
import eu.cloudnetservice.gradle.juppiter.flavor.FlavorExtension
import eu.cloudnetservice.gradle.juppiter.tasks.GenerateModuleJson
import eu.cloudnetservice.gradle.juppiter.util.ChecksumHelper
import eu.cloudnetservice.gradle.juppiter.util.ExtractModuleDependencyInformation
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class JuppiterPlugin : Plugin<Project> {
  fun ConfigurationContainer.declarable(name: String) = declarable(name) {}
  fun ConfigurationContainer.declarable(
    name: String, action: Action<in Configuration>
  ): NamedDomainObjectProvider<Configuration> = register(name) {
    isCanBeResolved = false
    isCanBeConsumed = false
    action.execute(this)
  }

  private fun Project.collect(
    dependency: ResolvedDependency,
    target: MutableSet<IntermediateExternalDependency> = HashSet()
  ): Set<IntermediateExternalDependency> {
    // Empty artifacts can exist for BOMs. We ignore those for now
    if (dependency.moduleArtifacts.isEmpty()) {
      return target
    }
    // For now, we require a single artifact
    dependency.moduleArtifacts.single().apply {
      val loader = "maven"
      val environments = setOf("*")
      val optional = false
      val group = moduleVersion.id.group
      val name = name
      val version = moduleVersion.id.version
      val classifier = classifier
      val snapshot = version.endsWith("-SNAPSHOT")
      val checksum = if (snapshot) null else ChecksumHelper.sha3256(file)
      target.add(
        IntermediateExternalDependency(
          loader,
          environments,
          optional,
          group,
          name,
          version,
          classifier,
          checksum
        )
      )
      if (!snapshot) {
        dependency.children.forEach {
          collect(it, target)
        }
      }
    }
    return target
  }

  override fun apply(target: Project) {
    target.run {
      val libraries = configurations.declarable("moduleLibrary")
      val moduleDependencies = configurations.declarable("moduleDependency")
      val librariesOnly = configurations.declarable("moduleLibraryOnly") {
        extendsFrom(libraries)
      }
      val moduleDependenciesOnly = configurations.declarable("moduleDependencyOnly") {
        extendsFrom(moduleDependencies)
      }
      val librariesClasspath = configurations.resolvable("moduleLibraryClasspath") { extendsFrom(libraries) }
      val dependenciesClasspath =
        configurations.resolvable("moduleDependencyClasspath") { extendsFrom(moduleDependencies) }
      val librariesOnlyClasspath =
        configurations.resolvable("moduleLibraryOnlyClasspath") { extendsFrom(librariesOnly) }
      val dependenciesOnlyClasspath =
        configurations.resolvable("moduleDependencyOnlyClasspath") { extendsFrom(moduleDependenciesOnly) }

      val moduleExtension = ModuleConfiguration(objects)
      extensions.add("moduleJson", moduleExtension)

      val flavorExtension = FlavorExtension(this)
      extensions.add("flavors", flavorExtension)

      val generateModuleTask =
        tasks.register<GenerateModuleJson>("genModuleJson") {
          outputFile.convention(
            layout.buildDirectory.dir("generated/module-json").map { it.file("cloudnet-module.json") })
          moduleConfiguration.convention(moduleExtension)
        }

      moduleExtension.dependencies.addAll(dependenciesOnlyClasspath.map { configuration ->
        configuration.allDependencies.map { dependency ->
          ModuleDependency(objects).apply {
            // We resolve a detached configuration with our single dependency.
            val extracted = configurations.detachedConfiguration(dependency)
              .also { it.isTransitive = false }.incoming.artifacts.resolvedArtifacts.map { set -> set.single() }
              .map { result -> ExtractModuleDependencyInformation.extract(result.file) }

            id.convention(extracted.map { it.id })
            versionRange.convention(dependency.version)
            dependencyType.convention(ModuleDependencyType.REQUIRED)
          }
        }
      })

      moduleExtension.externalDependencies.addAll(librariesOnlyClasspath.map { configuration ->
        configuration.allDependencies.flatMap { dependency ->

          val configuration = configurations.detachedConfiguration(dependency)


          val intermediates =
            configuration.resolvedConfiguration.firstLevelModuleDependencies.single().let { collect(it) }
          intermediates.map { e ->
            ModuleExternalDependency(objects).apply {
              this.loader.convention(e.loader)
              this.optional.convention(e.optional)
              this.environments.convention(e.environments)
              this.properties.putSimple("group", e.group)
              this.properties.putSimple("name", e.name)
              this.properties.putSimple("version", e.version)
              e.classifier?.also { this.properties.putSimple("classifier", it) }
              e.checksum?.also { this.properties.putSimple("checksum", it) }
            }
          }
//          if (isSnapshot(dependency)) {
//            configuration.isTransitive = false
//            configuration.resolvedConfiguration.firstLevelModuleDependencies.forEach { dep ->
//              println(dep)
//            }
//            configuration.resolvedConfiguration.resolvedArtifacts.map { resolvedArtifact ->
//              ModuleExternalDependency(objects).also { externalDependency ->
//                externalDependency.loader.convention("maven")
//                externalDependency.environments.convention(setOf("*"))
//                externalDependency.optional.convention(false)
//                externalDependency.properties.putSimple("group", resolvedArtifact.moduleVersion.id.group)
//                externalDependency.properties.putSimple("name", resolvedArtifact.name)
//                externalDependency.properties.putSimple("version", resolvedArtifact.moduleVersion.id.version)
//                resolvedArtifact.classifier?.let { externalDependency.properties.putSimple("classifier", it) }
//              }
//            }
//          } else {
//            configuration.resolvedConfiguration.firstLevelModuleDependencies.forEach { dep ->
//              println(dep)
//              dep.moduleArtifacts.forEach { artifact ->
//                println(artifact)
//              }
//              println(dep.children)
//            }
//            configuration.resolvedConfiguration.resolvedArtifacts.map { resolvedArtifact ->
//              ModuleExternalDependency(objects).also { externalDependency ->
//                externalDependency.loader.convention("maven")
//                externalDependency.environments.convention(setOf("*"))
//                externalDependency.optional.convention(false)
//                externalDependency.properties.putSimple("group", resolvedArtifact.moduleVersion.id.group)
//                externalDependency.properties.putSimple("name", resolvedArtifact.name)
//                externalDependency.properties.putSimple("version", resolvedArtifact.moduleVersion.id.version)
//                resolvedArtifact.classifier?.let { externalDependency.properties.putSimple("classifier", it) }
//                externalDependency.properties.putSimple("checksum", ChecksumHelper.fileShaSum(resolvedArtifact.file))
//              }
//            }
//          }
        }
      })


//      afterEvaluate {
//        libraries.map { it.allDependencies }.map {
//          it.map {
//            val c = configurations.detachedConfiguration(it)
//            c.isCanBeResolved = true
//            c.isCanBeConsumed = false
//            c.isCanBeDeclared = false
//
//            c.resolvedConfiguration.firstLevelModuleDependencies.forEach {
//              println(it)
//            }
//          }
//        }.get()
//      }

      plugins.withType<JavaPlugin> {
        extensions.getByType<SourceSetContainer>().named(SourceSet.MAIN_SOURCE_SET_NAME) {
          resources.srcDir(generateModuleTask)
          configurations.getByName(compileClasspathConfigurationName).extendsFrom(libraries, moduleDependencies)
        }
      }
    }
  }
}

data class IntermediateExternalDependency(
  val loader: String,
  val environments: Set<String>,
  val optional: Boolean,
  val group: String,
  val name: String,
  val version: String,
  val classifier: String?,
  val checksum: String?
)
