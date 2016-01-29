package morpheus

package object util {
  def slurp(name: String) = {
    val source = io.Source.fromFile(name)
    try source.mkString finally source.close()
  }
}
