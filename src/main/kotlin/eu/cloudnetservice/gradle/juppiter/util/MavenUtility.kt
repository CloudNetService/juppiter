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

package eu.cloudnetservice.gradle.juppiter.util

import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.HttpURLConnection
import java.net.URL

object MavenUtility {

  fun resolveRepository(
    id: ModuleVersionIdentifier,
    repositories: Iterable<MavenArtifactRepository>
  ): MavenArtifactRepository? {
    return repositories.firstOrNull {
      val url = URL(
        it.url.toURL(),
        "${id.group.replace('.', '/')}/${id.name}/${id.version}/${id.name}-${id.version}.jar"
      )
      with(url.openConnection() as HttpURLConnection) {
        useCaches = false
        readTimeout = 5000
        connectTimeout = 5000

        setRequestProperty(
          "User-Agent",
          "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
        )

        connect()
        responseCode == 200
      }
    }
  }
}
