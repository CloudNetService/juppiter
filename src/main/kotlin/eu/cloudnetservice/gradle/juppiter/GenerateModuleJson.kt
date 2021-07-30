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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

open class GenerateModuleJson : DefaultTask() {

  @Input
  val fileName: Property<String> = project.objects.property()

  @Nested
  val moduleConfiguration: Property<ModuleConfiguration> = project.objects.property()

  @OutputDirectory
  val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

  @TaskAction
  fun generate() {
    val factory = JsonFactory()
      .enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
      .enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION)

    val mapper = ObjectMapper(factory)
      .registerKotlinModule()
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    mapper.writeValue(outputDirectory.file(fileName).get().asFile, moduleConfiguration.get())
  }
}
