/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.http.check.body

import com.typesafe.scalalogging.slf4j.Logging
import io.gatling.core.check.Preparer
import io.gatling.core.check.extractor.jsonpath.JaywayJsonPathExtractors
import io.gatling.core.config.{ Gatling, Jayway }
import io.gatling.core.config.GatlingConfiguration.configuration
import io.gatling.core.session.Expression
import io.gatling.core.validation.{ FailureWrapper, SuccessWrapper }
import io.gatling.http.check.{ HttpCheckBuilders, HttpMultipleCheckBuilder }
import io.gatling.http.response.Response
import io.gatling.core.check.extractor.jsonpath.GatlingJsonPathExtractors
import io.gatling.core.check.extractor.jsonpath.JsonPathExtractors

object HttpBodyJsonPathCheckBuilder extends Logging {

	class HttpBodyJsonPathCheckBuilder(extractor: JsonPathExtractors) {
		val preparer: Preparer[Response, Any] = (response: Response) =>
			try {
				extractor.parse(response.getResponseBodyAsBytes).success
			} catch {
				case e: Exception =>
					val message = s"Could not parse response into a JSON object: ${e.getMessage}"
					logger.info(message, e)
					message.failure
			}

		def jsonPath(expression: Expression[String]) = new HttpMultipleCheckBuilder[Any, String, String](
			HttpCheckBuilders.bodyCheckFactory,
			preparer,
			extractor.extractOne,
			extractor.extractMultiple,
			extractor.count,
			expression)
	}

	val gatlingJsonPathExtractor = new HttpBodyJsonPathCheckBuilder(GatlingJsonPathExtractors)
	val jaywayJsonPathExtractor = new HttpBodyJsonPathCheckBuilder(JaywayJsonPathExtractors)

	def jsonPath(expression: Expression[String]): HttpMultipleCheckBuilder[_, String, String] =
		configuration.core.extract.jsonPath.engine match {
			case Gatling => gatlingJsonPathExtractor.jsonPath(expression)
			case Jayway => jaywayJsonPathExtractor.jsonPath(expression)
		}
}
