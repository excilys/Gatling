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
package com.excilys.ebi.gatling.charts.report

import scala.tools.nsc.io.Path

import com.excilys.ebi.gatling.charts.component.ComponentLibrary
import com.excilys.ebi.gatling.charts.config.ChartsFiles.globalFile
import com.excilys.ebi.gatling.charts.template.PageTemplate
import com.excilys.ebi.gatling.core.config.GatlingFiles.GATLING_ASSETS_JS_PACKAGE
import com.excilys.ebi.gatling.core.config.GatlingFiles.GATLING_ASSETS_STYLE_PACKAGE
import com.excilys.ebi.gatling.core.config.GatlingFiles.jsDirectory
import com.excilys.ebi.gatling.core.config.GatlingFiles.styleDirectory
import com.excilys.ebi.gatling.core.result.reader.DataReader
import com.excilys.ebi.gatling.core.util.ScanHelper.deepCopyPackageContent

import grizzled.slf4j.Logging

object ReportsGenerator extends Logging {

	def generateFor(outputDirectoryName: String): Path = {

		val dataReader = DataReader.newInstance(outputDirectoryName)

		def generateStats = new StatsReportGenerator(outputDirectoryName, dataReader, ComponentLibrary.instance).generate

		def copyAssets {
			deepCopyPackageContent(GATLING_ASSETS_STYLE_PACKAGE, styleDirectory(outputDirectoryName))
			deepCopyPackageContent(GATLING_ASSETS_JS_PACKAGE, jsDirectory(outputDirectoryName))
		}

		if (dataReader.requestPaths.isEmpty) throw new UnsupportedOperationException("There were no requests sent during the simulation, reports won't be generated")

		val reportGenerators =
			List(new AllSessionsReportGenerator(outputDirectoryName, dataReader, ComponentLibrary.instance),
				new GlobalReportGenerator(outputDirectoryName, dataReader, ComponentLibrary.instance),
				new RequestDetailsReportGenerator(outputDirectoryName, dataReader, ComponentLibrary.instance))

		copyAssets
		PageTemplate.setRunInfo(dataReader.runRecord)
		reportGenerators.foreach(_.generate)
		generateStats

		globalFile(outputDirectoryName)
	}
}