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

import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

/**
 * Gradle does not allow mixing @Input and @Nested.
 *
 * This class is to support both primitive @Input types and complex @Nested types
 */
class PropertyValueHolder {
  private constructor(simple: Any?, complex: Any) {
    this.simple = simple
    this.complex = complex
  }

  @Input
  @Optional
  val simple: Any?

  @Nested
  val complex: Any

  @get:Internal
  val value: Any
    get() = simple ?: complex

  companion object {
    fun simple(value: Any) = PropertyValueHolder(value, Empty)
    fun complex(value: Any) = PropertyValueHolder(null, value)
  }

  private object Empty
}

fun MapProperty<String, PropertyValueHolder>.putSimple(key: String, value: Any) =
  put(key, PropertyValueHolder.simple(value))

fun MapProperty<String, PropertyValueHolder>.putComplex(key: String, value: Any) =
  put(key, PropertyValueHolder.complex(value))

