package morpheus

object Fixtures {
  val models = morpheus.util.slurp("core/fixture/sources/models.scala")
  val routes = morpheus.util.slurp("core/fixture/sources/routes.scala")
  val controllers = morpheus.util.slurp("core/fixture/sources/controllers.scala")
}
