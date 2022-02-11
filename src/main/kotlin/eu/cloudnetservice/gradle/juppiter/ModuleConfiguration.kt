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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.cloudnetservice.gradle.juppiter.jackson.JavaVersionSerializer
import eu.cloudnetservice.gradle.juppiter.util.DependencyUtils
import eu.cloudnetservice.gradle.juppiter.util.MavenUtility
import groovy.lang.Closure
import org.gradle.api.JavaVersion
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.util.concurrent.atomic.AtomicBoolean

open class ModuleConfiguration(project: Project) {

  // internal marker to prevent duplicate resolving of dependencies
  // this was introduced initially because of a gradle issue introduces
  // in version 7.4 but might be useful in the future too
  // https://github.com/gradle/gradle/issues/19848
  @JsonIgnore
  private var resolved: AtomicBoolean = AtomicBoolean()

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
  var dataFolder: String? = null

  @Input
  @Optional
  var description: String? = null

  @Input
  @Optional
  @JsonSerialize(using = JavaVersionSerializer::class, nullsUsing = JavaVersionSerializer::class)
  var minJavaVersionId: JavaVersion? = null

  @Nested
  val properties: Map<String, Any> = HashMap()

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

    @Input
    @Optional
    var checksum: String? = null

    @Input
    @JsonIgnore
    var needsRepoResolve: Boolean = true
  }

  fun setDefaults(project: Project, libraries: Configuration, moduleDependencies: Configuration) {
    if (!this.resolved.getAndSet(true)) {
      name = name ?: project.name
      group = group ?: project.group.toString()
      version = version ?: project.version.toString()

      // other stuff
      author = author ?: "Anonymous"
      website = website ?: "https://cloudnetservice.eu"
      description = description ?: project.description ?: "Just another CloudNet3 module"

      // dependencies of the module we need to resolve
      libraries.resolvedConfiguration.resolvedArtifacts.forEach {
        dependencies.add(DependencyUtils.convertToDependency(it, it.moduleVersion.id))
      }

      // dependencies of the module that are other modules, so we only need: group, name, version
      moduleDependencies.resolvedConfiguration.firstLevelModuleDependencies
        .map { it.module.id }
        .forEach {
          val dependency = Dependency(it.name)
          dependency.group = it.group
          dependency.version = it.version
          dependency.needsRepoResolve = false

          dependencies.add(dependency)
        }
    }
  }

  fun resolveRepositories(project: Project) {
    val repos = project.repositories.filterIsInstance<MavenArtifactRepository>()
    // get the repos for the dependencies, throw an exception if we cannot resolve a dependency
    dependencies
      .filter { it.needsRepoResolve }
      .forEach {
        val repo = MavenUtility.resolveRepository(it, repos) ?: throw UnknownDependencyException(it)
        // set the repository of the dependency
        it.repo = repo.name
        // convert the repo
        val repository = Repository(repo.name)
        repository.url = repo.url.toURL().toExternalForm()
        // register the repo
        repositories.add(repository)
      }
  }

  fun validate() {
    if (main.isNullOrEmpty()) {
      throw InvalidModuleDescription("main class must be set")
    }
  }
}
