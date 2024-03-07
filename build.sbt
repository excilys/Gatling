import sbt._

import _root_.io.gatling.build.license.ApacheV2License

import BuildSettings._
import Dependencies._
import VersionFile._

Global / githubPath := "gatling/gatling"
Global / gatlingDevelopers := Seq(
  GatlingDeveloper("slandelle@gatling.io", "Stephane Landelle", isGatlingCorp = true),
  GatlingDeveloper("gcorre@gatling.io", "Guillaume Corré", isGatlingCorp = true),
  GatlingDeveloper("tpetillot@gatling.io", "Thomas Petillot", isGatlingCorp = true)
)
// [e]
//
// [e]

// Root project

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
Global / scalaVersion := "2.13.13"

lazy val root = Project("gatling-parent", file("."))
  .enablePlugins(GatlingOssPlugin)
  .disablePlugins(SbtSpotless)
  .aggregate(
    nettyUtil,
    sharedEnterprise,
    commons,
    docSamples,
    jsonpath,
    core,
    coreJava,
    jdbc,
    jdbcJava,
    redis,
    redisJava,
    httpClient,
    http,
    httpJava,
    jms,
    jmsJava,
    charts,
    graphite,
    app,
    recorder,
    testFramework
  )
  .settings(basicSettings)
  .settings(skipPublishing)

// Modules
lazy val docSamples = (project in file("src/docs"))
  .disablePlugins(SbtSpotless)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    basicSettings,
    skipPublishing,
    Test / unmanagedSourceDirectories ++= (baseDirectory.value ** "code").get,
    libraryDependencies ++= docDependencies,
    headerLicense := ApacheV2License,
    // Avoid formatting but avoid errors when calling this tasks with "all"
    List(Compile, Test).flatMap { conf =>
      inConfig(conf) {
        List(
          scalafmtSbtCheck := { true },
          scalafmtCheckAll := { () },
          scalafmt := { () },
          scalafmtSbt := { () },
          scalafixAll := { () }
        )
      }
    },
    List(
      scalafmtSbtCheck := { true },
      scalafmtCheckAll := { () },
      scalafmt := { () },
      scalafmtSbt := { () },
      scalafixAll := { () }
    ),
    spotlessCheck := { () },
    kotlinVersion := "1.9.23"
  )
  .dependsOn(
    Seq(commons, jsonpath, core, coreJava, http, httpJava, jms, jmsJava, jdbc, jdbcJava, redis, redisJava).map(
      _ % "compile->compile;test->test"
    ): _*
  )
  .settings(libraryDependencies ++= docSamplesDependencies)

def gatlingModule(id: String) =
  Project(id, file(id))
    .enablePlugins(GatlingOssPlugin)
    .disablePlugins(KotlinPlugin)
    .settings(gatlingModuleSettings ++ CodeAnalysis.settings)

lazy val sharedEnterprise = gatlingModule("gatling-shared-enterprise")

lazy val nettyUtil = gatlingModule("gatling-netty-util")
  .settings(libraryDependencies ++= nettyUtilDependencies)

lazy val commons = gatlingModule("gatling-commons")
  .disablePlugins(SbtSpotless)
  .settings(libraryDependencies ++= commonsDependencies)
  .settings(generateVersionFileSettings)

lazy val jsonpath = gatlingModule("gatling-jsonpath")
  .disablePlugins(SbtSpotless)
  .settings(libraryDependencies ++= jsonpathDependencies)

lazy val core = gatlingModule("gatling-core")
  .dependsOn(nettyUtil)
  .dependsOn(commons % "compile->compile;test->test")
  .dependsOn(sharedEnterprise)
  .dependsOn(jsonpath % "compile->compile;test->test")
  .settings(libraryDependencies ++= coreDependencies)

lazy val coreJava = gatlingModule("gatling-core-java")
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= coreJavaDependencies)

lazy val jdbc = gatlingModule("gatling-jdbc")
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= jdbcDependencies)

lazy val jdbcJava = gatlingModule("gatling-jdbc-java")
  .dependsOn(coreJava, jdbc % "compile->compile;test->test")
  .settings(libraryDependencies ++= defaultJavaDependencies)

lazy val redis = gatlingModule("gatling-redis")
  .disablePlugins(SbtSpotless)
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= redisDependencies)

lazy val redisJava = gatlingModule("gatling-redis-java")
  .dependsOn(coreJava, redis % "compile->compile;test->test")
  .settings(libraryDependencies ++= defaultJavaDependencies)

lazy val httpClient = gatlingModule("gatling-http-client")
  .dependsOn(nettyUtil % "compile->compile;test->test")
  .settings(libraryDependencies ++= httpClientDependencies)

lazy val http = gatlingModule("gatling-http")
  .dependsOn(core % "compile->compile;test->test", httpClient % "compile->compile;test->test")
  .settings(libraryDependencies ++= httpDependencies)

lazy val httpJava = gatlingModule("gatling-http-java")
  .dependsOn(coreJava, http % "compile->compile;test->test")
  .settings(libraryDependencies ++= defaultJavaDependencies)

lazy val jms = gatlingModule("gatling-jms")
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= jmsDependencies)
  .settings(Test / parallelExecution := false)

lazy val jmsJava = gatlingModule("gatling-jms-java")
  .dependsOn(coreJava, jms % "compile->compile;test->test")
  .settings(libraryDependencies ++= defaultJavaDependencies)

lazy val charts = gatlingModule("gatling-charts")
  .disablePlugins(SbtSpotless)
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= chartsDependencies)
  .settings(chartTestsSettings)

lazy val graphite = gatlingModule("gatling-graphite")
  .disablePlugins(SbtSpotless)
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= graphiteDependencies)

lazy val benchmarks = gatlingModule("gatling-benchmarks")
  .disablePlugins(SbtSpotless)
  .dependsOn(core, http)
  .enablePlugins(JmhPlugin)
  .settings(libraryDependencies ++= benchmarkDependencies)

lazy val app = gatlingModule("gatling-app")
  .disablePlugins(SbtSpotless)
  .dependsOn(core, coreJava, http, httpJava, jms, jmsJava, jdbc, jdbcJava, redis, redisJava, graphite, charts)

lazy val recorder = gatlingModule("gatling-recorder")
  .dependsOn(core % "compile->compile;test->test", http)
  .settings(libraryDependencies ++= recorderDependencies)

lazy val testFramework = gatlingModule("gatling-test-framework")
  .disablePlugins(SbtSpotless)
  .dependsOn(app)
  .settings(libraryDependencies ++= testFrameworkDependencies)

lazy val publicSamples = Project("gatling-samples", file("gatling-samples"))
  .dependsOn(app)
  .enablePlugins(GatlingOssPlugin)
  .settings(gatlingModuleSettings)
  .settings(
    skipPublishing
  )
