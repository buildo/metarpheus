package morpheus.extractors
package route

import org.scalatest._

class RouteSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    morpheus.Fixtures.routes.parse[Source]
  }

  test("parse successfully") {
    parsed
  }

  test("extract routes from fixture code") {
    import morpheus.intermediate._

    val models = model.extractModel(parsed)
    val caseClasses = models.collect { case x: morpheus.intermediate.CaseClass => x }
    val result = extractAllRoutes(caseClasses, Common.overrides, Common.routeMatcherToTpe)(parsed)

    assert(result.toString ===
      List(
        Route(
          method = "get",
          route = List(
            RouteSegment.String("campings")
          ),
          params = List(
            RouteParam(
              Some("coolness"),
              Type.Name("String"),
              true,
              Some("how cool it is")
            ),
            RouteParam(
              Some("size"),
              Type.Name("Int"),
              false,
              Some("the number of tents")
            )
          ),
          authenticated = false,
          returns = Type.Apply("List", List(Type.Name("Camping"))),
          body = None,
          ctrl = List("campingController", "getByCoolnessAndSize"),
          desc = Some("get campings matching the requested coolness and size"),
          name = List("campingController", "getByCoolnessAndSize")

        ),
        Route(
          method = "get",
          route = List(
            RouteSegment.String("campings"),
            RouteSegment.Param(RouteParam(
              None,
              Type.Name("Int"),
              true,
              None
            ))
          ),
          params = List(),
          authenticated = true,
          returns = Type.Name("Camping"),
          body = None,
          ctrl = List("campingController", "getById"),
          desc = Some("get a camping by id"),
          name = List("campingController", "getById")
        ),
        Route(
          method = "get",
          route = List(
            RouteSegment.String("campings"),
            RouteSegment.Param(RouteParam(
              None,
              Type.Apply("Id", Seq(Type.Name("Camping"))),
              true,
              None
            ))
          ),
          params = List(),
          authenticated = true,
          returns = Type.Name("Camping"),
          body = None,
          ctrl = List("campingController", "getByTypedId"),
          desc = Some("get a camping by typed id"),
          name = List("campingController", "getByTypedId")
        ),
        Route(
          method = "get",
          route = List(
            RouteSegment.String("campings")
          ),
          params = List(
            RouteParam(
              Some("hasBeach"),
              Type.Name("Boolean"),
              true,
              Some("whether there's a beach")
            )
          ),
          authenticated = false,
          returns = Type.Apply("List", List(Type.Name("Camping"))),
          body = None,
          ctrl = List("campingController", "getByHasBeach"),
          desc = Some("get campings based on whether they're close to a beach"),
          name = List("campingController", "getByHasBeach")
        ),
        Route(
          method = "post",
          route = List(
            RouteSegment.String("campings")
          ),
          params = List(),
          authenticated = false,
          returns = Type.Name("Camping"),
          body = Some(Route.Body(Type.Name("Camping"),None)),
          ctrl = List("campingController", "create"),
          desc = Some("create a camping"),
          name = List("campingController", "create")
        ),
        Common.overridableOverride,
        Route(
          method = "get",
          route = List(
            RouteSegment.String("campings"),
            RouteSegment.String("by_query")
          ),
          params = List(
            RouteParam(
              Some("coolness"),
              Type.Name("String"),
              false,
              Some("how cool it is")
            ),
            RouteParam(
              Some("size"),
              Type.Name("Int"),
              true,
              Some("the number of tents")
            )
          ),
          authenticated = false,
          returns = Type.Apply("List", List(Type.Name("Camping"))),
          body = None,
          ctrl = List("campingController", "getByQuery"),
          desc = Some("get multiple campings by params with case class"),
          name = List("campingController", "getByQuery")
        )
      ).toString
    )

  }
}
