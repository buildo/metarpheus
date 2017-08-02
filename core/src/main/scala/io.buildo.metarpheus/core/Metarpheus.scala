package io.buildo.metarpheus
package core

import scala.meta._
import scala.meta.inputs.Input

object Metarpheus {

  def run(files: List[String], config: Config, wiro: Boolean): intermediate.API = {
    val parsed = files.map(Input.String(_).parse[Source].get)
    extractors.extractFullAPI(
      parsed = parsed,
      routeOverrides = config.routeOverrides,
      routeMatcherToIntermediate = config.routeMatcherToIntermediate,
      authRouteTermNames = config.authRouteTermNames,
      wiro = wiro
    )
  }


}