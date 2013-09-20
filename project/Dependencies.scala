import sbt._

object Dependencies {

	val excilysNexus = "Excilys Nexus" at "http://repository.excilys.com/content/groups/public"
	val excilysReleases = "Excilys Releases" at "http://repository.excilys.com/content/repositories/releases"
	val cloudbeesSnapshots = "Cloudbees Private Repository" at "davs://repository-gatling.forge.cloudbees.com/snapshot"

	private val scalaCompiler     = "org.scala-lang"        % "scala-compiler"     % "2.10.3-RC2"
	private val scalaReflect      = "org.scala-lang"        % "scala-reflect"      % "2.10.3-RC2"
	private val scalaSwing        = "org.scala-lang"        % "scala-swing"        % "2.10.3-RC2"
	private val ahc               = "com.ning"              % "async-http-client"  % "1.7.19"
	private val netty             = "io.netty"              % "netty"              % "3.7.0.Final"
	private val akkaActor         = "com.typesafe.akka"    %% "akka-actor"         % "2.2.1"
	private val config            = "com.typesafe"          % "config"             % "1.0.2"
	private val saxon             = "io.gatling.net.sf.saxon" % "Saxon-HE"         % "9.5.1-2"
	private val slf4jApi          = "org.slf4j"             % "slf4j-api"          % "1.7.5"
	private val fastring          = "com.dongxiguo"        %% "fastring"           % "0.2.2"
	private val jodaTime          = "joda-time"             % "joda-time"          % "2.3"
	private val jodaConvert       = "org.joda"              % "joda-convert"       % "1.5"
	private val scopt             = "com.github.scopt"     %% "scopt"              % "3.1.0"
	private val scalalogging      = "com.typesafe"         %% "scalalogging-slf4j" % "1.0.1"
	private val jsonSmart         = "net.minidev"           % "json-smart"         % "1.2"
	private val jaywayJsonPath    = "com.jayway.jsonpath"   % "json-path"          % "0.8.2.fix24"
	private val gatlingJsonpath   = "io.gatling"           %% "jsonpath"           % "0.2.3"
	private val commonsMath       = "org.apache.commons"    % "commons-math3"      % "3.2"
	private val jsoup             = "org.jsoup"             % "jsoup"              % "1.7.2"
	private val joddLagarto       = "org.jodd"              % "jodd-lagarto"       % "3.4.7"
	private val jzlib             = "com.jcraft"            % "jzlib"              % "1.1.2"
	private val commonsIo         = "commons-io"            % "commons-io"         % "2.4"
	private val redisClient       = "net.debasishg"        %% "redisclient"        % "2.10"        exclude("org.scala-lang", "scala-actors")
	private val zinc              = "com.typesafe.zinc"     % "zinc"               % "0.3.0"
	private val plexusSelector    = "com.excilys.ebi"       % "plexus-selector"    % "1.0.0"
	private val openCsv           = "net.sf.opencsv"        % "opencsv"            % "2.3"

	private val grizzlyWebsockets = "org.glassfish.grizzly" % "grizzly-websockets" % "2.3.4"       % "provided" 

	private val logbackClassic    = "ch.qos.logback"        % "logback-classic"    % "1.0.13"      % "runtime"

	private val junit             = "junit"                 % "junit"              % "4.11"        % "test"
	private val specs2            = "org.specs2"           %% "specs2"             % "2.0"         % "test"
	private val akkaTestKit       = "com.typesafe.akka"    %% "akka-testkit"       % "2.2.0"       % "test"
	private val mockitoCore       = "org.mockito"           % "mockito-core"       % "1.9.5"       % "test"

	private val testDeps = Seq(junit, specs2, akkaTestKit, mockitoCore)

	val coreDeps = Seq(
		scalaCompiler, akkaActor, saxon, jodaTime, jodaConvert, slf4jApi, scalalogging, scalaReflect, jsonSmart,
		jaywayJsonPath, gatlingJsonpath, commonsMath, jsoup, joddLagarto, commonsIo, config, fastring, openCsv, logbackClassic) ++ testDeps

	val redisDeps = Seq(redisClient) ++ testDeps

	val httpDeps = Seq(ahc, netty, jzlib, grizzlyWebsockets) ++ testDeps

	val chartsDeps = testDeps

	val metricsDeps = testDeps

	val appDeps = Seq(scopt, zinc)

	val recorderDeps = Seq(scalaSwing, netty, logbackClassic, scopt, plexusSelector)
}