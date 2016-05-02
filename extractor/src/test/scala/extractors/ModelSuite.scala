package morpheus.extractors
package model

import org.scalatest._

class ModelSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    morpheus.Fixtures.models.parse[Source].get
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
              desc = None),
            CaseClass.Member(
              name = "size",
              tpe = Type.Name("Int"),
              desc = Some("number of tents")),
            CaseClass.Member(
              name = "location",
              tpe = Type.Name("CampingLocation"),
              desc = Some("camping location"))
          ),
          desc = Some("Represents a camping site")
        ),
        CaseEnum(
          name = "CampingLocation",
          values = List(
            CaseEnum.Member(
              name = "Seaside",
              desc = Some("Near the sea")
            ),
            CaseEnum.Member(
              name = "Mountains",
              desc = Some("High up")
            )
          ),
          desc = Some("Location of the camping site")
        ),
        CaseEnum(
          name = "Surface",
          values = List(
            CaseEnum.Member(
              name = "Sand",
              desc = Some("Sandy")
            ),
            CaseEnum.Member(
              name = "Earth",
              desc = Some("Dirt")
            )
          ),
          desc = Some("Surface of the camping site")
        )
      )
    )
  }

}
