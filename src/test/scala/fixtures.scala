package morpheus

object Fixtures {
  val models = morpheus.util.slurp("fixture/models.scala")
  val routes = morpheus.util.slurp("fixture/routes.scala")
}
