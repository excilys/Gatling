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
package io.gatling.core.session

import scala.reflect.ClassTag

import io.gatling.core.util.TypeHelper
import io.gatling.core.validation.{ FailureWrapper, Validation }

import grizzled.slf4j.Logging

/**
 * Session class companion
 */
object Session extends Logging {

	val GATLING_PRIVATE_ATTRIBUTE_PREFIX = "gatling."

	val TIME_SHIFT_KEY = GATLING_PRIVATE_ATTRIBUTE_PREFIX + "core.timeShift"

	val FAILED_KEY = GATLING_PRIVATE_ATTRIBUTE_PREFIX + "core.failed"

	val MUST_EXIT_ON_FAIL_KEY = GATLING_PRIVATE_ATTRIBUTE_PREFIX + "core.mustExitOnFailed"

}

/**
 * Session class representing the session passing through a scenario for a given user
 *
 * This session stores all needed data between requests
 *
 * @constructor creates a new session
 * @param scenarioName the name of the current scenario
 * @param userId the id of the current user
 * @param data the map that stores all values needed
 */
case class Session(scenarioName: String, userId: Int, attributes: Map[String, Any] = Map.empty) {

	def apply(name: String) = attributes(name)

	def get(key: String): Option[Any] = attributes.get(key)

	def getAs[T](key: String): Option[T] = attributes.get(key).map(_.asInstanceOf[T])

	def safeGetAs[T: ClassTag](key: String): Validation[T] = attributes.get(key).map(TypeHelper.as[T](_)).getOrElse(undefinedSessionAttributeMessage(key).failure[T])

	def set(newAttributes: Map[String, Any]) = copy(attributes = attributes ++ newAttributes)

	def set(key: String, value: Any) = copy(attributes = attributes + (key -> value))

	def remove(key: String) = if (contains(key)) copy(attributes = attributes - key) else this

	def contains(attributeKey: String) = attributes.contains(attributeKey)

	def setFailed: Session = set(Session.FAILED_KEY, "")

	def clearFailed: Session = remove(Session.FAILED_KEY)

	def isFailed: Boolean = contains(Session.FAILED_KEY)

	def setMustExitOnFail: Session = set(Session.MUST_EXIT_ON_FAIL_KEY, "")

	def isMustExitOnFail: Boolean = contains(Session.MUST_EXIT_ON_FAIL_KEY)

	def clearMustExitOnFail: Session = remove(Session.MUST_EXIT_ON_FAIL_KEY)

	def shouldExitBecauseFailed: Boolean = isFailed && isMustExitOnFail

	private[gatling] def setTimeShift(timeShift: Long): Session = set(Session.TIME_SHIFT_KEY, timeShift)

	private[gatling] def increaseTimeShift(time: Long): Session = setTimeShift(time + getTimeShift)

	private[gatling] def getTimeShift: Long = getAs[Long](Session.TIME_SHIFT_KEY).getOrElse(0L)
}
