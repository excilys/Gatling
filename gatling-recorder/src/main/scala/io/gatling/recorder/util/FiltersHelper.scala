/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.excilys.com)
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
package io.gatling.recorder.util

import scala.annotation.tailrec

import java.net.URI

import org.codehaus.plexus.util.SelectorUtils

import io.gatling.recorder.config.RecorderConfiguration.configuration
import io.gatling.recorder.config.Pattern
import io.gatling.recorder.ui.enumeration.{ FilterStrategy, PatternType }

object FiltersHelper {

	private val supportedHttpMethods = Vector("POST", "GET", "PUT", "DELETE", "HEAD")

	def isRequestAccepted(uri: String, method: String): Boolean = {

		def requestMatched = {
			val path = new URI(uri).getPath

			def gatlingPatternToPlexusPattern(pattern: Pattern) = {

				val prefix = pattern.patternType match {
					case PatternType.ANT => SelectorUtils.ANT_HANDLER_PREFIX
					case PatternType.JAVA => SelectorUtils.REGEX_HANDLER_PREFIX
				}

				prefix + pattern.pattern + SelectorUtils.PATTERN_HANDLER_SUFFIX
			}

			@tailrec
			def matchPath(patterns: List[Pattern]): Boolean = patterns match {
				case Nil => false
				case head :: tail =>
					if (SelectorUtils.matchPath(gatlingPatternToPlexusPattern(head), path)) true
					else matchPath(tail)
			}

			matchPath(configuration.filters.patterns)
		}

		def requestPassFilters = configuration.filters.filterStrategy match {
			case FilterStrategy.EXCEPT => !requestMatched
			case FilterStrategy.ONLY => requestMatched
			case FilterStrategy.NONE => true
		}

		supportedHttpMethods.contains(method) && requestPassFilters
	}
}
