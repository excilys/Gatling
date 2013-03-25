/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.excilys.com)
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
package io.gatling.charts.component

import io.gatling.charts.config.ChartsFiles.{ GATLING_TEMPLATE_STATISTICS_COMPONENT_URL, GLOBAL_PAGE_NAME }
import io.gatling.charts.template.PageTemplate.TEMPLATE_ENGINE
import io.gatling.core.config.GatlingConfiguration.configuration
import io.gatling.core.result.reader.DataReader.NO_PLOT_MAGIC_VALUE

case class Statistics(name: String, total: Long, success: Long, failure: Long) {

	private def makePrintable(value: Long) = if (value != NO_PLOT_MAGIC_VALUE) value.toString else "-"

	def printableTotal: String = makePrintable(total)

	def printableSuccess: String = makePrintable(success)

	def printableFailure: String = makePrintable(failure)

	def all = List(total, success, failure)
}

case class GroupedCount(name: String, count: Int, percentage: Int)

case class RequestStatistics(name: String,
	path: String,
	numberOfRequestsStatistics: Statistics,
	minResponseTimeStatistics: Statistics,
	maxResponseTimeStatistics: Statistics,
	meanStatistics: Statistics,
	stdDeviationStatistics: Statistics,
	percentiles1: Statistics,
	percentiles2: Statistics,
	groupedCounts: Seq[GroupedCount],
	meanNumberOfRequestsPerSecondStatistics: Statistics) {

	def mkString = {
		val outputName = List(if(name == GLOBAL_PAGE_NAME) name else path)
		List(
			outputName,
			numberOfRequestsStatistics.all,
			minResponseTimeStatistics.all,
			maxResponseTimeStatistics.all,
			meanStatistics.all,
			stdDeviationStatistics.all,
			percentiles1.all,
			percentiles2.all,
			groupedCounts.flatMap(groupedCount => List(groupedCount.name, groupedCount.count, groupedCount.percentage)),
			meanNumberOfRequestsPerSecondStatistics.all).flatten.mkString(configuration.charting.statsTsvSeparator)
	}
}

class StatisticsTextComponent extends Component {

	def getHTMLContent: String = TEMPLATE_ENGINE.layout(GATLING_TEMPLATE_STATISTICS_COMPONENT_URL)

	val getJavascriptContent: String = ""

	val getJavascriptFiles: Seq[String] = Seq.empty
}