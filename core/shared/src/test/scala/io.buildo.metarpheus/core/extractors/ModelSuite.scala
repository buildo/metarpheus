package io.buildo.metarpheus
package core
package test

import org.scalatest._
import ai.x.diff.DiffShow
import ai.x.diff.conversions._

import extractors._

class ModelSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    Fixtures.models.parse[Source].get
  }

  test("extract case classes") {
    val result = model.extractModel(parsed)

    import intermediate._

    val expected =
        List(
          CaseClass(
            name = "CampingName",
            members = List(
              CaseClass.Member(name = "s", tpe = Type.Name("String"), desc = None)
            ),
            desc = None,
            isValueClass = true,
          ),
          CaseClass(
            name = "Camping",
            members = List(
              CaseClass.Member(name = "id", tpe = Type.Name("UUID"), desc = None),
              CaseClass.Member(name = "name", tpe = Type.Name("CampingName"), desc = None),
              CaseClass
                .Member(name = "size", tpe = Type.Name("Int"), desc = Some("number of tents")),
              CaseClass.Member(
                name = "location",
                tpe = Type.Name("CampingLocation"),
                desc = Some("camping location")
              ),
              CaseClass.Member(
                name = "rating",
                tpe = Type.Name("CampingRating"),
                desc = Some("camping rating")
              ),
              CaseClass.Member(
                name = "a",
                tpe = Type.Name("A"),
                desc = None
              )
            ),
            desc = Some("Represents a camping site"),
            typeParams = List(
              Type.Name("A")
            )
          ),
          CaseClass(
            name = "Swan",
            members = List(
              CaseClass.Member(
                name = "color",
                tpe = Type.Name("String"),
                desc = Some("color of the swan")
              )
            ),
            desc = Some("Represents a swan")
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
            name = "CampingRating",
            values = List(
              CaseEnum.Member(
                name = "High",
                desc = Some("High")
              ),
              CaseEnum.Member(
                name = "Medium",
                desc = Some("Medium")
              ),
              CaseEnum.Member(
                name = "Low",
                desc = Some("Low")
              )
            ),
            desc = Some("Rating of the camping site")
          ),
          CaseEnum(
            name = "Planet",
            values = List(
              CaseEnum.Member(
                name = "Earth",
                desc = Some("Earth is a blue planet")
              ),
              CaseEnum.Member(
                name = "Another",
                desc = Some("Not sure campings exist")
              )
            ),
            desc = Some("Planet of the camping site")
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
    val comparison = DiffShow.diff[List[Model]](expected, result)
    assert(comparison.isIdentical, comparison.string)
  }

}
