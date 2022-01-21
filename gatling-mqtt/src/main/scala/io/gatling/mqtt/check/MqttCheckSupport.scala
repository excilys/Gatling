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

package io.gatling.mqtt.check

import scala.annotation.implicitNotFound

import io.gatling.core.check._
import io.gatling.core.check.bytes.BodyBytesCheckType
import io.gatling.core.check.jmespath.JmesPathCheckType
import io.gatling.core.check.jsonpath.JsonPathCheckType
import io.gatling.core.check.regex.RegexCheckType
import io.gatling.core.check.string.BodyStringCheckType
import io.gatling.core.check.substring.SubstringCheckType
import io.gatling.core.json.JsonParsers

import com.fasterxml.jackson.databind.JsonNode
import io.netty.buffer.ByteBuf

trait MqttCheckSupport {

  // materializers
  implicit def mqttTextJmesPathMaterializer(implicit jsonParsers: JsonParsers): CheckMaterializer[JmesPathCheckType, Check[String], String, JsonNode] = ???

  implicit def mqttBufferJmesPathMaterializer(implicit jsonParsers: JsonParsers): CheckMaterializer[JmesPathCheckType, Check[ByteBuf], ByteBuf, JsonNode] = ???

  implicit def mqttTextJsonPathMaterializer(implicit jsonParsers: JsonParsers): CheckMaterializer[JsonPathCheckType, Check[String], String, JsonNode] = ???

  implicit def mqttBufferJsonPathMaterializer(implicit jsonParsers: JsonParsers): CheckMaterializer[JsonPathCheckType, Check[ByteBuf], ByteBuf, JsonNode] = ???

  implicit val MqttTextRegexCorrelatorMaterializer: CheckMaterializer[RegexCheckType, Check[String], String, String] = ???

  implicit val MqttBufferRegexCorrelatorMaterializer: CheckMaterializer[RegexCheckType, Check[ByteBuf], ByteBuf, String] = ???

  implicit val MqttTextBodyStringCorrelatorMaterializer: CheckMaterializer[BodyStringCheckType, Check[String], String, String] = ???

  implicit val MqttBufferBodyStringCorrelatorMaterializer: CheckMaterializer[BodyStringCheckType, Check[ByteBuf], ByteBuf, String] = ???

  implicit val MqttTextSubstringCorrelatorMaterializer: CheckMaterializer[SubstringCheckType, Check[String], String, String] = ???

  implicit val MqttBufferSubstringCorrelatorMaterializer: CheckMaterializer[SubstringCheckType, Check[ByteBuf], ByteBuf, String] = ???

  implicit val MqttTextBodyBytesCorrelatorMaterializer: CheckMaterializer[BodyBytesCheckType, Check[String], String, Array[Byte]] = ???

  implicit val MqttBufferBodyBytesCorrelatorMaterializer: CheckMaterializer[BodyBytesCheckType, Check[ByteBuf], ByteBuf, Array[Byte]] = ???

  implicit val MqttTextBodyLengthCorrelatorMaterializer: CheckMaterializer[BodyBytesCheckType, Check[String], String, Int] = ???

  implicit val MqttBufferBodyLengthCorrelatorMaterializer: CheckMaterializer[BodyBytesCheckType, Check[ByteBuf], ByteBuf, Int] = ???

  // checks
  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for MQTT.")
  implicit def checkBuilder2MqttCheck[A, P](checkBuilder: CheckBuilder[A, P]) //
  (implicit materializer: CheckMaterializer[A, MqttCheck, ByteBuf, P]): MqttCheck =
    checkBuilder.build(materializer)

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for MQTT.")
  implicit def validate2MqttCheck[T, P, X](
      validate: CheckBuilder.Validate[T, P, X]
  )(implicit materializer: CheckMaterializer[T, MqttCheck, ByteBuf, P]): MqttCheck =
    validate.exists

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for MQTT.")
  implicit def find2MqttCheck[T, P, X](
      find: CheckBuilder.Find[T, P, X]
  )(implicit materializer: CheckMaterializer[T, MqttCheck, ByteBuf, P]): MqttCheck =
    find.find.exists

  // correlators
  implicit def find2MessageCorrelator[T, P](findCheckBuilder: CheckBuilder.Find[T, P, String])(implicit
      textMaterializer: CheckMaterializer[T, Check[String], String, P],
      bufferMaterializer: CheckMaterializer[T, Check[ByteBuf], ByteBuf, P]
  ): MessageCorrelator =
    validate2MessageCorrelator(findCheckBuilder.find)

  implicit def validate2MessageCorrelator[T, P](validate: CheckBuilder.Validate[T, P, String])(implicit
      textMaterializer: CheckMaterializer[T, Check[String], String, P],
      bufferMaterializer: CheckMaterializer[T, Check[ByteBuf], ByteBuf, P]
  ): MessageCorrelator = ???
}
