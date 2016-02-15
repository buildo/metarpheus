package morpheus.extractors
package model

import org.scalatest._

class ModelSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    morpheus.Fixtures.models.parse[Source]
  }

  test("extract case classes") {
    val result = extractModel(parsed)

    import morpheus.intermediate._

    assert(result ===
      List(
        CaseClass(
          name = "Camping",
          members = List(
            CaseClass.Member(
              name = "name",
              tpe = Type.Name("String"),
              desc = Some("camping name")),
            CaseClass.Member(
              name = "size",
              tpe = Type.Name("Int"),
              desc = Some("number of tents"))
          ),
          desc = Some("Represents a camping site")
        )
      )
    )
  }

}
