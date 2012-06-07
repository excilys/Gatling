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
package com.excilys.ebi.gatling.charts.result.reader

import scala.tools.nsc.io.Path
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import com.excilys.ebi.gatling.core.config.GatlingConfiguration

@RunWith(classOf[JUnitRunner])
class FileDataReaderSpec extends Specification {

	val advancedSimulationLog = Path("src/test/resources/simulation_AdvancedExampleSimulation.log")

	//The file data reader needs to know the encoding, use default conf.
	GatlingConfiguration.setUp(None, None, None, None, None)

	var advancedDataReader: FileDataReader = null

	"FileDataReader" should {

		"be able to read a simulation_AdvancedExampleSimulation.log file" in {
			advancedDataReader = new FileDataReader(advancedSimulationLog)
			advancedDataReader must not be null
		}

		"find the two correct scenarios" in {
			advancedDataReader.scenarioNames must haveTheSameElementsAs(List("Scenario name", "Other Scenario Name"))
		}
	}

}