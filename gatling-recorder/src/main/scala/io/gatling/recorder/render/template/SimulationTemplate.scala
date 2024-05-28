/*
 * Copyright 2011-2024 GatlingCorp (https://gatling.io)
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

package io.gatling.recorder.render.template

import scala.concurrent.duration._

import io.gatling.commons.util.StringHelper.Eol
import io.gatling.recorder.config.RecorderConfiguration
import io.gatling.recorder.render._

private[render] object SimulationTemplate {
  def apply(requestBodies: Map[Int, DumpedBody], responseBodies: Map[Int, DumpedBody], configuration: RecorderConfiguration): SimulationTemplate =
    new SimulationTemplate(
      configuration.core.pkg,
      configuration.core.className,
      configuration.core.format,
      new ProtocolTemplate(configuration),
      new RequestTemplate(requestBodies, responseBodies, configuration)
    )

  private[template] def renderNonBaseUrls(values: Seq[UrlVal], format: RenderingFormat): String = {
    val referenceType = format match {
      case RenderingFormat.Scala | RenderingFormat.Kotlin          => "private val"
      case RenderingFormat.Java11 | RenderingFormat.Java17         => "private String"
      case RenderingFormat.JavaScript | RenderingFormat.TypeScript => "const"
    }

    if (values.isEmpty) {
      ""
    } else {
      values
        .sortBy(_.valName)
        .map(value => s"$referenceType ${value.valName} = ${value.url.protect(format)}${format.lineTermination}")
        .mkString(Eol, s"$Eol$Eol", "")
    }
  }

  private[template] def headersBlockName(id: Int) = s"headers_$id"
}

private[render] class SimulationTemplate(
    packageName: String,
    simulationClassName: String,
    format: RenderingFormat,
    protocolTemplate: ProtocolTemplate,
    requestTemplate: RequestTemplate
) {
  import SimulationTemplate._

  private def renderHeaders(headers: Map[Int, Seq[(String, String)]]) =
    headers
      .map { case (headersBlockIndex, headersBlock) =>
        val headerReference = headersBlockName(headersBlockIndex)
        val protectedHeaders = headersBlock.map { case (name, value) => (name.protect(format), value.protect(format)) }

        format match {
          case RenderingFormat.Scala =>
            protectedHeaders match {
              case Seq((protectedName, protectedValue)) =>
                s"private val $headerReference = Map($protectedName -> $protectedValue)"
              case _ =>
                s"""private val $headerReference = Map(
                   |${protectedHeaders.map { case (protectedName, protectedValue) => s"		$protectedName -> $protectedValue" }.mkString(s",$Eol")}
                   |)""".stripMargin
            }
          case RenderingFormat.Kotlin =>
            protectedHeaders match {
              case Seq((protectedName, protectedValue)) =>
                s"private val $headerReference = mapOf($protectedName to $protectedValue)"
              case _ =>
                s"""private val $headerReference = mapOf(
                   |${protectedHeaders.map { case (protectedName, protectedValue) => s"  $protectedName to $protectedValue" }.mkString(s",$Eol")}
                   |)""".stripMargin
            }
          case RenderingFormat.Java11 | RenderingFormat.Java17 =>
            protectedHeaders match {
              case Seq((protectedName, protectedValue)) =>
                s"private Map<CharSequence, String> $headerReference = Map.of($protectedName, $protectedValue);"
              case _ =>
                s"""private Map<CharSequence, String> $headerReference = Map.ofEntries(
                   |${protectedHeaders.map { case (protectedName, protectedValue) => s"  Map.entry($protectedName, $protectedValue)" }.mkString(s",$Eol")}
                   |);""".stripMargin
            }
          case RenderingFormat.JavaScript | RenderingFormat.TypeScript =>
            s"""const $headerReference = {
               |${protectedHeaders.map { case (protectedName, protectedValue) => s"  $protectedName: $protectedValue" }.mkString(s",$Eol")}
               |};""".stripMargin
          case _ =>
            s"""Map<CharSequence, String> $headerReference = new HashMap<>();
               |${protectedHeaders
                .map { case (protectedName, protectedValue) => s"$headerReference.put($protectedName, $protectedValue);" }
                .mkString(Eol)}""".stripMargin
        }
      }
      .mkString(Eol, s"$Eol$Eol", "")

  private def renderScenarioElement(se: HttpTrafficElement, extractedUris: ExtractedUris) = se match {
    case TagElement(text) => s"// $text"
    case PauseElement(duration) =>
      val pauseString =
        if (duration > 1.second) {
          duration.toSeconds.toString
        } else {
          format match {
            case RenderingFormat.Scala                                   => s"${duration.toMillis}.milliseconds"
            case RenderingFormat.JavaScript | RenderingFormat.TypeScript => s"""{ amount: ${duration.toMillis}, unit: "milliseconds" }"""
            case _                                                       => s"Duration.ofMillis(${duration.toMillis})"
          }
        }

      s"pause($pauseString)"
    case request: RequestElement =>
      s"${requestTemplate.render(simulationClassName, request, extractedUris)}".stripMargin
  }

  private def renderScenario(extractedUris: ExtractedUris, elements: Seq[HttpTrafficElement]) = {
    val scenarioReferenceType = format match {
      case RenderingFormat.Scala | RenderingFormat.Kotlin          => "private val"
      case RenderingFormat.Java11 | RenderingFormat.Java17         => "private ScenarioBuilder"
      case RenderingFormat.JavaScript | RenderingFormat.TypeScript => "const"
    }

    val scenarioElements = elements
      .map(renderScenarioElement(_, extractedUris))
      .mkString(s",$Eol")

    s"""$scenarioReferenceType scn = scenario("$simulationClassName")
       |  .exec(
       |${s"$scenarioElements".indent(4)}
       |  )${format.lineTermination}""".stripMargin
  }

  def render(
      protocol: ProtocolDefinition,
      headers: Map[Int, Seq[(String, String)]],
      scenarioElements: Seq[HttpTrafficElement]
  ): String = {
    val extractedUris = ExtractedUris(scenarioElements, format)
    val nonBaseUrls: Seq[UrlVal] = extractedUris.nonBaseUrls(protocol.baseUrl)

    format match {
      case RenderingFormat.Scala =>
        s"""${if (packageName.nonEmpty) s"package $packageName$Eol" else ""}
           |import scala.concurrent.duration._
           |
           |import io.gatling.core.Predef._
           |import io.gatling.http.Predef._
           |import io.gatling.jdbc.Predef._
           |
           |class $simulationClassName extends Simulation {
           |
           |${protocolTemplate.render(protocol).indent(2)}
           |${renderHeaders(headers).indent(2)}
           |${renderNonBaseUrls(nonBaseUrls, format).indent(2)}
           |
           |${renderScenario(extractedUris, scenarioElements).indent(2)}
           |
           |	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
           |}
           |""".stripMargin

      case RenderingFormat.Kotlin =>
        s"""${if (packageName.nonEmpty) s"package $packageName$Eol" else ""}
           |import java.time.Duration
           |
           |import io.gatling.javaapi.core.*
           |import io.gatling.javaapi.http.*
           |import io.gatling.javaapi.jdbc.*
           |
           |import io.gatling.javaapi.core.CoreDsl.*
           |import io.gatling.javaapi.http.HttpDsl.*
           |import io.gatling.javaapi.jdbc.JdbcDsl.*
           |
           |class $simulationClassName : Simulation() {
           |
           |${protocolTemplate.render(protocol).indent(2)}
           |${renderHeaders(headers).indent(2)}
           |${renderNonBaseUrls(nonBaseUrls, format).indent(2)}
           |
           |${renderScenario(extractedUris, scenarioElements).indent(2)}
           |
           |  init {
           |	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol)
           |  }
           |}
           |""".stripMargin

      case RenderingFormat.Java11 | RenderingFormat.Java17 =>
        s"""${if (packageName.nonEmpty) s"package $packageName;$Eol" else ""}
           |import java.time.Duration;
           |import java.util.*;
           |
           |import io.gatling.javaapi.core.*;
           |import io.gatling.javaapi.http.*;
           |import io.gatling.javaapi.jdbc.*;
           |
           |import static io.gatling.javaapi.core.CoreDsl.*;
           |import static io.gatling.javaapi.http.HttpDsl.*;
           |import static io.gatling.javaapi.jdbc.JdbcDsl.*;
           |
           |public class $simulationClassName extends Simulation {
           |
           |${protocolTemplate.render(protocol).indent(2)}
           |${renderHeaders(headers).indent(2)}
           |${renderNonBaseUrls(nonBaseUrls, format).indent(2)}
           |
           |${renderScenario(extractedUris, scenarioElements).indent(2)}
           |
           |  {
           |	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
           |  }
           |}
           |""".stripMargin

      case RenderingFormat.JavaScript | RenderingFormat.TypeScript =>
        s"""import { simulation, scenario, pause, atOnceUsers, RawFileBody } from "@gatling.io/core";
           |import { http, status } from "@gatling.io/http";
           |
           |${protocolTemplate.render(protocol).indent(2)}
           |${renderHeaders(headers).indent(2)}
           |${renderNonBaseUrls(nonBaseUrls, format).indent(2)}
           |
           |${renderScenario(extractedUris, scenarioElements).indent(2)}
           |
           |export default simulation((setUp) => {
           |  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
           |});
           |""".stripMargin
    }
  }
}
