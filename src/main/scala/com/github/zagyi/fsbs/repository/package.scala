package com.github.zagyi.fsbs

import com.github.zagyi.fsbs.ZioExampleHttp4sServer.Pet
import com.github.zagyi.fsbs.persistence.PostgresContext
import io.getquill._
import zio._
import zio.blocking._

package object repository {

  type PetRepository = Has[PetRepository.Service]

  object PetRepository {

    private type Deps = Blocking with PostgresContext
    type Env          = Deps with PetRepository

    trait Service {
      def create(pet: Pet): RIO[Deps, Unit]

      def find(species: String): RIO[Deps, Option[Pet]]
    }

    private def withService[A](f: Service => RIO[Env, A]): RIO[Env, A] =
      ZIO.accessM(env => f(env.get[Service]))

    def create(pet: Pet): RIO[Env, Unit] =
      withService(_.create(pet))

    def find(species: String): RIO[Env, Option[Pet]] =
      withService(_.find(species))

    val live: URLayer[PostgresContext, PetRepository] =
      ZLayer.succeed(
        new Service {
          override def create(pet: Pet): RIO[Deps, Unit] =
            ZIO.accessM { hasCtx =>
              val ctx = hasCtx.get[PostgresJdbcContext[SnakeCase]]
              import ctx._
              effectBlocking(run(query[Pet].insert(lift(pet))))
            }

          override def find(species: String): RIO[Deps, Option[Pet]] =
            ZIO.accessM { hasCtx =>
              val ctx = hasCtx.get[PostgresJdbcContext[SnakeCase]]
              import ctx._
              effectBlocking(
                run(query[Pet].filter(_.species == lift(species))).headOption
              )
            }
        }
      )
  }
}
