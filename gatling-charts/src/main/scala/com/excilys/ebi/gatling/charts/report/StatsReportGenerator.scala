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

import scala.collection.immutable.ListMap

import com.excilys.ebi.gatling.charts.component.{ ComponentLibrary, RequestStatistics, Statistics }
import com.excilys.ebi.gatling.charts.config.ChartsFiles.{ GLOBAL_PAGE_NAME, jsStatsFile, tsvStatsFile,jsonStatsFile }
import com.excilys.ebi.gatling.charts.template.{StatsJsonTemplate, StatsJsTemplate, StatsTsvTemplate}
import com.excilys.ebi.gatling.core.result.message.RequestStatus.{KO, OK}
import com.excilys.ebi.gatling.core.result.reader.DataReader

class StatsReportGenerator(runOn: String, dataReader: DataReader, componentLibrary: ComponentLibrary) {

	def generate: Map[String, RequestStatistics] = {
		val criteria: List[(String, Option[String])] = (GLOBAL_PAGE_NAME, None) :: dataReader.requestNames.map(name => (name, Some(name))).toList

		val stats: ListMap[String, RequestStatistics] = criteria.map {
			case (name, requestName) =>
				
				val total = dataReader.generalStats(None, requestName)
				val ok = dataReader.generalStats(Some(OK), requestName)
				val ko = dataReader.generalStats(Some(KO), requestName)

				val numberOfRequestsStatistics = Statistics("numberOfRequests", total.count, ok.count, ko.count)
				val minResponseTimeStatistics = Statistics("minResponseTime", total.min, ok.min, ko.min)
				val maxResponseTimeStatistics = Statistics("maxResponseTime", total.max, ok.max, ko.max)
				val meanResponseTimeStatistics = Statistics("meanResponseTime", total.mean, ok.mean, ko.mean)
				val stdDeviationStatistics = Statistics("stdDeviation", total.stdDev, ok.stdDev, ko.stdDev)
				val percentiles1 = Statistics("percentiles1", total.percentile1, ok.percentile1, ko.percentile1)
				val percentiles2 = Statistics("percentiles2", total.percentile2, ok.percentile2, ko.percentile2)
				val meanNumberOfRequestsPerSecondStatistics = Statistics("meanNumberOfRequestsPerSecond", total.meanRequestsPerSec, ok.meanRequestsPerSec, ko.meanRequestsPerSec)

				val groupedCounts = dataReader
					.numberOfRequestInResponseTimeRange(requestName)
					.map {
					case (name, count) => (name, count, count * 100 / total.count)
				}

				(name -> RequestStatistics(name, numberOfRequestsStatistics, minResponseTimeStatistics, maxResponseTimeStatistics, meanResponseTimeStatistics, stdDeviationStatistics, percentiles1, percentiles2, groupedCounts, meanNumberOfRequestsPerSecondStatistics))
		}(collection.breakOut)

		new TemplateWriter(jsStatsFile(runOn)).writeToFile(new StatsJsTemplate(stats).getOutput)
		new TemplateWriter(jsonStatsFile(runOn)).writeToFile(new StatsJsonTemplate(stats).getOutput)
		new TemplateWriter(tsvStatsFile(runOn)).writeToFile(new StatsTsvTemplate(stats).getOutput)

		stats
	}
}
