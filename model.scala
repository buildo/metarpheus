package morpheus
package extractors

import scala.meta._
import scala.meta.dialects.Scala211

package object model {

  import scala.meta.internal.ast._

  case class CaseClassDefnInfo(defn: Defn.Class, commentToken: Option[scala.meta.Token])

  def extractCaseClassDefns(source: scala.meta.Source): List[CaseClassDefnInfo] = {
    source.topDownBreak.collect {
      case c:Defn.Class if c.mods.collectFirst {
        case Mod.Case() => ()
      }.isDefined => c
    }.map { cc =>
      val tokenIdx = source.tokens.indexOf(cc.tokens(0))
      val comment = findRelatedComment(source, cc)
      CaseClassDefnInfo(cc, comment)
    }
  }

  /**
   * Extract the intermediate representation for a case class from the output
   * of extractCaseClassDefns
   */
  def extractCaseClass(caseClassDefnInfo: CaseClassDefnInfo): intermediate.CaseClass = {
    val CaseClassDefnInfo(defn, comment) = caseClassDefnInfo
    val className = defn.name.value
    val Ctor.Primary(_, Ctor.Ref.Name("this"), List(plist)) = defn.ctor
    val (classDesc, tags) = extractDescAndTagsFromComment(comment)
    // FIXME fail if unmatched parameter descriptions are found
    val paramDescs = tags.collect { case p: ParamDesc => p }
    val members = plist.map {
      case Term.Param(_, Term.Name(name), Some(tpe : scala.meta.internal.ast.Type), _) =>
        intermediate.CaseClass.Member(
          name = name,
          tpe = tpeToIntermediate(tpe),
          desc = paramDescs.find(_.name == name).map(_.desc)
        )
    }.toList
    intermediate.CaseClass(
      name = className,
      members = members,
      desc = classDesc)
  }

  def extractModel(source: scala.meta.Source): List[intermediate.CaseClass] =
    extractCaseClassDefns(source).map(extractCaseClass _)

}
