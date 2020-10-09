package com.github.zagyi.fsbs

import zio.config._
import ConfigDescriptor.{ url => _, _ }
import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric.GreaterEqual
import eu.timepit.refined.string._
import zio.config.refined._
import zio.config.syntax._
import zio.config.typesafe.TypesafeConfig

package object config {

  type NEString = String Refined NonEmpty

  final case class AppConfig(
      dbConfig: DBConfig,
      apiConfig: ApiConfig
    )

  final case class DBConfig(
      url: NEString,
      driver: NEString,
      user: NEString,
      password: NEString
    )

  final case class ApiConfig(
      baseUrl: String Refined Url,
      port: Int Refined GreaterEqual[W.`1024`.T]
    )

  // format: off
  private val appConfigDescription: ConfigDescriptor[AppConfig] = (
    nested("db")((
      nonEmpty(string("url")) |@|
      nonEmpty(string("driver")) |@|
      nonEmpty(string("user")) |@|
      nonEmpty(string("password"))
    )(DBConfig.apply, DBConfig.unapply)) |@|
    nested("api")((
      url(string("base-url")) |@|
      greaterEqual[W.`1024`.T](int("port"))
    )(ApiConfig.apply, ApiConfig.unapply))
  )(AppConfig.apply, AppConfig.unapply)
  // format: on

  object AppConfig {

    val appConfig = TypesafeConfig.fromDefaultLoader(appConfigDescription)

    val apiConfig = appConfig.narrow(_.apiConfig)

    val dbConfig = appConfig.narrow(_.dbConfig)
  }
}
