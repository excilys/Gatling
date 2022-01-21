/*
 * Copyright 2011-2022 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.javaapi.core.internal.condition

import java.{ util => ju }

import scala.jdk.CollectionConverters._

import io.gatling.javaapi.core.{ ChainBuilder, Choice, StructureBuilder }
import io.gatling.javaapi.core.condition.RandomSwitchOrElse

object ScalaRandomSwitchOrElse {

  final class Choices[T <: StructureBuilder[T, W], W <: io.gatling.core.structure.StructureBuilder[W]](
      context: RandomSwitchOrElse[T, W]
  ) {
    def choices(choices: ju.List[Choice.WithWeight]): OrElse[T, W] =
      new OrElse(context, choices)
  }

  final class OrElse[T <: StructureBuilder[T, W], W <: io.gatling.core.structure.StructureBuilder[W]](
      context: RandomSwitchOrElse[T, W],
      possibilities: ju.List[Choice.WithWeight]
  ) {
    def orElse(chain: ChainBuilder): T =
      context.make(_.randomSwitchOrElse(possibilities.asScala.map(p => (p.weight, p.chain.wrapped)).toSeq: _*)(chain.wrapped))
  }
}
