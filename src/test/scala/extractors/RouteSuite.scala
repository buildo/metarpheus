package morpheus.extractors
package route

import org.scalatest._

class RouteSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    Fixture.routeCode.parse[Source]
  }

  test("parse successfully") {
    parsed
  }

  test("extract routes from fixture code") {
    val result = extractAllRoutes(parsed)

    import morpheus.intermediate._

    assert(result ===
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
          returns = Type.Apply("List", List(Type.Name("Camping"))),
          body = None,
          ctrl = List("campingController", "getByCoolnessAndSize"),
          desc = Some("get campings matching the requested coolness and size")
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
          returns = Type.Name("Camping"),
          body = None,
          ctrl = List("campingController", "getById"),
          desc = Some("get a camping by id")
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
          returns = Type.Apply("List", List(Type.Name("Camping"))),
          body = None,
          ctrl = List("campingController", "getByHasBeach"),
          desc = Some("get campings based on whether they're close to a beach")
        ),
        Route(
          method = "post",
          route = List(
            RouteSegment.String("campings")
          ),
          params = List(),
          returns = Type.Name("Camping"),
          body = Some(Route.Body(Type.Name("Camping"),None)),
          ctrl = List("campingController", "create"),
          desc = Some("create a camping")
        )
      )
    )

  }
}
