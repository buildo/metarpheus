package io.buildo.metarpheus
package core

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

case class Config(
  authRouteTermNames: List[String] = Nil,
  modelsForciblyInUse: Set[String] = Set.empty,
  wiro: Boolean = false
) extends LegacyConfig

trait LegacyConfig {
  val routeOverrides: Map[List[String], intermediate.Route] = Map.empty
  val routeMatcherToIntermediate: PartialFunction[(String, Option[intermediate.Type]), intermediate.Type] = PartialFunction.empty
}
