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

package eu.cloudnetservice.gradle.juppiter.tasks

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import eu.cloudnetservice.gradle.juppiter.data.ModuleDependencyType
import eu.cloudnetservice.gradle.juppiter.util.ChecksumHelper
import eu.cloudnetservice.gradle.juppiter.util.ExtractModuleDependencyInformation
import eu.cloudnetservice.gradle.juppiter.util.UnknownDependencyException
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@CacheableTask
abstract class PrepareModuleJson : DefaultTask() {
  @get:Input
  abstract val repositories: SetProperty<String>

  @get:Nested
  abstract val unresolvedModuleDependencies: SetProperty<UnresolvedModuleDependency>

  @get:Nested
  abstract val unresolvedExternalDependencies: SetProperty<UnresolvedExternalDependency>

  @get:OutputFile
  abstract val moduleJson: RegularFileProperty

  init {
    repositories.finalizeValueOnRead()
    unresolvedModuleDependencies.finalizeValueOnRead()
    unresolvedExternalDependencies.finalizeValueOnRead()
  }

  @TaskAction
  fun run() {
    val repositories = repositories.get()
    val externalDependencies = unresolvedExternalDependencies.get().map { dep ->
      if (dep.snapshot) {
        // We pass all repositories for snapshot dependencies.
        // Some dependencies of the snapshots could be on other repositories, there is no clear "source" repository
        val properties = HashMap<String, Any>()
        properties["group"] = dep.group
        properties["name"] = dep.name
        properties["version"] = dep.version
        dep.classifier?.let { properties["classifier"] = it }
        properties["repositories"] = repositories
        ResolvedExternalDependency("maven-snapshot", dep.optional, dep.environments, properties)
      } else {
        repositories.forEach { repository ->
          val repositoryUrl = URL(repository)
          val depUrl = buildURL(repositoryUrl, dep)
          if (!depUrl.hasBackedResource()) {
            return@forEach
          }

          val properties = HashMap<String, Any>()
          properties["group"] = dep.group
          properties["name"] = dep.name
          properties["version"] = dep.version
          dep.classifier?.let { properties["classifier"] = it }
          properties["repository"] = repository
          properties["url"] = depUrl.toString()
          properties["checksum"] = "sha3256:${ChecksumHelper.sha3256(dep.file)}"
          return@map ResolvedExternalDependency("maven", dep.optional, dep.environments, properties)
        }
        throw UnknownDependencyException("Failed to resolve $dep")
      }
    }.toSet()
    val moduleDependencies = unresolvedModuleDependencies.get().map { dep ->
      val id = ExtractModuleDependencyInformation.extract(dep.file).id
      ResolvedModuleDependency(id, dep.versionRange, dep.dependencyType)
    }.toSet()
    val prepared = PreparedModuleJson(moduleDependencies, externalDependencies)
    moduleJson.get().asFile.writeText(prepared.serialized())
  }

  private fun URL.hasBackedResource(): Boolean {
    return with(openConnection() as HttpURLConnection) {
      useCaches = false
      connectTimeout = 500
      requestMethod = "HEAD"
      instanceFollowRedirects = true

      setRequestProperty("User-Agent", "CloudNetService/juppiter Repository Resolve")
      connect()

      responseCode == 200
    }
  }

  private fun buildURL(repositoryUrl: URL, dep: UnresolvedExternalDependency): URL {
    val group = dep.group.replace(".", "/")
    val version = dep.version
    val classifier = dep.classifier?.let { "-$it" } ?: ""
    val componentName = "${dep.name}-$version$classifier.jar"
    val urlPath = "$group/${dep.name}/$version/$componentName"
    return URL(repositoryUrl, urlPath)
  }

  data class UnresolvedModuleDependency(
    @Input
    val versionRange: String,
    @Input
    val optional: Boolean,
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    val file: File,
    @Input
    val dependencyType: ModuleDependencyType
  )

  data class UnresolvedExternalDependency(
    @Input
    val snapshot: Boolean,
    @Input
    val group: String,
    @Input
    val name: String,
    @Input
    val version: String,
    @Input
    @Optional
    val classifier: String?,
    @Input
    val environments: Set<String>,
    @Input
    val optional: Boolean,
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    val file: File
  )

  data class ResolvedModuleDependency(
    val id: String,
    val versionRange: String,
    val dependencyType: ModuleDependencyType
  )

  data class ResolvedExternalDependency(
    val loader: String,
    val optional: Boolean,
    val environments: Set<String>,
    // For future maintainers: take great care of serialization
    val properties: Map<String, Any>
  )

  data class PreparedModuleJson(
    val moduleDependencies: Set<ResolvedModuleDependency>,
    val externalDependencies: Set<ResolvedExternalDependency>
  ) {
    fun serialized(): String = mapper.writeValueAsString(this)

    companion object {
      private val mapper = ObjectMapper().registerKotlinModule()

      fun deserialize(input: String): PreparedModuleJson {
        return mapper.readValue<PreparedModuleJson>(input)
      }
    }
  }
}
