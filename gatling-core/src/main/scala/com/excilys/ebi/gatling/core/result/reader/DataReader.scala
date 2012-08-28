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
package com.excilys.ebi.gatling.core.result.reader

import com.excilys.ebi.gatling.core.config.GatlingConfiguration.configuration
import com.excilys.ebi.gatling.core.result.message.{ RequestStatus, RunRecord }
import com.excilys.ebi.gatling.core.result.message.RequestStatus.RequestStatus

object DataReader {
	val NO_PLOT_MAGIC_VALUE = -1

	def newInstance(runOn: String) = Class.forName(configuration.data.dataReaderClass).asInstanceOf[Class[DataReader]].getConstructor(classOf[String]).newInstance(runOn)
}

abstract class DataReader(runUuid: String) {

	def runRecord: RunRecord

	def requestNames: Seq[String]

	def scenarioNames: Seq[String]

	def numberOfActiveSessionsPerSecond(scenarioName: Option[String] = None): Seq[(Long, Long)]

	def numberOfRequestsPerSecond(status: Option[RequestStatus] = None, requestName: Option[String] = None): Seq[(Long, Long)]

	def numberOfTransactionsPerSecond(status: Option[RequestStatus] = None, requestName: Option[String] = None): Seq[(Long, Long)]

	def responseTimeDistribution(slotsNumber: Int, requestName: Option[String] = None): (Seq[(Long, Long)], Seq[(Long, Long)])

	def percentiles(percentage1: Double, percentage2: Double, status: Option[RequestStatus] = None, requestName: Option[String] = None): (Long, Long)

	def minResponseTime(status: Option[RequestStatus] = None, requestName: Option[String] = None): Long

	def maxResponseTime(status: Option[RequestStatus] = None, requestName: Option[String] = None): Long

	def countRequests(status: Option[RequestStatus] = None, requestName: Option[String] = None): Long

	def meanResponseTime(status: Option[RequestStatus] = None, requestName: Option[String] = None): Long

	def meanLatency(status: Option[RequestStatus] = None, requestName: Option[String] = None): Long

	def meanNumberOfRequestsPerSecond(status: Option[RequestStatus], requestName: Option[String]): Long

	def responseTimeStandardDeviation(status: Option[RequestStatus] = None, requestName: Option[String] = None): Long

	def numberOfRequestInResponseTimeRange(lowerBound: Int, higherBound: Int, requestName: Option[String] = None): Seq[(String, Long)]

	def responseTimeGroupByExecutionStartDate(status: RequestStatus, requestName: String): Seq[(Long, Long)]

	def latencyGroupByExecutionStartDate(status: RequestStatus, requestName: String): Seq[(Long, Long)]

	def requestAgainstResponseTime(status: RequestStatus.RequestStatus, requestName: String): Seq[(Long, Long)]
}
