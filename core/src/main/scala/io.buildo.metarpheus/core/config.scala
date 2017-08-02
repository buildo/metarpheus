package io.buildo.metarpheus
package core

case class Config(
  authRouteTermNames: List[String] = Nil,
  modelsForciblyInUse: Set[String] = Set.empty,
  wiro: Boolean = false
)

object Config {
  val default: Config = Config()
}