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
package io.gatling.http.request

import scala.collection.mutable

import org.apache.commons.io.IOUtils

import io.gatling.core.config.GatlingConfiguration.configuration
import io.gatling.core.config.GatlingFiles
import io.gatling.core.session.{ EL, Expression, Session }
import io.gatling.core.validation.Validation
import io.gatling.core.util.IOHelper.withCloseable

object ELFileBodies {

	private val elFileBodiesCache = mutable.Map.empty[String, Validation[Expression[String]]]

	def compileFile(path: String): Validation[Expression[String]] =
		GatlingFiles.requestBodyResource(path)
			.map(resource => withCloseable(resource.inputStream)(IOUtils.toString(_, configuration.core.encoding)))
			.map(EL.compile[String])

	def buildExpression[T](filePath: Expression[String], f: String => T): Expression[T] = (session: Session) =>
		for {
			path <- filePath(session)
			expression <- elFileBodiesCache.getOrElseUpdate(path, compileFile(path))
			body <- expression(session)
		} yield f(body)

	def asString(filePath: Expression[String]) = buildExpression(filePath, identity)

	def asBytes(filePath: Expression[String]) = buildExpression(filePath, _.getBytes(configuration.core.encoding))
}