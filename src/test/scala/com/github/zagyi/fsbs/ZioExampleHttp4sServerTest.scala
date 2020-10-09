package com.github.zagyi.fsbs

import com.github.zagyi.fsbs.ZioExampleHttp4sServer.Pet
import com.github.zagyi.fsbs.config.AppConfig._
import com.github.zagyi.fsbs.config._
import com.github.zagyi.fsbs.persistence.PostgresModule
import com.github.zagyi.fsbs.repository.PetRepository
import zio.config.getConfig
import zio.test.Assertion._
import zio.test._

object ZioExampleHttp4sServerTest extends DefaultRunnableSpec {

  override def spec =

    suite("x")(
      testM("read config") {
        getConfig[AppConfig]
          .provideCustomLayer(appConfig)
          .map(appConfig =>
            assert(appConfig.apiConfig.port.value)(equalTo(8080))
          )
      },
      testM("find-create-find") {
        for {
          noPet <- PetRepository.find("foo")
          newPet = Pet("foo", "url")
          _     <- PetRepository.create(newPet)
          pet2  <- PetRepository.find("foo")
        } yield assert(noPet)(isNone) &&
          assert(pet2)(isSome(equalTo(newPet)))
      }.provideCustomLayer(
        PostgresModule.embedded >+> PetRepository.live
      )
    )
}
