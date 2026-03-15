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

package eu.cloudnetservice.gradle.juppiter.flavor

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.setProperty

class FlavorExtension(private val project: Project) {
  private var javaRegistered = false
  private val flavors = HashSet<Flavor>()

  val main: Flavor = Flavor("main", project.objects.setProperty<Flavor>().apply { finalizeValue() })

  fun addFlavor(name: String): Flavor {
    return Flavor(name, project.objects.setProperty()).apply {
      flavors.add(this)
      if (javaRegistered) {
        registerJava(this)
      }
    }
  }

  fun registerJavaSourceSets() {
    if (javaRegistered) return
    javaRegistered = true
    flavors.forEach {
      registerJava(it)
    }
  }

  private fun registerJava(flavor: Flavor) {
    val java = project.extensions.getByType<JavaPluginExtension>()
    val sourceSets = java.sourceSets
    val sourceSet = sourceSets.register(flavor.name)
    val files = project.objects.fileCollection()
    files.from(flavor.dependsOn.map { flavors ->
//      val flavors = collectDependencies(flavor = flavor).filter { it !== flavor }
      flavors.map { dependencyFlavor ->
        sourceSets.named(dependencyFlavor.name).map { it.output }
      }
    })
    val configurations = project.configurations
    val apiConfiguration = configurations.register(sourceSet.get().apiConfigurationName)
    val compileOnlyApiConfiguration = configurations.register(sourceSet.get().compileOnlyApiConfigurationName)
    configurations.named(sourceSet.get().implementationConfigurationName).configure {
      extendsFrom(apiConfiguration.get())
    }
    configurations.named(sourceSet.get().compileOnlyConfigurationName).configure {
      extendsFrom(compileOnlyApiConfiguration.get())
    }

    // Get around IDE loading this too early. Ugly but works
    // Loading early causes flavor.dependsOn to be read-only
    project.afterEvaluate {
      project.dependencies.add(sourceSet.get().apiConfigurationName, files)

      apiConfiguration.configure {
        flavor.dependsOn.get().map { sourceSets.getByName(it.name) }.forEach {
          extendsFrom(configurations.getByName(it.apiConfigurationName))
        }
      }
      compileOnlyApiConfiguration.configure {
        flavor.dependsOn.get().map { sourceSets.getByName(it.name) }.forEach {
          extendsFrom(configurations.getByName(it.compileOnlyApiConfigurationName))
        }
      }
      configurations.named(sourceSet.get().runtimeOnlyConfigurationName).configure {
        flavor.dependsOn.get().map { sourceSets.getByName(it.name) }.forEach {
          extendsFrom(configurations.getByName(it.runtimeClasspathConfigurationName))
        }
      }
    }

    val task = project.tasks.register<Jar>(sourceSet.get().jarTaskName) {
      val dependencies = flavor.dependsOn.get()
      archiveClassifier.set(flavor.name)
      from(sourceSet.map { it.output })
      dependencies.forEach { dependency ->
        val taskName = sourceSets.named(dependency.name).get().jarTaskName
        from(project.tasks.named(taskName))
      }
    }
    project.tasks.named("assemble").configure {
      dependsOn(task.get())
    }
  }

  private fun collectDependencies(set: MutableSet<Flavor> = HashSet(), flavor: Flavor): MutableSet<Flavor> {
    if (set.add(flavor)) {
      flavor.dependsOn.get().forEach {
        collectDependencies(set, it)
      }
    }
    return set
  }
}
