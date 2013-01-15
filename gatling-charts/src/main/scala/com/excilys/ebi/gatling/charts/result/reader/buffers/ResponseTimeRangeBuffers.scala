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
package com.excilys.ebi.gatling.charts.result.reader.buffers

import scala.collection.mutable

import com.excilys.ebi.gatling.charts.result.reader.ActionRecord
import com.excilys.ebi.gatling.core.result.Group
import com.excilys.ebi.gatling.core.result.message.{ KO, RequestStatus }

trait ResponseTimeRangeBuffers {

	val responseTimeRangeBuffers: mutable.Map[BufferKey, ResponseTimeRangeBuffer] = mutable.HashMap.empty

	def getResponseTimeRangeBuffers(requestName: Option[String], group: Option[Group]): ResponseTimeRangeBuffer = responseTimeRangeBuffers.getOrElseUpdate(computeKey(requestName, group, None), new ResponseTimeRangeBuffer)

	def updateResponseTimeRangeBuffer(record: ActionRecord, group: Option[Group]) {
		getResponseTimeRangeBuffers(Some(record.request), group).update(record.responseTime, record.status)
		getResponseTimeRangeBuffers(None, None).update(record.responseTime, record.status)
	}

	def updateGroupResponseTimeRangeBuffer(duration: Int, group: Group, status: RequestStatus) {
		getResponseTimeRangeBuffers(None, Some(group)).update(duration, status)
	}

	class ResponseTimeRangeBuffer {

		import com.excilys.ebi.gatling.core.config.GatlingConfiguration.configuration

		var low = 0
		var middle = 0
		var high = 0
		var ko = 0

		def update(time: Int, status: RequestStatus) {

			if (status == KO) ko += 1
			else if (time < configuration.charting.indicators.lowerBound) low += 1
			else if (time > configuration.charting.indicators.higherBound) high += 1
			else middle += 1
		}
	}

}