package com.github.zagyi.fsbs

import java.io.Closeable

import com.github.zagyi.fsbs.config.DBConfig
import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.typesafe.config.ConfigFactory
import io.getquill._
import org.flywaydb.core.Flyway
import zio._
import zio.blocking._
import zio.config.ZConfig

package object persistence {

  type PostgresContext = Has[PostgresJdbcContext[SnakeCase]]
  type DataSource      = Has[javax.sql.DataSource with Closeable]

  object PostgresModule {

    lazy val live: URLayer[ZConfig[DBConfig], PostgresContext] =
      liveDS >>> postgresContext

    lazy val embedded: URLayer[Blocking, PostgresContext] =
      embeddedDS >>> postgresContext

    private val postgresContext: URLayer[DataSource, PostgresContext] =
      ZLayer.fromServiceM { ds =>
        initDB(ds)
          .as(new PostgresJdbcContext[SnakeCase](SnakeCase, ds))
          .orDie
      }

    private val liveDS: URLayer[ZConfig[DBConfig], DataSource] =
      ZLayer.fromService { config =>
        import config._

        import scala.jdk.CollectionConverters._
        JdbcContextConfig(
          ConfigFactory.parseMap(
            Map(
              "dataSourceClassName" -> driver,
              "dataSource.url"      -> url,
              "dataSource.user"     -> user,
              "dataSource.password" -> password
            ).asJava
          )
        ).dataSource
      }

    private val embeddedDS: URLayer[Blocking, DataSource] = {
      type DataSource = javax.sql.DataSource with Closeable

      val start: URIO[Blocking, DataSource] =
        effectBlocking {
          import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
          val db           = EmbeddedPostgres.builder().setPort(0).start()
          val pgDataSource = new org.postgresql.ds.PGSimpleDataSource()
          pgDataSource.setUser("postgres")
          pgDataSource.setPortNumber(db.getPort)
          val config       = new HikariConfig()
          config.setDataSource(pgDataSource)
          new HikariDataSource(config)
        }.orDie

      val stop: DataSource => URIO[Blocking, Any] =
        ds => effectBlocking(ds.close()).orDie

      Managed
        .make(start)(stop)
        .toLayer
    }

    def initDB(ds: javax.sql.DataSource): Task[Any] =
      Task {
        Flyway
          .configure()
          .dataSource(ds)
          .load()
          .migrate()
      }
  }
}
