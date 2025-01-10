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

package io.gatling.jakarta.jms.action

import io.gatling.core.action.Action
import io.gatling.core.structure.ScenarioContext
import io.gatling.jakarta.jms.request.JmsAttributes

final class SendBuilder(attributes: JmsAttributes) extends JmsActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val jmsComponents = components(ctx.protocolComponentsRegistry)

    new Send(
      attributes,
      jmsComponents.jmsProtocol,
      jmsComponents.jmsConnectionPool,
      ctx.coreComponents.statsEngine,
      ctx.coreComponents.clock,
      next,
      ctx.coreComponents.throttler.filter(_ => ctx.throttled)
    )
  }
}
