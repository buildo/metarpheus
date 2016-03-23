package morpheus

trait Config {
  val routeMatcherToIntermediate: PartialFunction[(String, Option[intermediate.Type]), intermediate.Type]

  val routeOverrides: Map[List[String], intermediate.Route] = Map()
}

object DefaultConfig extends Config {
  val routeMatcherToIntermediate = PartialFunction.empty
}
