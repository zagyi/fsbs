import sbt._

object Versions {
  val circe          = "0.13.0"
  val doobie         = "0.9.2"
  val h2database     = "1.4.200"
  val http4s         = "0.21.7"
  val kindProjector  = "0.11.0"
  val logback        = "1.2.3"
  val quill          = "3.5.3"
  val tapir          = "0.16.16"
  val zio            = "1.0.1"
  val zioInteropCats = "2.1.4.0"
}

object Dependencies {

  object circe {
    val generic = circe("generic")

    private def circe(module: String) =
      "io.circe" %% s"circe-$module" % Versions.circe
  }

  object doobie {
    val core = doobie("core")
    val h2   = doobie("h2")

    private def doobie(module: String) =
      "org.tpolecat" %% s"doobie-$module" % Versions.doobie
  }

  object http4s {
    val core = http4s("core")

    private def http4s(module: String) =
      "org.http4s" %% s"http4s-$module" % Versions.http4s
  }

  val kindProjector =
    "org.typelevel" %% "kind-projector" % Versions.kindProjector cross CrossVersion.full

  object quill {
    val jdbc = quill("jdbc")

    private def quill(module: String) =
      "io.getquill" %% s"quill-$module" % Versions.quill
  }

  object tapir {
    val core               = tapir("core")
    val http4s_server      = tapir("http4s-server")
    val json_circe         = tapir("json-circe")
    val openapi_docs       = tapir("openapi-docs")
    val openapi_circe_yaml = tapir("openapi-circe-yaml")
    val swagger_ui_http4s  = tapir("swagger-ui-http4s")
    val zio                = tapir("zio")
    val zio_http4s_server  = tapir("zio-http4s-server")

    private def tapir(module: String) =
      "com.softwaremill.sttp.tapir" %% s"tapir-$module" % Versions.tapir
  }

  object zio {
    val zio          = zio0("")
    val test         = zio0("test")
    val interop_cats = zio0("interop-cats", Versions.zioInteropCats)

    private def zio0(module: String, version: String = Versions.zio) =
      "dev.zio" %% m("zio", module) % version
  }

  private def m(stem: String, module: String) =
    stem + (if (module.isEmpty) "" else "-") + module
}
