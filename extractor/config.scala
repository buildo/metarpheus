package morpheus

trait Config {
  val routeMatcherToIntermediate: PartialFunction[(String, Option[intermediate.Type]), intermediate.Type]

  val routeOverrides: Map[List[String], intermediate.Route] = Map()

  val authRouteTermNames: List[String] = Nil

  val customModelsIncluded: Set[String] = Set.empty
}

object DefaultConfig extends Config {
  val routeMatcherToIntermediate = PartialFunction.empty
  override val authRouteTermNames = List("withUserAuthentication")
}
