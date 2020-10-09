import sbt._

object Versions {
  var circe          = "0.13.0"
  var h2database     = "1.4.200"
  var http4s         = "0.21.7"
  var kindProjector  = "0.11.0"
  var logback        = "1.2.3"
  var quill          = "3.5.3"
  var tapir          = "0.16.16"
  var zio            = "1.0.1"
  var zioConfig      = "1.0.0-RC27"
  var zioInteropCats = "2.1.4.0"
  var zioLogging     = "0.5.2"

}

object Dependencies {

  implicit class ModuleIDOps(private val self: ModuleID) extends AnyVal {
    import self._

    def <<(artifactSuffix: String) =
      withName(name + "-" + artifactSuffix)

    def @@(version: String) = withRevision(version)
  }

  val V = Versions

  object circe {
    private[Dependencies] val circe =
      "io.circe" %% "circe" % V.circe

    val generic = circe << "generic"
  }

  object http4s {
    private[Dependencies] val http4s =
      "org.http4s" %% "http4s" % V.http4s

    val core = http4s << "core"
  }

  val kindProjector =
    "org.typelevel" %% "kind-projector" % V.kindProjector cross CrossVersion.full

  object quill {
    private[Dependencies] val quill = "io.getquill" %% "quill" % V.quill

    val jdbc = quill << "jdbc"
  }

  object tapir {
    private[Dependencies] val tapir =
      "com.softwaremill.sttp.tapir" %% "tapir" % V.tapir

    val core               = tapir << "core"
    val http4s_server      = tapir << "http4s-server"
    val json_circe         = tapir << "json-circe"
    val openapi_docs       = tapir << "openapi-docs"
    val openapi_circe_yaml = tapir << "openapi-circe-yaml"
    val swagger_ui_http4s  = tapir << "swagger-ui-http4s"
    val zio                = tapir << "zio"
    val zio_http4s_server  = tapir << "zio-http4s-server"
  }

  object zio {
    private[Dependencies] val zio = "dev.zio" %% "zio" % V.zio

    val interop_cats = (zio << "interop-cats") @@ V.zioInteropCats
    val test         = zio << "test"

    object config {
      private[Dependencies] val zioConfig = (zio << "config") @@ V.zioConfig

      val magnolia = zioConfig << "magnolia"
      val refined  = zioConfig << "refined"
      val typesafe = zioConfig << "typesafe"
    }

    object logging {
      private[Dependencies] val zioLogging = (zio << "logging") @@ V.zioLogging

      val slf4j = zioLogging << "slf4j"
    }
  }

  implicit def zioModuleID(m: zio.type): ModuleID              = m.zio
  implicit def zioConfigModuleID(m: zio.config.type): ModuleID = m.zioConfig
}
