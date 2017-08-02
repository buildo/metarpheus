package io.buildo.metarpheus
package core

import scala.meta._
import scala.meta.inputs.Input

object Metarpheus {

  def run(files: List[String], config: Config): intermediate.API = {
    val parsed = files.map(Input.String(_).parse[Source].get)
    extractors.extractFullAPI(
      parsed = parsed,
      authRouteTermNames = config.authRouteTermNames,
      wiro = config.wiro
    )
  }


}