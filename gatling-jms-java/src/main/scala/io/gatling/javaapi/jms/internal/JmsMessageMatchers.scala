/*
 * Copyright 2011-2022 GatlingCorp (https://gatling.io)
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

package io.gatling.javaapi.jms.internal

import javax.jms.Message

import io.gatling.javaapi.jms.JmsMessageMatcher

object JmsMessageMatchers {

  def toScala(javaMatcher: JmsMessageMatcher): io.gatling.jms.protocol.JmsMessageMatcher =
    new io.gatling.jms.protocol.JmsMessageMatcher() {
      override def prepareRequest(msg: Message): Unit = javaMatcher.prepareRequest(msg)
      override def requestMatchId(msg: Message): String = javaMatcher.requestMatchId(msg)
      override def responseMatchId(msg: Message): String = javaMatcher.responseMatchId(msg)
    }
}
