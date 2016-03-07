package morpheus.extractors

import morpheus.intermediate._

object Common {
    val overridableOverride = Route(
      method = "get",
      route = List(
        RouteSegment.String("something")
      ),
      params = List(),
      authenticated = false,
      returns = Type.Apply("List", List(Type.Name("Something"))),
      body = None,
      ctrl = List("campingController", "something"),
      desc = Some("gets something"),
      name = List("campingController", "overridden")
    )

    val overrides = Map(
      List("campingController", "overridden") -> overridableOverride
    )
}
