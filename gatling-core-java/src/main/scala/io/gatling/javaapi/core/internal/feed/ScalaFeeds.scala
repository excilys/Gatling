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

package io.gatling.javaapi.core.internal.feed

import java.{ util => ju }
import java.util.{ function => juf }

import scala.jdk.CollectionConverters._

import io.gatling.javaapi.core.StructureBuilder
import io.gatling.javaapi.core.feed.Feeds

object ScalaFeeds {

  def apply[T <: StructureBuilder[T, W], W <: io.gatling.core.structure.StructureBuilder[W]](
      context: Feeds[T, W],
      feederBuilder: juf.Supplier[ju.Iterator[ju.Map[String, AnyRef]]]
  ): T =
    context.make(_.feed(() => feederBuilder.get().asScala.map(_.asScala.toMap)))

  def apply[T <: StructureBuilder[T, W], W <: io.gatling.core.structure.StructureBuilder[W]](
      context: Feeds[T, W],
      feeder: ju.Iterator[ju.Map[String, AnyRef]]
  ): T =
    context.make(_.feed(feeder.asScala.map(_.asScala.toMap)))
}
