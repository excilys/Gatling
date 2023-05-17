/*
 * Copyright 2011-2023 GatlingCorp (https://gatling.io)
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

package computerdatabase.advanced

import io.gatling.javaapi.core.*
import io.gatling.javaapi.http.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*

import java.util.concurrent.ThreadLocalRandom

class KotlinAdvancedSimulationStep05: Simulation() {

  val httpProtocol =
    http.baseUrl("http://computer-database.gatling.io")
      .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      .doNotTrackHeader("1")
      .acceptLanguageHeader("en-US,en;q=0.5")
      .acceptEncodingHeader("gzip, deflate")
      .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val feeder = csv("search.csv").random()

  val search =
    exec(http("Home").get("/"))
      .pause(1)
      .feed(feeder)
      .exec(
        http("Search").get("/computers?f=#{searchCriterion}")
          .check(css("a:contains('#{searchComputerName}')", "href").saveAs("computerUrl"))
      )
      .pause(1)
      .exec(http("Select").get("#{computerUrl}").check(status().shouldBe(200)))
      .pause(1)

  val browse =
    // Repeat is a loop resolved at RUNTIME.
    // Note how we force the counter name so we can reuse it.
    repeat(4, "i")
      .on(exec(http("Page #{i}").get("/computers?p=#{i}")).pause(1))

  // Note we should be using a feeder here.
  // Let's demonstrate how we can retry: let's make the request fail randomly and retry a given
  // number of times
  val edit =
    // Let's try at max 2 times
    tryMax(2)
      .on(
        exec(http("Form").get("/computers/new"))
          .pause(1)
          .exec(
            http("Post")
              .post("/computers")
              .formParam("name", "Beautiful Computer")
              .formParam("introduced", "2012-05-30")
              .formParam("discontinued", "")
              .formParam("company", "37")
              .check(
                status().shouldBe { session ->
                  // We do a check on a condition that's been customized with
                  // a lambda. It will be evaluated every time a user executes
                  // the request.
                  200 + ThreadLocalRandom.current().nextInt(2)
                }
              )
          )
      )
      // If the chain didn't finally succeed, have the user exit the whole scenario
      .exitHereIfFailed()

  val users = scenario("Users").exec(search, browse)
  val admins = scenario("Admins").exec(search, browse, edit)

  init {
    setUp(users.injectOpen(rampUsers(10).during(10)), admins.injectOpen(rampUsers(2).during(10)))
      .protocols(httpProtocol)
  }
}
