package io.buildo.metarpheus
package core

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.util.control.NonFatal

import io.circe._
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.generic.extras._
import io.circe.generic.extras.auto._

object JSFacade {

  trait JSConfig extends js.Object {
    val authRouteTermNames: js.UndefOr[js.Array[String]]
    val modelsForciblyInUse: js.UndefOr[js.Array[String]]
    val wiro: js.UndefOr[Boolean]
  }

  @JSExportTopLevel("run")
  def run(paths: js.Array[String], jsConfig: js.UndefOr[JSConfig]) = {
    implicit val circeConfiguration: Configuration =
      Configuration.default.withDefaults.withDiscriminator("_type")
    val config = jsConfig
      .map { jsConfig =>
        val json = js.JSON.stringify(jsConfig)
        decode[Config](json) match {
          case Left(error) => throw js.JavaScriptException(error.toString)
          case Right(config) => config
        }
      }
      .getOrElse(Config.default)

    try {
      val result = Metarpheus.run(paths.toList, config)
      val printer = Printer.noSpaces.copy(dropNullKeys = true)
      js.JSON.parse(printer.pretty(result.asJson))
    } catch {
      case NonFatal(e) => throw js.JavaScriptException(e.getMessage)
    }
  }

}
