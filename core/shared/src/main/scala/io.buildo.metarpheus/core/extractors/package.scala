package io.buildo.metarpheus
package core

import scala.meta.{Type, Term, Syntax}
import scala.meta.contrib.AssociatedComments

package object extractors {

  def extractFullAPI(
    parsed: List[scala.meta.Source]
  ): intermediate.API = {

    val models: List[intermediate.Model] =
      parsed.flatMap(extractors.model.extractModel)

    val caseClasses: List[intermediate.CaseClass] =
      models.collect { case x: intermediate.CaseClass => x }

    val routes: List[intermediate.Route] =
      parsed.flatMap(extractors.controller.extractAllRoutes)

    intermediate.API(models, routes)
  }

  /**
    * Extract all terms from a sequence of applications of an infix operator
    * (which translates to nested `ApplyInfix`es).
    * e.g. getAllInfix(t1 + t2 + t3 + t4, "+") results in List(t1, t2, t3, t4)
    */
  private[extractors] def getAllInfix(ainfix: Term, op: String): List[Term] = {
    import scala.meta._
    ainfix match {
      case Term.ApplyInfix(subinfix: Term.ApplyInfix, Term.Name(`op`), Nil, List(term: Term)) =>
        getAllInfix(subinfix, `op`) :+ term
      case Term.ApplyInfix(term1: Term, Term.Name(`op`), Nil, List(term2: Term)) =>
        term1 :: term2 :: Nil
      case term: Term => term :: Nil
    }
  }

  /*
   * Convert a scala-meta representation of a type to a metarpheus
   * intermediate representation
   */
  private[extractors] def tpeToIntermediate(tpe: Type): intermediate.Type = tpe match {
    case name: scala.meta.Type.Name =>
      intermediate.Type.Name(name.value)
    case scala.meta.Type.Apply(name: scala.meta.Type.Name, args) =>
      intermediate.Type.Apply(name.value, args.map(tpeToIntermediate))
    case scala.meta.Type.Select(_, t) => tpeToIntermediate(t)
  }

  private[extractors] def tpeToIntermediate(t: Term.ApplyType): intermediate.Type = t match {
    case Term.ApplyType(name: Term.Name, targs) =>
      intermediate.Type.Apply(name.value, t.targs.map(tpeToIntermediate))
  }

  private[extractors] def typeParamToIntermediate(t: Type.Param): intermediate.Type =
    t.tparams match {
      case Nil => intermediate.Type.Name(t.name.value)
      case tparams => intermediate.Type.Apply(t.name.value, tparams.map(typeParamToIntermediate))
    }

  private[extractors] def stripCommentMarkers(s: String) =
    s.stripPrefix("/")
      .dropWhile(_ == '*')
      .reverse
      .stripPrefix("/")
      .dropWhile(_ == '*')
      .reverse

  /*
   * Search for the comment associated with this definition
   */
  private[extractors] def findRelatedComment(
    source: scala.meta.Source,
    t: scala.meta.Tree
  ): Option[scala.meta.Token] =
    AssociatedComments(source.tokens).leading(t).headOption

  private[extractors] sealed trait Tag
  private[extractors] case class ParamDesc(name: String, desc: Option[String]) extends Tag
  private[extractors] case class PathParamDesc(name: String, desc: Option[String]) extends Tag
  private[extractors] case class RouteName(name: List[String]) extends Tag

  /**
    * Extract route description and tags (such as @param) from route comment
    */
  private[extractors] def extractDescAndTagsFromComment(
    token: Option[scala.meta.Token]
  ): (Option[String], List[Tag]) =
    token
      .map { c =>
        val cleanLines = stripCommentMarkers(c.show[Syntax])
          .split("\n")
          .map(_.trim.stripPrefix("*").trim)
          .filter(_ != "")
          .toList

        val TagRegex = """@([^\s]+) (.*)""".r
        val ParamRegex = """@param ([^\s]+) (.+)""".r
        val ParamRegexNoDesc = """@param ([^\s]+)""".r
        val PathParamRegex = """@pathParam ([^\s]+) (.+)""".r
        val PathParamRegexNoDesc = """@pathParam ([^\s]+)""".r
        val RouteNameRegex = """@name ([^\s]+)""".r

        val (desc, tagLines) = cleanLines.span(_ match {
          case TagRegex(_, _) => false
          case _ => true
        })

        @annotation.tailrec
        def getTags(acc: List[Tag], lines: List[String]): List[Tag] = lines match {
          case Nil => acc
          case l :: ls => {
            val (tagls, rest) = ls.span(_ match {
              case TagRegex(tag, rest) => false
              case _ => true
            })
            val next = l match {
              case ParamRegex(name, l1) => ParamDesc(name, Some((l1 :: tagls).mkString(" ")))
              case ParamRegexNoDesc(name) => ParamDesc(name, None)
              case PathParamRegex(name, l1) =>
                PathParamDesc(name, Some((l1 :: tagls).mkString(" ")))
              case PathParamRegexNoDesc(name) => PathParamDesc(name, None)
              case RouteNameRegex(name) => RouteName(name.split("""\.""").toList)
            }
            getTags(acc :+ next, rest)
          }
        }

        (Some(desc.mkString(" ")), getTags(Nil, tagLines))
      }
      .getOrElse((None, List()))
}
