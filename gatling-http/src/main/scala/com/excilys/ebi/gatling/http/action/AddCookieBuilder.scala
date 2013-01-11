/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
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
package com.excilys.ebi.gatling.http.action

import com.excilys.ebi.gatling.core.action.builder.ActionBuilder
import com.excilys.ebi.gatling.core.action.system
import com.excilys.ebi.gatling.core.config.ProtocolConfigurationRegistry
import com.excilys.ebi.gatling.core.session.{ Expression, Session }
import com.ning.http.client.Cookie

import akka.actor.{ ActorRef, Props }

object AddCookieBuilder {

	def apply(url: Expression[String], domain: Expression[String], name: Expression[String], value: Expression[String], path: Expression[String]) = {

		val cookie: Expression[Cookie] = (session: Session) =>
			for {
				domain <- domain(session)
				name <- name(session)
				value <- value(session)
				path <- path(session)
			} yield new Cookie(domain, name, value, path, 100000, false)

		new AddCookieBuilder(url, cookie)
	}
}

class AddCookieBuilder(url: Expression[String], cookie: Expression[Cookie]) extends ActionBuilder {

	def build(next: ActorRef, protocolConfigurationRegistry: ProtocolConfigurationRegistry) = system.actorOf(Props(new AddCookie(url, cookie, next)))
}