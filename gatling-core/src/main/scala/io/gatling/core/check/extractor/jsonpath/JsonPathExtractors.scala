package io.gatling.core.check.extractor.jsonpath

import io.gatling.core.check.Extractor
import net.minidev.json.parser.JSONParser

abstract class JsonPathExtractors {

	abstract class JsonPathExtractor[X] extends Extractor[Any, String, X] {
		val name = "jsonPath"
	}

	def parse(bytes: Array[Byte]) = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(bytes)

	def extractOne: (Int) => Extractor[Any, String, String]
	def extractMultiple: Extractor[Any, String, Seq[String]]
	def count: Extractor[Any, String, Int]
}
