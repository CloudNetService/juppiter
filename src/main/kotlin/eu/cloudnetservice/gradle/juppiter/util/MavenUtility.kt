/*
 * Copyright 2019-2025 CloudNetService team & contributors
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

package eu.cloudnetservice.gradle.juppiter.util

import eu.cloudnetservice.gradle.juppiter.ModuleConfiguration
import eu.cloudnetservice.gradle.juppiter.UnknownDependencyException
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.HttpURLConnection
import java.net.URL

object MavenUtility {

  fun findRepository(
    dependency: ModuleConfiguration.Dependency,
    repositories: Iterable<MavenArtifactRepository>
  ): ModuleConfiguration.Repository? {
    repositories.forEach {
      val urlInRepository = resolveUrlInRepository(dependency, it) ?: return@forEach
      if (dependency.timestampedVersion != null || dependency.classifier != null) {
        // timestamped version and classifier are not supported by CloudNet module loading currently
        // therefore, we need to hack around this limitation by providing the url directly
        dependency.url = urlInRepository.toExternalForm()
        return null
      } else {
        // CloudNet can download this dependency directly from the maven repository
        val repository = ModuleConfiguration.Repository(it.name)
        repository.url = it.url.toURL().toExternalForm()
        dependency.repo = repository.name
        return repository
      }
    }

    throw UnknownDependencyException(dependency)
  }

  private fun resolveUrlInRepository(
    dependency: ModuleConfiguration.Dependency,
    repository: MavenArtifactRepository
  ): URL? {
    val groupForUrl = dependency.group!!.replace(".", "/")
    val componentVersion = dependency.timestampedVersion ?: dependency.version
    val classifier = if (dependency.classifier != null) "-${dependency.classifier}" else ""
    val componentName = "${dependency.name}-${componentVersion}${classifier}.jar"
    val urlPath = "${groupForUrl}/${dependency.name}/${dependency.version}/${componentName}"
    val fullUrl = URL(repository.url.toURL(), urlPath)
    return if (resourceExists(fullUrl)) fullUrl else null
  }

  private fun resourceExists(url: URL): Boolean {
    return with(url.openConnection() as HttpURLConnection) {
      useCaches = false
      connectTimeout = 5000
      requestMethod = "HEAD"
      instanceFollowRedirects = true

      setRequestProperty("User-Agent", "CloudNetService/juppiter Repository Resolve")
      connect()

      responseCode == 200
    }
  }
}
