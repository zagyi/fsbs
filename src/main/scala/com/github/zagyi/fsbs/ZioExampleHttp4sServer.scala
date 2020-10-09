package com.github.zagyi.fsbs

import cats.implicits._
import com.github.zagyi.fsbs.layers.AppLayer
import com.github.zagyi.fsbs.repository.PetRepository
import io.circe.generic.auto._
import org.http4s._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import sttp.tapir.docs.openapi._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import sttp.tapir.ztapir._
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.logging._

object ZioExampleHttp4sServer extends scala.App {

  type AppEnv = Blocking with AppLayer

  val appEnv: ULayer[AppEnv] = Blocking.live >+> layers.dev

  implicit val runtime: Runtime[ZEnv] = Runtime.default

  case class Pet(species: String, url: String)

  val getPet: ZEndpoint[String, String, Option[Pet]] =
    endpoint
      .get
      .in("pet" / path[String]("petId"))
      .errorOut(stringBody)
      .out(jsonBody[Option[Pet]])

  val postPet: ZEndpoint[Pet, String, Unit] =
    endpoint
      .post
      .in("pet")
      .in(jsonBody[Pet])
      .errorOut(stringBody)

  val getPetRoute: HttpRoutes[Task] =
    getPet.toRoutes { species =>
      val err = "Failed find pet."
      PetRepository
        .find(species)
        .tapError(t => log.throwable(err, t))
        .orElseFail(err)
        .provideLayer(appEnv)
    }

  val postPetRoute: HttpRoutes[Task] =
    postPet.toRoutes { pet =>
      val err = "Failed to add new pet."
      PetRepository
        .create(pet)
        .tapError(t => log.throwable(err, t))
        .orElseFail(err)
        .provideLayer(appEnv)
    }

  val postPetRoute2 =
    postPet.toRoutesR[AppEnv] { pet =>
      val err = "Failed to add new pet."
      PetRepository
        .create(pet)
        .tapError(t => log.throwable(err, t))
        .orElseFail(err)
    }

  val service: HttpRoutes[Task] = getPetRoute <+> postPetRoute

  val yaml = List(getPet).toOpenAPI("Our pets", "1.0").toYaml

  // TODO: get host/port from config
  val serve =
    BlazeServerBuilder[Task](runtime.platform.executor.asEC)
      .bindHttp(8080, "localhost")
      .withHttpApp(
        Router(
          "/" -> (service <+> new SwaggerHttp4s(yaml).routes[Task])
        ).orNotFound
      )
      .serve
      .compile
      .drain

  runtime.unsafeRun(serve)
}
