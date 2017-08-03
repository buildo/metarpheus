package io.buildo.metarpheus
package core
package test

import scala.io.Source

object Fixtures {
  val models      = Source.fromURL(getClass.getResource("/fixtures/models.scala")).mkString
  val routes      = Source.fromURL(getClass.getResource("/fixtures/routes.scala")).mkString
  val controllers = Source.fromURL(getClass.getResource("/fixtures/controllers.scala")).mkString
}
