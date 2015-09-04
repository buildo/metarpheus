package morpheus
package extractors

import scala.meta._
import scala.meta.dialects.Scala211

package object route {

  case class Alias(term: internal.ast.Term, desc: Option[String])

  private val emptyTokens = Set(" ", "\\n", "comment")

  private def stripCommentMarkers(s: String) =
    s.stripPrefix("/")
      .dropWhile(_ == '*')
      .reverse
      .stripPrefix("/")
      .dropWhile(_ == '*')
      .reverse

  def extractAliases(source: scala.meta.Source): Map[String, Alias] = {

    import scala.meta.internal.ast._

    source.topDownBreak.collect {
      case x: Defn.Val if x.mods.collectFirst {
        case Mod.Annot(Ctor.Ref.Name("alias")) => ()
      }.isDefined => x
    }.map { case t@Defn.Val(
        _,
        List(Pat.Var.Term(Term.Name(name))),
        _,
        term: Term
      ) =>
      val tokenIdx = source.tokens.indexOf(t.tokens(0))
      val comment = source.tokens.take(tokenIdx).reverse
        .takeWhile(c => emptyTokens.contains(c.name))
        .find(_.name == "comment")
        .map(c => stripCommentMarkers(c.code).trim)
      (name, Alias(term, comment))
    }.toMap
  }
  
  def extractRouteTerms(source: scala.meta.Source
    ): List[(List[String], internal.ast.Term.ApplyInfix, internal.ast.Term)] = {

    import scala.meta.internal.ast._

    val routesTerms: List[Term] =
      source.topDownBreak.collect {
        case x: Defn.Val if x.mods.collectFirst {
          case Mod.Annot(Ctor.Ref.Name("publishroute")) => ()
        }.isDefined => x
      } match {
        case List(route) => 
          val Defn.Val(_, routePats, _, routeTerm) = route
          val Term.Block(List(routeStat : Term)) = routeTerm
          List(routeStat)
        case Nil =>
          Nil
      }

    def recurse(routesTerm: Term, prefix: List[String]
      ): List[(List[String], internal.ast.Term.ApplyInfix, internal.ast.Term)] = {

      val routeTerms = getAllInfix(routesTerm, "~")

      routeTerms.flatMap { 
        case Term.Apply(
          Term.Apply(
            Term.Name("pathPrefix"),
            List(Lit.String(addPrefix))
          ),
          List(Term.Block(List(t : Term)))
        ) => recurse(t, prefix :+ addPrefix)
        case Term.Apply(routeTpe : Term.ApplyInfix, List(routeTerm : Term)) =>
          List(
            (prefix, routeTpe, routeTerm)
          )
      }
    }

    routesTerms.flatMap(recurse(_, Nil))
  }

  def routeMatcherToTpe(name: String): internal.ast.Type = name match {
    case "IntNumber" => scala.meta.internal.ast.Type.Name("Int")
    case "Segment" => scala.meta.internal.ast.Type.Name("String")
  }

  private case class TooFewMatches() extends Exception
  private case class TooManyMatches() extends Exception
  private implicit class ListPimp[A](list: List[A]) {
    def getOne[B](pf: PartialFunction[A, B]) = 
      list.collect(pf) match {
        case List(one) => one
        case List() => throw TooFewMatches()
        case _ => throw TooManyMatches()
      }
  }

  def extractRoute(
    aliases: Map[String, Alias])(
    route: (List[String], internal.ast.Term.ApplyInfix, internal.ast.Term)) = {

    import scala.meta.internal.ast._

    val (prefix, rtpe, rterm) = route

    sealed trait Tag
    case class ParamDesc(name: String, desc: String) extends Tag

    val (desc, tags) = rtpe.tokens.find(_.name == "comment").map { c =>
      val cleanLines = stripCommentMarkers(c.code)
        .split("\n").map(_.trim.stripPrefix("*").trim)
        .filter(_ != "").toList

      val TagRegex = """@([^\s]+) (.*)""".r
      val ParamRegex = """@param ([^\s]+) (.*)""".r

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
            case ParamRegex(name, l1) => ParamDesc(name, (l1 :: tagls).mkString(" "))
          }
          getTags(acc :+ next, rest)
        }
      }

      (Some(desc.mkString(" ")), getTags(Nil, tagLines))
    }.getOrElse((None, List()))

    val rdirs = getAllInfix(rtpe, "&")

    sealed trait DirOut
    case class Method(method: String) extends DirOut
    case class Param(p: intermediate.RouteParam) extends DirOut
    case class Route(segments: List[intermediate.RouteSegment]) extends DirOut
    case class Body(b: intermediate.Route.Body) extends DirOut

    def extractParamTerm(
      applyType: Term.ApplyType, optional: Boolean, aliasDesc: Option[String]): intermediate.RouteParam = {

      val Term.ApplyType(
        Term.Select(Lit.Symbol(paramSym), Term.Name("as")),
        List(paramTpe: Type)
      ) = applyType
      val name = paramSym.name
      val desc = tags.collectFirst { case ParamDesc(`name`, d) => d }
        .orElse(aliasDesc)
      intermediate.RouteParam(
        name = Some(name),
        tpe = tpeToIntermediate(paramTpe),
        required = !optional,
        desc = desc)
    }

    val dirOut: List[DirOut] = rdirs.flatMap { term =>
      def extract(t: Term, aliasDesc: Option[String]): List[DirOut] = t match {
        case Term.Name(method) if List("get", "post").contains(method) =>
          List(Method(method))
        case Term.Apply(Term.Name("parameters" | "parameter"), paramTerms: List[Term]) =>
          paramTerms.map {
            case Term.Select(applyType: Term.ApplyType, Term.Name("?")) =>
              Param(extractParamTerm(applyType, true, aliasDesc))
            case applyType: Term.ApplyType =>
              Param(extractParamTerm(applyType, false, aliasDesc))
          }
        case Term.Name("pathEnd") =>
          List(Route(Nil))
        case Term.Apply(Term.Name("path"), List(pathTerm: Term)) =>
          val route = getAllInfix(pathTerm, "/").map {
            case Term.Name(segmentMatcher) =>
              intermediate.RouteSegment.Param(intermediate.RouteParam(
                name = None,
                tpe = (routeMatcherToTpe _).andThen(tpeToIntermediate _)(segmentMatcher),
                required = true,
                desc = None))
            case Lit.String(stringSegm) =>
              intermediate.RouteSegment.String(stringSegm)
          }
          List(Route(route))
        case Term.Apply(Term.Name("entity"), List(Term.ApplyType(Term.Name("as"), List(tpe: internal.ast.Type)))) =>
          List(Body(intermediate.Route.Body(
            tpeToIntermediate(tpe),
            None)))
        case Term.Name(name) if aliases.contains(name) =>
          extract(aliases(name).term, aliasDesc = aliases(name).desc)
      }
      extract(term, aliasDesc = None)
    }

    val Term.Apply(
      Term.Select(
        Term.ApplyType(
          Term.Name("returns"),
          List(returnTpe: Type)
        ),
        Term.Name("ctrl")
      ),
      List(ctrlTerm: Term)
    ) = rterm

    val Term.Eta(Term.Select(
      Term.Name(ctrlName1),
      Term.Name(ctrlName2)
    )) = ctrlTerm

    val segments = prefix.map(intermediate.RouteSegment.String) ++
      dirOut.getOne({ case Route(r) => r })

    val ctrl: List[String] = ctrlName1 :: ctrlName2 :: Nil

    intermediate.Route(
      method = dirOut.getOne({ case Method(m) => m }),
      route = segments,
      params = dirOut.collect({ case Param(p) => p }),
      returns = tpeToIntermediate(returnTpe),
      body = dirOut.collectFirst({ case Body(b) => b }),
      ctrl = ctrl,
      desc = desc)

  }

  def extractAllRoutes(f: scala.meta.Source): List[intermediate.Route] = {
    val aliases = extractAliases(f)
    extractRouteTerms(f).map(extractRoute(aliases) _)
  }

}
