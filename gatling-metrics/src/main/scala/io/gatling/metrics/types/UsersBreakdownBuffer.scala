/**
 * Copyright 2011-2014 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
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
package io.gatling.metrics.types

import io.gatling.core.result.message.{ End, Start }
import io.gatling.core.result.writer.UserMessage

class UsersBreakdownBuffer(val nbUsers: Int) {

  private var _active = 0
  private var activeBuffer = 0
  private var _waiting = nbUsers
  private var _done = 0
  private var doneBuffer = 0

  def add(userMessage: UserMessage) {
    userMessage.event match {
      case Start =>
        _active += 1
        _waiting -= 1

      case End =>
        activeBuffer += 1
        doneBuffer += 1
    }
  }

  def active = {
    _active -= activeBuffer
    activeBuffer = 0
    _active
  }

  def waiting = _waiting

  def done = {
    _done += doneBuffer
    doneBuffer = 0
    _done
  }

}

case class UsersBreakdown(nbUsers: Int, active: Int, waiting: Int, done: Int)
object UsersBreakdown {
  def apply(buf: UsersBreakdownBuffer): UsersBreakdown = UsersBreakdown(buf.nbUsers, buf.active, buf.waiting, buf.done)
}