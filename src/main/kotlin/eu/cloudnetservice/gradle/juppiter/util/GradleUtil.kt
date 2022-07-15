/*
 * Copyright 2021 - 2022 CloudNetService team & contributors
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

import org.gradle.api.plugins.ExtensionContainer
import kotlin.reflect.KClass

object GradleUtil {

  fun <E : Any> findOrAddExtension(extensions: ExtensionContainer, name: String, type: KClass<E>, factory: () -> E): E {
    return extensions.findByType(type.java) ?: run {
      val extension = factory.invoke()
      extensions.add(name, extension)
      extension
    }
  }
}
