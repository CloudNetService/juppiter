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

package eu.cloudnetservice.gradle.juppiter.data

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty

class ModuleConfiguration(
  objectFactory: ObjectFactory,
) {
  @Input
  val id: Property<String> = objectFactory.property()

  @Input
  val name: Property<String> = objectFactory.property()

  @Input
  @Optional
  val description: Property<String> = objectFactory.property()

  @Input
  val entrypoint: Property<String> = objectFactory.property()

  @Input
  @Optional
  val version: Property<String> = objectFactory.property()

  @Nested
  val artifacts: SetProperty<ModuleArtifact> = objectFactory.setProperty()

  @Nested
  val dependencies: SetProperty<ModuleDependency> = objectFactory.setProperty()

  @Nested
  val externalDependencies: SetProperty<ModuleExternalDependency> = objectFactory.setProperty()

  @Nested
  val contributors: SetProperty<ModuleContributor> = objectFactory.setProperty()

  @Input
  val properties: MapProperty<String, Any> = objectFactory.mapProperty()

//  fun setDefaults(
//    project: Project,
//    libraries: Configuration,
//    moduleDependencies: Configuration,
//  ) {
//    if (!this.resolved.getAndSet(true)) {
//      name = name ?: project.name
//      group = group ?: project.group.toString()
//      version = version ?: project.version.toString()
//
//      // other stuff
//      author = author ?: "Anonymous"
//      website = website ?: "https://cloudnetservice.eu"
//      description = description ?: project.description ?: "Just another CloudNet module"
//
//      // dependencies of the module we need to resolve
//      libraries.resolvedConfiguration.resolvedArtifacts.forEach {
//        val versionId = it.moduleVersion.id
//        val dependency = Dependency(it.name)
//        dependency.group = versionId.group
//        dependency.version = versionId.version
//        dependency.classifier = it.classifier
//        dependency.checksum = ChecksumHelper.fileShaSum(it.file)
//
//        val componentIdentifier = it.id.componentIdentifier
//        if (versionId.version.endsWith("-SNAPSHOT") && componentIdentifier is MavenUniqueSnapshotComponentIdentifier) {
//          dependency.timestampedVersion = componentIdentifier.timestampedVersion
//        }
//
//        dependencies.add(dependency)
//      }
//
//      // dependencies of the module that are other modules, so we only need: group, name, version
//      moduleDependencies.resolvedConfiguration.firstLevelModuleDependencies
//        .map { it.module.id }
//        .forEach {
//          val dependency = Dependency(it.name)
//          dependency.group = it.group
//          dependency.version = it.version
//          dependency.needsRepoResolve = false
//
//          dependencies.add(dependency)
//        }
//    }
//  }
//
//  fun resolveRepositories(repositoryHandler: RepositoryHandler) {
//    val repos = repositoryHandler.filterIsInstance<MavenArtifactRepository>()
//    dependencies
//      .filter { it.needsRepoResolve }
//      .forEach {
//        val repo = MavenUtility.findRepository(it, repos) ?: return@forEach
//        repositories.add(repo)
//      }
//  }
//
//  fun validate() {
//    if (main.isNullOrEmpty()) {
//      throw InvalidModuleDescription("main class must be set")
//    }
//  }
}
