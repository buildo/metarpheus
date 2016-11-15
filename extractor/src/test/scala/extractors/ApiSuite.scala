package morpheus.extractors

import org.scalatest._

class ApiSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    List(
      morpheus.Fixtures.models.parse[Source].get,
      morpheus.Fixtures.routes.parse[Source].get
    )
  }

  test("extract used models") {
    import morpheus.intermediate._

    val api = extractFullAPI(parsed, Common.overrides, Common.routeMatcherToTpe, Common.authRouteTermNames)
      .stripUnusedModels(Common.customModelsIncluded)

    assert(api.models.collectFirst {
      case CaseEnum("CampingLocation", _, _) => ()
    }.isDefined)

  }

}
