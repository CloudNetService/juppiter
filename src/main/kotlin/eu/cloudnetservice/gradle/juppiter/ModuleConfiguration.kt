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

package eu.cloudnetservice.gradle.juppiter

import eu.cloudnetservice.gradle.juppiter.util.MavenUtility
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

open class ModuleConfiguration(objects: ObjectFactory) {

  @Input
  var runtimeModule = false

  @Input
  var storesSensitiveData = false

  @Input
  var main: String? = null

  @Input
  @Optional
  var name: String? = null

  @Input
  @Optional
  var group: String? = null

  @Input
  @Optional
  var author: String? = null

  @Input
  @Optional
  var version: String? = null

  @Input
  @Optional
  var website: String? = null

  @Input
  @Optional
  var description: String? = null

  @Nested
  val repositories: NamedDomainObjectContainer<Repository> = objects.domainObjectContainer(Repository::class.java)

  @Nested
  val dependencies: NamedDomainObjectContainer<Dependency> = objects.domainObjectContainer(Dependency::class.java)

  // for groovy
  fun repositories(closure: Closure<Unit>) = repositories.configure(closure)
  fun dependencies(closure: Closure<Unit>) = dependencies.configure(closure)

  data class Repository(@Input val name: String, @Input val url: String)

  data class Dependency(@Input val group: String, @Input val name: String, @Input val version: String) {
    @Input
    @Optional
    var url: String? = null

    @Input
    @Optional
    var repo: String? = null
  }

  fun setDefaults(project: Project, libraries: Configuration) {
    name = name ?: project.name
    group = group ?: project.group.toString()
    version = version ?: project.version.toString()

    // other stuff
    author = author ?: "Anonymous"
    website = website ?: "https://cloudnetservice.eu"
    description = description ?: project.description ?: "Just another CloudNet3 module"

    val repos = project.repositories.filterIsInstance<MavenArtifactRepository>()
    libraries.resolvedConfiguration.resolvedArtifacts
      .map { it.moduleVersion.id }
      .filter { it.group != project.group }
      .forEach {
        val repository = MavenUtility.resolveRepository(it, repos) ?: return@forEach

        val dependency = Dependency(it.group, it.name, it.version)
        dependency.repo = repository.name

        dependencies.add(dependency)
        repositories.add(Repository(repository.name, repository.url.toURL().toExternalForm()))
      }
  }

  fun validate() {
    if (main.isNullOrEmpty()) {
      throw InvalidModuleDescription("main class must be set")
    }
  }
}
