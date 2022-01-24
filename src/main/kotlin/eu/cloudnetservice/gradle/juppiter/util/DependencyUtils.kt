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

import eu.cloudnetservice.gradle.juppiter.ModuleConfiguration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.internal.artifacts.repositories.resolver.MavenUniqueSnapshotComponentIdentifier
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object DependencyUtils {

  fun convertToDependency(art: ResolvedArtifact, id: ModuleVersionIdentifier): ModuleConfiguration.Dependency {
    var version = id.version
    if (id.version.endsWith("-SNAPSHOT") && art.id.componentIdentifier is MavenUniqueSnapshotComponentIdentifier) {
      version = (art.id.componentIdentifier as MavenUniqueSnapshotComponentIdentifier).timestampedVersion
    }

    val dependency = ModuleConfiguration.Dependency(id.name)
    dependency.group = id.group
    dependency.version = version
    dependency.checksum = ChecksumHelper.fileShaSum(art.file)

    return dependency
  }
}

// copied from the CloudNet-Updater to keep consistent when generating checksums
object ChecksumHelper {

  @Throws(IOException::class)
  fun fileShaSum(path: File): String {
    return newSha3256Digest().run {
      update(path.readBytes())
      bytesToHex(digest())
    }
  }

  @Throws(NoSuchAlgorithmException::class)
  private fun newSha3256Digest(): MessageDigest = MessageDigest.getInstance("SHA3-256")

  private fun bytesToHex(input: ByteArray): String {
    val buffer = StringBuilder()
    for (b in input) {
      buffer.append(Character.forDigit(b.toInt() shr 4 and 0xF, 16))
      buffer.append(Character.forDigit(b.toInt() and 0xF, 16))
    }
    // convert to a string
    return buffer.toString()
  }
}
