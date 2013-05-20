/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
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
package io.gatling.recorder.config

import java.io.{ BufferedWriter, File => JFile, FileWriter }

import scala.collection.JavaConversions.{ asScalaBuffer, mapAsJavaMap }
import scala.collection.mutable
import scala.tools.nsc.io.File

import com.typesafe.config.{ Config, ConfigFactory, ConfigRenderOptions }
import com.typesafe.scalalogging.slf4j.Logging

import io.gatling.core.config.{ GatlingConfiguration, GatlingFiles }
import io.gatling.core.util.IOHelper.withCloseable
import io.gatling.core.util.StringHelper.{ eol, RichString }
import io.gatling.recorder.config.ConfigurationConstants._
import io.gatling.recorder.ui.enumeration.FilterStrategy
import io.gatling.recorder.ui.enumeration.FilterStrategy.FilterStrategy
import io.gatling.recorder.ui.enumeration.PatternType
import io.gatling.recorder.ui.enumeration.PatternType.PatternType

case class Pattern(patternType: PatternType, pattern: String)

object RecorderConfiguration extends Logging {

	val remove4Spaces = """\s{4}""".r

	var saveConfiguration = false

	val renderOptions = ConfigRenderOptions.concise.setFormatted(true).setJson(false)

	var configFile: JFile = _

	var configuration: RecorderConfiguration = _

	GatlingConfiguration.setUp()

	def initialSetup(props: mutable.Map[String, _ <: Any], recorderConfigFile: Option[File]) {
		val classLoader = getClass.getClassLoader
		val defaultsConfig = ConfigFactory.parseResources(classLoader, "recorder-defaults.conf")
		configFile = recorderConfigFile.map(_.jfile).getOrElse(new JFile(classLoader.getResource("recorder.conf").getFile))
		val customConfig = ConfigFactory.parseFile(configFile)
		val propertiesConfig = ConfigFactory.parseMap(props)
		buildConfig(ConfigFactory.systemProperties.withFallback(propertiesConfig).withFallback(customConfig).withFallback(defaultsConfig))
		logger.debug(s"configured $configuration")
	}

	def reload(props: mutable.Map[String, _ <: Any]) {
		val frameConfig = ConfigFactory.parseMap(props)
		buildConfig(frameConfig.withFallback(configuration.config))
		logger.debug(s"reconfigured $configuration")
	}

	def saveConfig {
		// Remove request bodies folder configuration (transient), keep only Gatling-related properties
		val configToSave = configuration.config.withoutPath(REQUEST_BODIES_FOLDER).root.withOnlyKey(CONFIG_ROOT)
		withCloseable(new BufferedWriter(new FileWriter(configFile)))(_.write(cleanOutput(configToSave.render(renderOptions))))
	}

	// Removes first empty line and remove the extra level of indentation
	private def cleanOutput(configToSave: String) =
		configToSave.split(eol).drop(1).map(remove4Spaces.replaceFirstIn(_, "")).mkString(eol)

	private def buildConfig(config: Config) {
		def zeroToOption(value: Int) = if (value != 0) Some(value) else None

		def buildPatterns(patterns: List[String], patternsType: List[String]) = {
			patterns.zip(patternsType).map { case (pattern, patternType) => Pattern(PatternType.withName(patternType), pattern) }
		}
		def getOutputFolder(folder: String) = {
			folder.trimToOption.getOrElse(Option(System.getenv("GATLING_HOME")).map(_ => GatlingFiles.sourcesDirectory.toString).getOrElse(System.getProperty("user.home")))
		}

		def getRequestBodiesFolder =
			if (config.hasPath(REQUEST_BODIES_FOLDER))
				config.getString(REQUEST_BODIES_FOLDER)
			else GatlingFiles.requestBodiesDirectory.toString

		configuration = RecorderConfiguration(
			filters = FiltersConfiguration(
				filterStrategy = FilterStrategy.withName(config.getString(FILTER_STRATEGY)),
				patterns = buildPatterns(config.getStringList(PATTERNS).toList, config.getStringList(PATTERNS_TYPE).toList)),
			http = HttpConfiguration(
				automaticReferer = config.getBoolean(AUTOMATIC_REFERER),
				followRedirect = config.getBoolean(FOLLOW_REDIRECT)),
			proxy = ProxyConfiguration(
				port = config.getInt(LOCAL_PORT),
				sslPort = config.getInt(LOCAL_SSL_PORT),
				outgoing = OutgoingProxyConfiguration(
					host = config.getString(PROXY_HOST).trimToOption,
					username = config.getString(PROXY_USERNAME).trimToOption,
					password = config.getString(PROXY_PASSWORD).trimToOption,
					port = zeroToOption(config.getInt(PROXY_PORT)),
					sslPort = zeroToOption(config.getInt(PROXY_SSL_PORT)))),
			core = CoreConfiguration(
				encoding = config.getString(ENCODING),
				outputFolder = getOutputFolder(config.getString(SIMULATION_OUTPUT_FOLDER)),
				requestBodiesFolder = getRequestBodiesFolder,
				pkg = config.getString(SIMULATION_PACKAGE),
				className = config.getString(SIMULATION_CLASS_NAME)),
			config)
	}
}

case class FiltersConfiguration(
	filterStrategy: FilterStrategy,
	patterns: List[Pattern])

case class HttpConfiguration(
	automaticReferer: Boolean,
	followRedirect: Boolean)

case class OutgoingProxyConfiguration(
	host: Option[String],
	username: Option[String],
	password: Option[String],
	port: Option[Int],
	sslPort: Option[Int])

case class ProxyConfiguration(
	port: Int,
	sslPort: Int,
	outgoing: OutgoingProxyConfiguration)

case class CoreConfiguration(
	encoding: String,
	outputFolder: String,
	requestBodiesFolder: String,
	pkg: String,
	className: String)

case class RecorderConfiguration(
	filters: FiltersConfiguration,
	http: HttpConfiguration,
	proxy: ProxyConfiguration,
	core: CoreConfiguration,
	config: Config)