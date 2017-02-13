package morpheus.extractors
package controller

import org.scalatest._

class ControllerSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    morpheus.Fixtures.controllers.parse[Source].get
  }

  test("parse successfully") {
    parsed
  }

  test("extract routes from fixture code") {
    import morpheus.intermediate._

    val models = model.extractModel(parsed)
    val caseClasses = models.collect { case x: morpheus.intermediate.CaseClass => x }
    val result = extractAllRoutes(parsed)

    assert(result.toString ===
      List(
        Route(
          method = "get",
          route = List(
            RouteSegment.String("campings"),
            RouteSegment.String("getByCoolnessAndSize")
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
            ),
            RouteParam(
              Some("nickname"),
              Type.Name("String"),
              true,
              Some("a friendly name for the camping")
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
            RouteSegment.String("getBySizeAndDistance")
          ),
          params = List(
            RouteParam(
              Some("size"),
              Type.Name("Int"),
              true,
              Some("the number of tents")
            ),
            RouteParam(
              Some("distance"),
              Type.Name("Int"),
              true,
              Some("how distant it is")
            )
          ),
          authenticated = false,
          returns = Type.Apply("List", List(Type.Name("Camping"))),
          body = None,
          ctrl = List("campingController", "getBySizeAndDistance"),
          desc = Some("get campings matching the requested size and distance"),
          name = List("campingController", "getBySizeAndDistance")
        ),
        Route(
          method = "get",
          route = List(
            RouteSegment.String("campings"),
            RouteSegment.String("getById")
          ),
          params = List(
            RouteParam(
              Some("id"),
              Type.Name("Int"),
              true,
              Some("camping id")
            )
          ),
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
            RouteSegment.String("getByTypedId")
          ),
          params = List(
            RouteParam(
              Some("id"),
              Type.Apply("Id", Seq(Type.Name("Camping"))),
              true,
              None
            )
          ),
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
            RouteSegment.String("campings"),
            RouteSegment.String("getByHasBeach")
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
            RouteSegment.String("campings"),
            RouteSegment.String("create")
          ),
          params = List(
            RouteParam(
              Some("camping"),
              Type.Name("Camping"),
              true,
              None,
              inBody = true
            )
          ),
          authenticated = false,
          returns = Type.Name("Camping"),
          body = None,
          ctrl = List("campingController", "create"),
          desc = Some("create a camping"),
          name = List("campingController", "create")
        )
      ).toString
    )

  }
}

