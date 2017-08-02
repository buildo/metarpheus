package io.buildo.metarpheus
package core

trait Config {
  val routeMatcherToIntermediate: PartialFunction[(String, Option[intermediate.Type]), intermediate.Type]

  val routeOverrides: Map[List[String], intermediate.Route] = Map()

  val authRouteTermNames: List[String] = Nil

  val modelsForciblyInUse: Set[String] = Set.empty
}

object DefaultConfig extends Config {
  val routeMatcherToIntermediate = PartialFunction.empty
  override val authRouteTermNames = List("withUserAuthentication")
}
