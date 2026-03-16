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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.StreamWriteConstraints
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import eu.cloudnetservice.gradle.juppiter.data.ModuleConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*

@CacheableTask
abstract class GenerateModuleJson : DefaultTask() {
  @get:Input
  abstract val fileName: Property<String>

  @get:Nested
  abstract val moduleConfiguration: Property<ModuleConfiguration>

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  @TaskAction
  fun generate() {
    val factory =
      JsonFactory()
        .enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
        .enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION)
        .setStreamWriteConstraints(StreamWriteConstraints.builder().maxNestingDepth(2000).build())

    val module = SimpleModule().apply {
      addSerializer(Provider::class.java, object : JsonSerializer<Provider<*>>() {
        override fun serialize(
          value: Provider<*>,
          gen: JsonGenerator,
          serializers: SerializerProvider
        ) {
          if (value.isPresent) {
            gen.writeObject(value.get())
          }
          else gen.writeNull()
        }
      })
      addSerializer(PropertyValueHolder::class.java, object : JsonSerializer<PropertyValueHolder>() {
        override fun serialize(
          value: PropertyValueHolder, gen: JsonGenerator, serializers: SerializerProvider
        ) {
          gen.writeObject(value.value)
        }
      })
    }
    val mapper =
      ObjectMapper(factory)
        .registerKotlinModule()
        .registerModule(module)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY).writerWithDefaultPrettyPrinter()

    val moduleConfiguration = moduleConfiguration.get()
    mapper.writeValue(outputDirectory.file(fileName).get().asFile, moduleConfiguration)
  }
}
