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
package io.gatling.core.controller.throttle

import scala.concurrent.duration._
import scala.annotation.tailrec

trait ThrottleStep {

	val durationInSec: Long
	def target(previousLastValue: Int): Int
	def rps(time: Long, previousLastValue: Int): Int
}

case object Init extends ThrottleStep {
	val durationInSec = 0L
	def target(previousLastValue: Int) = 0
	def rps(time: Long, previousLastValue: Int) = 0
}

case class Reach(target: Int, duration: Duration) extends ThrottleStep {
	val durationInSec = duration.toSeconds
	def target(previousLastValue: Int) = target
	def rps(time: Long, previousLastValue: Int): Int = ((target - previousLastValue) * time / durationInSec + previousLastValue).toInt
}

case class Hold(duration: Duration) extends ThrottleStep {
	val durationInSec = duration.toSeconds
	def target(previousLastValue: Int) = previousLastValue
	def rps(time: Long, previousLastValue: Int) = previousLastValue
}

case class Jump(target: Int) extends ThrottleStep {
	val durationInSec = 0L
	def target(previousLastValue: Int) = target
	def rps(time: Long, previousLastValue: Int) = 0
}

case class ReachIntermediate(target: Int, history: List[ThrottleStep]) {
	def in(duration: Duration) = ThrottlingBuilder(Reach(target, duration) :: history)
}

trait Throttling {
	def throttlingSteps: List[ThrottleStep]
	def reach(target: Int) = ReachIntermediate(target, throttlingSteps)
	def holdFor(duration: Duration) = ThrottlingBuilder(Hold(duration) :: throttlingSteps)
	def jumpTo(target: Int) = ThrottlingBuilder(Jump(target) :: throttlingSteps)
}

case class ThrottlingBuilder(throttlingSteps: List[ThrottleStep]) extends Throttling {

	def build() = {

		@tailrec
		def valueAt(steps: List[ThrottleStep], pendingTime: Long, previousLastValue: Int): Int = steps match {
			case Nil => previousLastValue
			case head :: tail =>
				if (pendingTime < head.durationInSec)
					head.rps(pendingTime, previousLastValue)
				else
					valueAt(tail, pendingTime - head.durationInSec, head.target(previousLastValue))
		}

		val reversedSteps = throttlingSteps.reverse
		(now: Long) => valueAt(reversedSteps, now, 0)
	}
}
