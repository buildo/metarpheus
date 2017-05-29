package morpheus.extractors

import org.scalatest._

class ApiSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    List(
      morpheus.Fixtures.models.parse[Source].get,
      morpheus.Fixtures.routes.parse[Source].get,
      morpheus.Fixtures.controllers.parse[Source].get
    )
  }

  test("extract used models") {
    import morpheus.intermediate._

    val api = extractFullAPI(parsed, Common.overrides, Common.routeMatcherToTpe, Common.authRouteTermNames, wiro = false)
      .stripUnusedModels(Common.modelsForciblyInUse)

    assert(api.models.collectFirst {
      case CaseEnum("CampingLocation", _, _) => ()
    }.isDefined)

  }

  test("extract wiro style") {
    import morpheus.intermediate._

    val api = extractFullAPI(parsed, Common.overrides, Common.routeMatcherToTpe, Common.authRouteTermNames, wiro = true)
      .stripUnusedModels(Common.modelsForciblyInUse)

    assert(api.routes.collectFirst {
      case Route(get, List(
        RouteSegment.String("campings"),
        RouteSegment.String("getByCoolnessAndSize")
      ), _, _, _, _, _, _, _) => ()
    }.isDefined)
  }

}
