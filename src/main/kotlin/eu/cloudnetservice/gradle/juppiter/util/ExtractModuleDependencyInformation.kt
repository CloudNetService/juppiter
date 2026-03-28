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

package eu.cloudnetservice.gradle.juppiter.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.util.jar.JarFile

object ExtractModuleDependencyInformation {
  private val objectMapper = ObjectMapper()
  fun extract(file: File): ExtractedModuleDependency {
    val jarFile = JarFile(file)

    // TODO remove, used for testing with old module system
    jarFile.getJarEntry("module.json")?.let { jarFile.getInputStream(it) }.use { objectMapper.readTree(it) }
      ?.let { it["name"] }?.textValue()?.let { return ExtractedModuleDependency(it) }

    val entry = jarFile.getJarEntry("META-INF/cloudnet-module.json")
      ?: throw IllegalArgumentException("File ${file.absolutePath} is not a CloudNet module")
    val jsonNode = jarFile.getInputStream(entry).use { objectMapper.readTree(it) }!!

    val schemaNode: JsonNode? = jsonNode["schema"]
    if (schemaNode == null || !schemaNode.isTextual) {
      throw IllegalArgumentException("File ${file.absolutePath} has a badly formatted cloudnet-module.json")
    }
    val schema = schemaNode.textValue()!!
    return when (schema) {
      "v1" -> {
        val id = jsonNode["id"]?.textValue()
          ?: throw IllegalArgumentException("File ${file.absolutePath} is missing module id")
        ExtractedModuleDependency(id)
      }

      else -> throw IllegalArgumentException("Unknown schema: $schema")
    }
  }
}

data class ExtractedModuleDependency(val id: String)
