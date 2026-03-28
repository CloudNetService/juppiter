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

package eu.cloudnetservice.gradle.juppiter.data

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property

class ModuleDependency(objectFactory: ObjectFactory) {
  @Input
  val id: Property<String> = objectFactory.property()
  @Input
  val versionRange: Property<String> = objectFactory.property()
  @Input
  val dependencyType: Property<ModuleDependencyType> = objectFactory.property()

  init {
    id.finalizeValueOnRead()
    versionRange.finalizeValueOnRead()
    dependencyType.finalizeValueOnRead()
  }
}

enum class ModuleDependencyType {
  REQUIRED,
  SUGGESTED,
  OPTIONAL
}
