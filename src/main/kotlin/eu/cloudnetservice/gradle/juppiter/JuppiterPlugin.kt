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

import eu.cloudnetservice.gradle.juppiter.data.ModuleConfiguration
import eu.cloudnetservice.gradle.juppiter.flavor.FlavorExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class JuppiterPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.run {
      val libraries = configurations.register("moduleLibrary")
      val moduleDependencies = configurations.register("moduleDependency")

      val moduleExtension = ModuleConfiguration(target.objects)
      extensions.add("moduleJson", moduleExtension)

      val flavorExtension = FlavorExtension(this)
      extensions.add("flavors", flavorExtension)

      val generateModuleTask =
        tasks.register<GenerateModuleJson>("genModuleJson") {
          fileName.convention("cloudnet-module.json")
          outputDirectory.convention(layout.buildDirectory.dir("generated/module-json"))
          moduleConfiguration.convention(moduleExtension)
        }

      plugins.withType<JavaPlugin> {
        extensions.getByType<SourceSetContainer>().named(SourceSet.MAIN_SOURCE_SET_NAME) {
          resources.srcDir(generateModuleTask)
          configurations.getByName(compileClasspathConfigurationName).extendsFrom(libraries, moduleDependencies)
        }
      }
    }
  }
}
