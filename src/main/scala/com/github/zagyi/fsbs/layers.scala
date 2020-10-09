package com.github.zagyi.fsbs

import com.github.zagyi.fsbs.config.AppConfig
import com.github.zagyi.fsbs.persistence.{PostgresContext, PostgresModule}
import com.github.zagyi.fsbs.repository.PetRepository
import zio._
import zio.blocking.Blocking
import zio.config.ReadError
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

object layers {

  type AppLayer = PostgresContext with PetRepository with Logging

  lazy val live: Layer[ReadError[String], AppLayer] =
    AppConfig.dbConfig >>> PostgresModule.live >+> common

  lazy val dev: URLayer[Blocking, AppLayer] =
    PostgresModule.embedded >+> common

  private lazy val common: URLayer[PostgresContext, PetRepository with Logging] =
    PetRepository.live ++ logging

  lazy val logging: ULayer[Logging] = Slf4jLogger.make((_, s) => s)
}
