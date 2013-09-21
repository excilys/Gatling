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
package io.gatling.core.action.builder

import akka.actor.ActorRef
import io.gatling.core.action.AkkaDefaults
import io.gatling.core.config.ProtocolRegistry

/**
 * Top level abstraction for components in charge of building Actions
 */
trait ActionBuilder extends AkkaDefaults {

	/**
	 * @param next the Action that will be chained with the Action build by this builder
	 * @param protocolRegistry
	 * @return the built Action
	 */
	private[gatling] def build(next: ActorRef, protocolRegistry: ProtocolRegistry): ActorRef
}
