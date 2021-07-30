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

import com.fasterxml.jackson.annotation.JsonTypeInfo
import eu.cloudnetservice.gradle.juppiter.util.MavenUtility
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

open class ModuleConfiguration(project: Project) {

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
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, include = JsonTypeInfo.As.WRAPPER_OBJECT)
  val repositories: NamedDomainObjectContainer<Repository> = project.container(Repository::class.java)

  @Nested
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, include = JsonTypeInfo.As.WRAPPER_OBJECT)
  val dependencies: NamedDomainObjectContainer<Dependency> = project.container(Dependency::class.java)

  // for groovy
  fun repositories(closure: Closure<Unit>) = repositories.configure(closure)
  fun dependencies(closure: Closure<Unit>) = dependencies.configure(closure)

  data class Repository(@Input val name: String) {
    @Input
    var url: String? = null
  }

  data class Dependency(@Input val name: String) {
    @Input
    var group: String? = null

    @Input
    var version: String? = null

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
      .forEach {
        val repository = MavenUtility.resolveRepository(it, repos) ?: return@forEach

        val dependency = Dependency(it.name)
        dependency.group = it.group
        dependency.version = it.version
        dependency.repo = repository.name

        val repo = Repository(repository.name)
        repo.url = repository.url.toURL().toExternalForm()

        repositories.add(repo)
        dependencies.add(dependency)
      }
  }

  fun validate() {
    if (main.isNullOrEmpty()) {
      throw InvalidModuleDescription("main class must be set")
    }
  }
}
