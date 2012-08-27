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

import cascading.tuple.{TupleEntryChainIterator, Tuple, Fields}
import scala.collection.JavaConversions._
import grizzled.slf4j.Logging
import com.excilys.ebi.gatling.charts.result.reader.Predef.LOG_STEP

class GatlingTupleIterator(fields: Fields, iterator: Iterator[Tuple], size: Long) extends TupleEntryChainIterator(fields, iterator) with Logging {

	private var linesRead = 0L

	override def next() = {
		val tupleEntry = super.next()

		linesRead += 1
		if (linesRead % LOG_STEP == 0)
			info("Read " + linesRead + " lines (" + (linesRead * 100L / size) + " %)")

		tupleEntry
	}

	override def close() {
		info("Read " + linesRead + " lines (" + (linesRead * 100L / size) + " %)")
	}
}
