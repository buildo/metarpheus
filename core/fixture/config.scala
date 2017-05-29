new morpheus.Config {
  import morpheus.intermediate._

  val routeMatcherToIntermediate = PartialFunction[(String, Option[Type]), Type] {
    case ("Id", Some(x@Type.Name(_))) => Type.Apply("Id", Seq(x))
  }

  override val routeOverrides = Map(
    List("campingController", "overridden") -> Route(
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
    ))

  override val authRouteTermNames = List("withUserAuthentication", "withRole")
}
