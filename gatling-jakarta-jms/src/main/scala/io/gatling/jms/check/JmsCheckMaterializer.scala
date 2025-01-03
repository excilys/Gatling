/*
 * Copyright 2011-2025 GatlingCorp (https://gatling.io)
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

package io.gatling.jms.check

import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import javax.jms.{ BytesMessage, Message, TextMessage }

import io.gatling.commons.validation._
import io.gatling.core.check.{ identityPreparer, CheckMaterializer, Preparer }
import io.gatling.core.check.bytes.BodyBytesCheckType
import io.gatling.core.check.jmespath.JmesPathCheckType
import io.gatling.core.check.jsonpath.JsonPathCheckType
import io.gatling.core.check.string.BodyStringCheckType
import io.gatling.core.check.substring.SubstringCheckType
import io.gatling.core.check.xpath.{ XPathCheckType, XmlParsers }
import io.gatling.core.json.JsonParsers
import io.gatling.jms.JmsCheck
import io.gatling.jms.client.CachingMessage

import com.fasterxml.jackson.databind.JsonNode
import net.sf.saxon.s9api.XdmNode

final class JmsCheckMaterializer[T, P](override val preparer: Preparer[Message, P]) extends CheckMaterializer[T, JmsCheck, Message, P](identity)

object JmsCheckMaterializer {

  private def bodyBytesPreparer(charset: Charset): Preparer[Message, Array[Byte]] = {
    case tm: TextMessage          => tm.getText.getBytes(charset).success
    case bm: CachingMessage.Bytes => bm.bytes.success
    case _                        => "Unsupported message type".failure
  }

  private def bodyLengthPreparer(charset: Charset): Preparer[Message, Int] = {
    case tm: TextMessage  => tm.getText.getBytes(charset).length.success
    case bm: BytesMessage => bm.getBodyLength.toInt.success
    case _                => "Unsupported message type".failure
  }

  private val JsonPreparerErrorMapper: String => String = "Could not parse response into a JSON: " + _

  private def jsonPreparer(jsonParsers: JsonParsers): Preparer[Message, JsonNode] =
    replyMessage =>
      safely(JsonPreparerErrorMapper) {
        replyMessage match {
          case tm: TextMessage          => jsonParsers.safeParse(tm.getText)
          case bm: CachingMessage.Bytes => jsonParsers.safeParse(new ByteArrayInputStream(bm.bytes))
          case _                        => "Unsupported message type".failure
        }
      }

  private def stringBodyPreparer(charset: Charset): Preparer[Message, String] = {
    case tm: TextMessage          => tm.getText.success
    case bm: CachingMessage.Bytes => new String(bm.bytes, charset).success
    case _                        => "Unsupported message type".failure
  }

  def bodyString(charset: Charset): CheckMaterializer[BodyStringCheckType, JmsCheck, Message, String] =
    new JmsCheckMaterializer(stringBodyPreparer(charset))

  def bodyBytes(charset: Charset): CheckMaterializer[BodyBytesCheckType, JmsCheck, Message, Array[Byte]] =
    new JmsCheckMaterializer(bodyBytesPreparer(charset))

  def bodyLength(charset: Charset): CheckMaterializer[BodyBytesCheckType, JmsCheck, Message, Int] =
    new JmsCheckMaterializer(bodyLengthPreparer(charset))

  def substring(charset: Charset): CheckMaterializer[SubstringCheckType, JmsCheck, Message, String] =
    new JmsCheckMaterializer(stringBodyPreparer(charset))

  def jmesPath(jsonParsers: JsonParsers): CheckMaterializer[JmesPathCheckType, JmsCheck, Message, JsonNode] =
    new JmsCheckMaterializer(jsonPreparer(jsonParsers))

  def jsonPath(jsonParsers: JsonParsers): CheckMaterializer[JsonPathCheckType, JmsCheck, Message, JsonNode] =
    new JmsCheckMaterializer(jsonPreparer(jsonParsers))

  val Xpath: CheckMaterializer[XPathCheckType, JmsCheck, Message, XdmNode] = {
    val errorMapper: String => String = "Could not parse response into a DOM Document: " + _

    val preparer: Preparer[Message, XdmNode] =
      message =>
        safely(errorMapper) {
          message match {
            case tm: TextMessage => XmlParsers.parse(tm.getText).success
            case _               => "Unsupported message type".failure
          }
        }

    new JmsCheckMaterializer(preparer)
  }

  val JmsProperty: CheckMaterializer[JmsPropertyCheckType, JmsCheck, Message, Message] =
    new JmsCheckMaterializer(identityPreparer)
}
