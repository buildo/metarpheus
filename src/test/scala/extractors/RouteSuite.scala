package morpheus.extractors
package route

import org.scalatest._

class RouteSuite extends FunSuite {
  def parse = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    Fixture.code.parse[Source]
  }

  test("parse successfully") {
    parse
  }
}
