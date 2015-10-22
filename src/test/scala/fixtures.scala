package morpheus.extractors

object Fixtures {
  private def slurp(name: String) = {
    val source = io.Source.fromFile(name)
    try source.mkString finally source.close()
  }
  val models = slurp("fixture/models.scala")
  val routes = slurp("fixture/routes.scala")
}
