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
package com.excilys.ebi.gatling.app

import java.lang.System.currentTimeMillis
import java.util.{ Map => JMap }

import com.excilys.ebi.gatling.app.CommandLineConstants._
import com.excilys.ebi.gatling.charts.report.ReportsGenerator
import com.excilys.ebi.gatling.core.ConfigurationConstants._
import com.excilys.ebi.gatling.core.config.{ GatlingConfiguration, GatlingFiles }
import com.excilys.ebi.gatling.core.runner.{ Runner, Selection }
import com.excilys.ebi.gatling.core.scenario.configuration.Simulation
import grizzled.slf4j.Logging
import scopt.OptionParser
import com.excilys.ebi.gatling.core.config.GatlingPropertiesBuilder

/**
 * Object containing entry point of application
 */
object Gatling extends Logging {

	/**
	 * Entry point of Application
	 *
	 * @param args Arguments of the main method
	 */
	def main(args: Array[String]) {

		val props = new GatlingPropertiesBuilder

		val cliOptsParser = new OptionParser("gatling") {
			opt(CLI_NO_REPORTS, CLI_NO_REPORTS_ALIAS, "Runs simulation but does not generate reports", { v: String => props.noReports })
			opt(CLI_REPORTS_ONLY, CLI_REPORTS_ONLY_ALIAS, "<directoryName>", "Generates the reports for the simulation in <directoryName>", { v: String => props.reportsOnly(v) })
			opt(CLI_DATA_FOLDER, CLI_DATA_FOLDER_ALIAS, "<directoryPath>", "Uses <directoryPath> as the absolute path of the directory where feeders are stored", { v: String => props.dataDirectory(v) })
			opt(CLI_RESULTS_FOLDER, CLI_RESULTS_FOLDER_ALIAS, "<directoryPath>", "Uses <directoryPath> as the absolute path of the directory where results are stored", { v: String => props.resultsDirectory(v) })
			opt(CLI_REQUEST_BODIES_FOLDER, CLI_REQUEST_BODIES_FOLDER_ALIAS, "<directoryPath>", "Uses <directoryPath> as the absolute path of the directory where request bodies are stored", { v: String => props.requestBodiesDirectory(v) })
			opt(CLI_SIMULATIONS_FOLDER, CLI_SIMULATIONS_FOLDER_ALIAS, "<directoryPath>", "Uses <directoryPath> to discover simulations that could be run", { v: String => props.sourcesDirectory(v) })
			opt(CLI_SIMULATIONS_BINARIES_FOLDER, CLI_SIMULATIONS_BINARIES_FOLDER_ALIAS, "<directoryPath>", "Uses <directoryPath> to discover already compiled simulations", { v: String => props.binariesDirectory(v) })
			opt(CLI_SIMULATIONS, CLI_SIMULATIONS_ALIAS, "<classNamesList>", "Runs the <classNamesList> simulations sequentially", { v: String => props.classes(v) })
			opt(CLI_RUN_NAME, CLI_RUN_NAME_ALIAS, "<runName>", "Use <runName> for the output directory", { v: String => props.runName(v) })
		}

		// if arguments are incorrect, usage message is displayed
		if (cliOptsParser.parse(args))
			fromMap(props.build)
	}

	def fromMap(props: JMap[String, Any]) {
		GatlingConfiguration.setUp(props)
		new Gatling().start
	}
}

class Gatling extends Logging {

	import GatlingConfiguration.configuration

	def start {
		val runUuids = GatlingFiles.reportsOnlyDirectory
			.map(List(_))
			.getOrElse {
				val simulations = GatlingFiles.binariesDirectory
					.map( // expect simulations to have been pre-compiled (ex: IDE)
						SimulationClassLoader.fromClasspathBinariesDirectory(_))
					.getOrElse(SimulationClassLoader.fromSourcesDirectory(GatlingFiles.sourcesDirectory))
					.simulationClasses(configuration.simulation.classes)
					.sortWith(_.getName < _.getName)

				val selection = configuration.simulation.classes match {
					case Nil => interactiveSelect(simulations)
					case simulation => new Selection(simulations, configuration.simulation.runName, configuration.simulation.runDescription)
				}

				new Runner(selection).run
			}

		if (!configuration.charting.noReports)
			runUuids.foreach(generateReports)
	}

	private def interactiveSelect(simulations: List[Class[Simulation]]): Selection = {

		val simulation = selectSimulationClass(simulations)

		println("Select run id (default is '" + configuration.simulation.runName + "'). Accepted characters are a-z, A-Z, 0-9, - and _")
		val runId = {
			val userInput = Console.readLine.trim

			if (!userInput.matches("[\\w-_]*"))
				throw new IllegalArgumentException(userInput + " contains illegal characters")

			if (!userInput.isEmpty) userInput else configuration.simulation.runName
		}

		println("Select run description (optional)")
		val runDescription = Console.readLine.trim

		new Selection(List(simulation), runId, runDescription)
	}

	private def selectSimulationClass(simulations: List[Class[Simulation]]): Class[Simulation] = {
	
		val selection = simulations.size match {
			case 0 =>
				// If there is no simulation file
				println("There is no simulation script. Please check that your scripts are in user-files/simulations")
				sys.exit
			case 1 =>
				// If there is only one simulation file
				info("There is only one simulation, executing it.")
				0
			case size =>
				println("Choose a simulation number:")
				for ((simulation, index) <- simulations.zipWithIndex) {
					println("     [" + index + "] " + simulation.getName)
				}
				Console.readInt
		}

		val validRange = 0 until simulations.size
		if (validRange contains selection)
			simulations(selection)
		else {
			println("Invalid selection, must be in " + validRange)
			selectSimulationClass(simulations)
		}
	}

	/**
	 * This method call the statistics module to generate the charts and statistics
	 *
	 * @param runUuid The directory from which the simulation.log will be parsed
	 * @return Nothing
	 */
	private def generateReports(runUuid: String) {
		println("Generating reports...")
		val start = currentTimeMillis
		try {
			val indexFile = ReportsGenerator.generateFor(runUuid)
			println("Reports generated in " + (currentTimeMillis - start) / 1000 + "s.")
			println("Please open the following file : " + indexFile)

		} catch {
			case e => error("Reports weren't generated", e)
		}
	}
}
