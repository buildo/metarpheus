package morpheus
package extractors

import scala.meta._
import scala.meta.dialects.Scala211

package object model {

  import scala.meta.internal.ast._

  def extractCaseClassDefns(source: scala.meta.Source): List[Defn.Class] = {
    source.topDownBreak.collect {
      case c:Defn.Class if c.mods.collectFirst {
        case Mod.Case() => ()
      }.isDefined => c
    }
  }

  /**
   * Extract the intermediate representation for a case class from the output
   * of extractCaseClassDefns
   */
  def extractCaseClass(defn: Defn.Class): intermediate.CaseClass = {
    val className = defn.name.value
    val Ctor.Primary(_, Ctor.Ref.Name("this"), List(plist)) = defn.ctor
    val members = plist.map {
      case Term.Param(_, Term.Name(name), Some(tpe : scala.meta.internal.ast.Type), _) =>
        intermediate.CaseClass.Member(
          name = name,
          tpe = tpeToIntermediate(tpe),
          desc = None // FIXME
        )
    }.toList
    intermediate.CaseClass(className, members)
  }

  def extractModel(source: scala.meta.Source): List[intermediate.CaseClass] =
    extractCaseClassDefns(source).map(extractCaseClass _)

}
