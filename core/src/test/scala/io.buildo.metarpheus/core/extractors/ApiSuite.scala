package io.buildo.metarpheus
package core
package test

import org.scalatest._

import extractors._

class ApiSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    List(
      Fixtures.models.parse[Source].get,
      Fixtures.routes.parse[Source].get,
      Fixtures.controllers.parse[Source].get
    )
  }

  test("extract used models") {
    import intermediate._

    val api = extractFullAPI(parsed, Common.authRouteTermNames, wiro = false)
      .stripUnusedModels(Common.modelsForciblyInUse)

    assert(api.models.collectFirst {
      case CaseEnum("CampingLocation", _, _) => ()
    }.isDefined)

  }

  test("extract wiro style") {
    import intermediate._

    val api = extractFullAPI(parsed, Common.authRouteTermNames, wiro = true)
      .stripUnusedModels(Common.modelsForciblyInUse)

    assert(api.routes.collectFirst {
      case Route(get, List(
        RouteSegment.String("campings"),
        RouteSegment.String("getByCoolnessAndSize")
      ), _, _, _, _, _, _, _) => ()
    }.isDefined)
  }

}
