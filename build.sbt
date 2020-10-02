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
    .settings(name := "zio-tapir-http4s")
    .settings(commonSettings)
    .settings(dependencies)

lazy val commonSettings = Seq(
  update / evictionWarningOptions := EvictionWarningOptions.empty,
  Compile / console / scalacOptions --= Seq(
    "-Wunused:_",
    "-Xfatal-warnings"
  ),
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value
)

addCompilerPlugin(
  kindProjector
)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    circe.generic,
    doobie.core,
    http4s.core,
    quill.jdbc,
    tapir.json_circe,
    tapir.swagger_ui_http4s,
    tapir.openapi_docs,
    tapir.openapi_circe_yaml,
    tapir.zio_http4s_server,
    zio.zio,
    zio.interop_cats
  ),
  libraryDependencies ++= Seq(
    zio.test
  ).map(_ % Test)
)
