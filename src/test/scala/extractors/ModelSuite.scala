package morpheus.extractors
package model

import org.scalatest._

class ModelSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    Fixture.modelCode.parse[Source]
  }

  test("extract case classes") {
    val result = extractModel(parsed)

    import morpheus.intermediate._

    assert(result ===
      List(
        CaseClass(
          "Camping",
          List(
            CaseClass.Member("name", Type.Name("String"), None),
            CaseClass.Member("size", Type.Name("Int"), None)
          )
        )
      )
    )
  }

}
