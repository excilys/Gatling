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
package io.gatling.core.structure

import scala.concurrent.duration.Duration
import io.gatling.core.action.UserEnd
import io.gatling.core.action.builder.{ ActionBuilder, UserStartBuilder }
import io.gatling.core.config.{ Protocol, ProtocolRegistry }
import io.gatling.core.pause.{ Constant, Custom, Disabled, Exponential, PauseProtocol, PauseType, UniformDuration, UniformPercentage }
import io.gatling.core.scenario.{ InjectionProfile, InjectionStep, Scenario }
import io.gatling.core.session.Expression
import io.gatling.core.controller.throttle.ThrottlingProtocol

/**
 * The scenario builder is used in the DSL to define the scenario
 *
 * @param name the name of the scenario
 * @param actionBuilders the list of all the actions that compose the scenario
 */
case class ScenarioBuilder(name: String, actionBuilders: List[ActionBuilder] = List(UserStartBuilder)) extends AbstractStructureBuilder[ScenarioBuilder] {

	private[core] def newInstance(actionBuilders: List[ActionBuilder]) = copy(actionBuilders = actionBuilders)

	private[core] def getInstance = this

	def inject(is: InjectionStep, iss: InjectionStep*) = new ProfiledScenarioBuilder(this, InjectionProfile(is +: iss))
}

case class ProfiledScenarioBuilder(scenarioBuilder: ScenarioBuilder, injectionProfile: InjectionProfile, protocols: List[Protocol] = Nil) {

	def protocols(protocol: Protocol, protocols: Protocol*) = copy(protocols = protocol :: protocols.toList)

	def disablePauses = pauses(Disabled)
	def constantPauses = pauses(Constant)
	def exponentialPauses = pauses(Exponential)
	def customPauses(custom: Expression[Long]) = pauses(Custom(custom))
	def uniform(plusOrMinus: Double) = pauses(UniformPercentage(plusOrMinus))
	def uniform(plusOrMinus: Duration) = pauses(UniformDuration(plusOrMinus))
	def pauses(pauseType: PauseType) = protocols(PauseProtocol(pauseType))

	def throttle(maxRps: Int): ProfiledScenarioBuilder = protocols(ThrottlingProtocol(_ => maxRps))

	/**
	 * @param protocolRegistry
	 * @return the scenario
	 */
	private[core] def build(globalProtocols: List[Protocol]): Scenario = {

		val protocolRegistry = {
			val resolvedProtocols = (globalProtocols.groupBy(_.getClass) ++ protocols.groupBy(_.getClass)).values.toSeq.flatten
			ProtocolRegistry(resolvedProtocols)
		}

		val entryPoint = scenarioBuilder.buildChainedActions(UserEnd.userEnd, protocolRegistry)
		new Scenario(scenarioBuilder.name, entryPoint, injectionProfile)
	}
}
