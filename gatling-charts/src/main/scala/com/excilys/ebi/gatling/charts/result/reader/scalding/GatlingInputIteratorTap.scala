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
package com.excilys.ebi.gatling.charts.result.reader.scalding

import cascading.tap.SourceTap
import java.util.{UUID, Properties}
import cascading.flow.FlowProcess
import cascading.scheme.NullScheme
import cascading.tuple.Tuple
import com.excilys.ebi.gatling.charts.result.reader.FileDataReader.TABULATION_PATTERN

class GatlingInputIteratorTap(inputIterator: Iterator[String], scheme: NullScheme[Properties, Iterator[Tuple], Void, Void, Void], size: Long) extends SourceTap[Properties, Iterator[Tuple]](scheme) {
	override val getIdentifier = classOf[GatlingInputIteratorTap].getCanonicalName + UUID.randomUUID()

	override def openForRead(flowProcess: FlowProcess[Properties], input: Iterator[Tuple]) = {
		val fields = scheme.getSourceFields

		val theInput =
			if (input == null)
				inputIterator.map(TABULATION_PATTERN.split(_).take(fields.size()))
					.filter(_.size == fields.size())
					.map(asTuple(_))
			else
				input

		new GatlingTupleIterator(fields, theInput, size)
	}

	override def resourceExists(conf: Properties) = true

	override def getModifiedTime(conf: Properties) = 1L

	private def asTuple(values: Array[String]) = {
		val tuple = new Tuple()

		values.foreach(tuple.add(_))

		tuple
	}
}
