import Dependencies._

ThisBuild / organization := "com.zagyi"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.0.1-SNAPSHOT"

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:_",
  "-unchecked",
  "-Xfatal-warnings",
  "-Ymacro-annotations"
)

lazy val `zio-tapir-http4s` =
  project
    .in(file("."))
    .settings(name := "fsbs")
    .settings(commonSettings)
    .settings(dependencies)

lazy val commonSettings = Seq(
  update / evictionWarningOptions := EvictionWarningOptions.empty,
  Compile / console / scalacOptions --= Seq(
    "-Wunused:_",
    "-Xfatal-warnings"
  ),
  testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value
)

addCompilerPlugin(
  kindProjector
)

lazy val dependencies = Seq(
  libraryDependencies ++=
    Seq[ModuleID](
      "ch.qos.logback"           % "logback-classic" % "1.2.3",
      "com.opentable.components" % "otj-pg-embedded" % "0.13.3",
      "org.flywaydb"             % "flyway-core"     % "7.0.1",
      circe.generic,
      http4s.core,
      quill.jdbc,
      tapir.json_circe,
      tapir.openapi_circe_yaml,
      tapir.openapi_docs,
      tapir.swagger_ui_http4s,
      tapir.zio_http4s_server,
      zio,
      zio.config.magnolia,
      zio.config.refined,
      zio.config.typesafe,
      zio.interop_cats,
      zio.logging.slf4j
    ),
  libraryDependencies ++= Seq(
    zio.test
  ).map(_ % Test)
)
