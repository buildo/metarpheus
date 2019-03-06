package io.buildo.metarpheus
package core
package test

import org.scalatest._

import extractors._

class ApiSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    List(
      Fixtures.models.parse[Source].get,
      Fixtures.controllers.parse[Source].get
    )
  }

  test("extract used models") {
    import intermediate._

    val api = extractFullAPI(parsed).stripUnusedModels(Common.modelsForciblyInUse)

    assert(api.routes.collectFirst {
      // format: off
      case Route(get, List(
        RouteSegment.String("campings"),
        RouteSegment.String("getByCoolnessAndSize")
      ), _, _, _, _, _, _, _) => ()
      // format: on
    }.isDefined)

    assert(api.models.collectFirst {
      case CaseClass(
          "IgnoreMe",
          _,
          _,
          _,
          _
          ) =>
        ()
    }.isEmpty)
  }

}
