package morpheus

object Fixtures {
  val models = morpheus.util.slurp("fixture/sources/models.scala")
  val routes = morpheus.util.slurp("fixture/sources/routes.scala")
  val controllers = morpheus.util.slurp("fixture/sources/controllers.scala")
}
