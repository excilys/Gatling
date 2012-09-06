/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.metrics

import types.{RequestMetric, UserMetric}

import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.mutable

import com.excilys.ebi.gatling.core.action.EndAction.END_OF_SCENARIO
import com.excilys.ebi.gatling.core.action.StartAction.START_OF_SCENARIO
import com.excilys.ebi.gatling.core.config.GatlingConfiguration.configuration
import com.excilys.ebi.gatling.core.result.message._
import com.excilys.ebi.gatling.core.result.writer.DataWriter
import com.excilys.ebi.gatling.core.util.StringHelper.END_OF_LINE
import com.excilys.ebi.gatling.core.util.TimeHelper.nowMillis
import MetricsDataWriter.{DOT, SPACE}
import com.excilys.ebi.gatling.core.result.terminator.Terminator


import java.net.Socket
import java.io.{BufferedWriter, IOException, OutputStreamWriter}
import java.util.{TimerTask, Timer, HashMap}

import org.apache.commons.io.IOUtils.closeQuietly
import com.excilys.ebi.gatling.core.result.message.RunRecord
import com.excilys.ebi.gatling.core.result.message.ShortScenarioDescription
import com.excilys.ebi.gatling.core.result.message.RequestRecord

object MetricsDataWriter {

	val SPACE = " "
	val DOT = "."
}

class MetricsDataWriter extends DataWriter {

	private var simulationName: String = _
	private val allRequests = new RequestMetric
	private val perRequest: mutable.Map[String, RequestMetric] = new HashMap[String, RequestMetric]
	private var allUsers: UserMetric = _
	private val usersPerScenario: mutable.Map[String, UserMetric] = new HashMap[String, UserMetric]
	private var timer: Timer = _
	private var writer: BufferedWriter = _
	private var socket: Socket = _

	def onInitializeDataWriter(runRecord: RunRecord, scenarios: Seq[ShortScenarioDescription]) {
		simulationName = runRecord.simulationClassSimpleName
		allUsers = new UserMetric(scenarios.map(_.nbUsers).sum)
		scenarios.foreach(scenario => usersPerScenario.+=((scenario.name, new UserMetric(scenario.nbUsers))))
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
		timer = new Timer(true)
		timer.scheduleAtFixedRate(new SendToGraphiteTask, 0, 1000)
		socket = new Socket(configuration.graphite.host, configuration.graphite.port)
	}

	def onRequestRecord(requestRecord: RequestRecord) {
		//Update request metrics
		val requestName = requestRecord.requestName
		if (requestName != START_OF_SCENARIO && requestName != END_OF_SCENARIO) {
			val metric = perRequest.getOrElseUpdate(requestName, new RequestMetric)
			metric.update(requestRecord)
		}
		allRequests.update(requestRecord)
		// Update sessions metrics
		usersPerScenario(requestRecord.scenarioName).update(requestRecord)
		allUsers.update(requestRecord)
	}

	def onFlushDataWriter {
		socket.close
	}

	def uninitialized2: Receive = {
		case InitializeDataWriter(runRecord, scenarios) =>

			Terminator.registerDataWriter(self)
			onInitializeDataWriter(runRecord, scenarios)
			context.become(initialized)
	}

	def initialized2: Receive = {
		case requestRecord: RequestRecord => onRequestRecord(requestRecord)
		case SendToGraphite => sendMetricsToGraphite
		case FlushDataWriter =>
			try {
				onFlushDataWriter
			} finally {
				context.unbecome
				sender ! true
			}
	}

	override def receive = uninitialized2

//	override def initialized: Receive = super.initialized orElse{
//		case SendToGraphite => sendMetricsToGraphite
//	}

//	override def initialized: Receive = {
//		case requestRecord: RequestRecord => onRequestRecord(requestRecord)
//		case SendToGraphite => sendMetricsToGraphite
//		case FlushDataWriter =>
//			try {
//				onFlushDataWriter
//			} finally {
//				context.unbecome
//				sender ! true
//			}
//		case _ =>
//	}

	def sendToGraphite(header: String, valueName: String, value: Long, epoch: Long) {
		writer.write(header)
		writer.write(DOT)
		writer.write(sanitizeString(valueName))
		writer.write(SPACE)
		writer.write(value.toString)
		writer.write(SPACE)
		writer.write(epoch.toString)
		writer.write(END_OF_LINE)
		writer.flush
	}

	def sanitizeString(s: String) = s.replace(' ', '-')

	def formatUserMetric(scenarioName: String, userMetric: UserMetric, epoch: Long) = {
		val header = simulationName + ".users." + sanitizeString(scenarioName)
		sendToGraphite(header, "active", userMetric.getActive, epoch)
		sendToGraphite(header, "waiting", userMetric.getWaiting, epoch)
		sendToGraphite(header, "done", userMetric.getDone, epoch)
	}

	def formatRequestMetric(requestName: String, requestMetric: RequestMetric, epoch: Long) = {
		val header = simulationName + DOT + sanitizeString(requestName)
		val percentiles1 = configuration.charting.indicators.percentile1
		val percentiles2 = configuration.charting.indicators.percentile2
		sendToGraphite(header, "all.count", requestMetric.getCount.all, epoch)
		sendToGraphite(header, "all.max", requestMetric.getMax.all, epoch)
		sendToGraphite(header, "all.percentiles" + percentiles1, requestMetric.getPercentiles.all.getQuantile(percentiles1), epoch)
		sendToGraphite(header, "all.percentiles" + percentiles2, requestMetric.getPercentiles.all.getQuantile(percentiles2), epoch)
		sendToGraphite(header, "ko.count", requestMetric.getCount.ko, epoch)
		sendToGraphite(header, "ko.max", requestMetric.getMax.ko, epoch)
		sendToGraphite(header, "ko.percentiles" + percentiles1, requestMetric.getPercentiles.ko.getQuantile(percentiles1), epoch)
		sendToGraphite(header, "ko.percentiles" + percentiles2, requestMetric.getPercentiles.ko.getQuantile(percentiles2), epoch)
		sendToGraphite(header, "ok.count", requestMetric.getCount.ok, epoch)
		sendToGraphite(header, "ok.max", requestMetric.getMax.ok, epoch)
		sendToGraphite(header, "ok.percentiles" + percentiles1, requestMetric.getPercentiles.ok.getQuantile(percentiles1), epoch)
		sendToGraphite(header, "ok.percentiles" + percentiles2, requestMetric.getPercentiles.ok.getQuantile(percentiles2), epoch)
	}

	def sendMetricsToGraphite {
		try {
			if (writer == null) writer = new BufferedWriter(new OutputStreamWriter(new Socket(configuration.graphite.host, configuration.graphite.port).getOutputStream))
			val epoch = nowMillis / 1000
			formatUserMetric("allUsers", allUsers, epoch)
			usersPerScenario.foreach {
				case (scenarioName, userMetric) => formatUserMetric(scenarioName, userMetric, epoch)
			}
			formatRequestMetric("allRequests", allRequests, epoch)
			perRequest.foreach {
				case (requestName, requestMetric) => formatRequestMetric(requestName, requestMetric, epoch)
			}
		} catch {
			case e: IOException => {
				error("Error writing to Graphite", e)
				closeQuietly(writer)
				writer = null
			}
		}
	}
	private class SendToGraphiteTask extends TimerTask {
		def run() {
			self ! SendToGraphite
//			sendMetricsToGraphite
		}
	}

}

case object SendToGraphite

