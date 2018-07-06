package io.buildo.metarpheus
package core

case class Config(
  modelsForciblyInUse: Set[String] = Set.empty
)

object Config {
  val default: Config = Config()
}
