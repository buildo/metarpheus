package morpheus
package extractors

import scala.meta._
import scala.meta.dialects.Scala211

package object route {

  case class Alias(term: internal.ast.Term, desc: Option[String])

  /**
   * Extract aliases: directives assigned to vals for use in routes.
   */
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
      val comment = findRelatedComment(source, t)
      (name, Alias(term, comment.map(token => stripCommentMarkers(token.code).trim)))
    }.toMap
  }

  case class RouteTermInfo(
    pathPrefix: List[String],
    authenticated: Boolean,
    routeTpe: internal.ast.Term.ApplyInfix,
    routeTerm: internal.ast.Term)
  
  /**
   * Find a router definition in a source file and extract a list of routes to
   * be parsed as a bundle of terms and metadata.
   *
   * routeTpe will contain the route directives
   * routeTerm will contain the parameter the route directives are applied to
   * (that is to say, the lambda to run when the route is matched).
   *
   * In the source:
   *
   * @publishRoute
   * val route = {
   *   (<routeTpe>) (<routeTerm>) ~
   *   (<routeTpe>) (<routeTerm>) ~
   *   ...
   * }
   *
   * prefix will contain a list of all "pathPrefix"es encountered
   * authenticated will be true if the "authenticated" param for @publishRoute
   * is true or if a "withUserAuthentication" directive has been encountered
   */
  def extractRouteTerms(source: scala.meta.Source): List[RouteTermInfo] = {

    import scala.meta.internal.ast._

    val routesTerms: List[(Term, Boolean)] = // (term, authenticated)
      source.topDownBreak.collect {
        case x: Defn.Val if x.mods.collectFirst {
          case Mod.Annot(
            Ctor.Ref.Name("publishRoute") |
            Term.Apply(
              Ctor.Ref.Name("publishRoute"),
              _
            )
          ) => ()
        }.isDefined => x
      } match {
        case List(route) => 
          val Defn.Val(_, routePats, _, routeTerm) = route
          val Term.Block(List(routeStat : Term)) = routeTerm
          val authenticated = route.mods.collectFirst {
            case Mod.Annot(
              Term.Apply(
                Ctor.Ref.Name("publishRoute"),
                appliedTo
              )
            ) if appliedTo.collectFirst {
              case Term.Arg.Named(Term.Name("authenticated"), Lit(true)) => ()
            }.isDefined => true
          }.getOrElse(false)
          List((routeStat, authenticated))
        case Nil =>
          Nil
      }

    def recurse(prefix: List[String])(routesTerm: Term, authenticated: Boolean
      ): List[RouteTermInfo] = {

      val routeTerms = getAllInfix(routesTerm, "~")

      routeTerms.flatMap { 
        case Term.Apply(
          Term.Apply(
            Term.Name("pathPrefix"),
            List(Lit(addPrefix: String))
          ),
          List(Term.Block(List(t : Term)))
        ) => recurse(prefix :+ addPrefix)(t, authenticated)
        case Term.Apply(
          Term.Name("withUserAuthentication"),
          List(Term.Block(List(Term.Select(t : Term, _))))
        ) => recurse(prefix)(t, true)
        case Term.Apply(routeTpe : Term.ApplyInfix, List(routeTerm : Term)) =>
          List(
            RouteTermInfo(prefix, authenticated, routeTpe, routeTerm)
          )
        case otherwise => println(otherwise.show[Structure]); ???
      }
    }

    routesTerms.flatMap(x => (recurse(Nil) _).tupled(x))
  }

  /**
   * Convert a spray route matcher to the corresponding resulting type
   */
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

  case class RouteCommentInfo(desc: Option[String], paramDescs: Map[String, String], routeName: Option[List[String]])

  /**
   * Extract relevant information from the route comment.
   */
  def extractRouteCommentInfo(rtpe: internal.ast.Term.ApplyInfix): RouteCommentInfo = {
    val (desc, tags) = extractDescAndTagsFromComment(
      rtpe.tokens.find(_.name == "comment"))

    val paramDescs = tags.collect { case ParamDesc(name, d) => name -> d }.toMap

    val routeName = tags.collectFirst { case RouteName(name) => name }

    RouteCommentInfo(desc, paramDescs, routeName)
  }

  /**
   * Extract the intermediate representation for a route from the output
   * of extractRouteTerms
   */
  def extractRoute(
    aliases: Map[String, Alias],
    models: List[intermediate.CaseClass])(
    route: RouteTermInfo,
    routeCommentInfo: RouteCommentInfo): intermediate.Route = {

    import scala.meta.internal.ast._

    val RouteTermInfo(prefix, authenticated, rtpe, rterm) = route

    val RouteCommentInfo(desc, paramDescs, routeName) = routeCommentInfo

    val rdirs = getAllInfix(rtpe, "&")

    /**
     * Represents the result of interpreting a directive
     */
    sealed trait DirOut
    case class Method(method: String) extends DirOut
    case class Param(p: intermediate.RouteParam) extends DirOut
    case class Route(segments: List[intermediate.RouteSegment]) extends DirOut
    case class Body(b: intermediate.Route.Body) extends DirOut

    def extractParamTerm(
      applyType: Term.ApplyType, optional: Boolean, aliasDesc: Option[String]): intermediate.RouteParam = {

      val Term.ApplyType(
        Term.Select(Lit(paramSym: Symbol), Term.Name("as")),
        List(paramTpe: Type)
      ) = applyType
      val name = paramSym.name
      val desc = paramDescs.get(name).orElse(aliasDesc)
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
        case Term.ApplyType(Term.Name("params"), Seq(Type.Name(typeName))) =>
          models.find(_.name == typeName).get.members.map {
            case intermediate.CaseClass.Member(name, tpe, desc) =>
              val (paramTpe, required) = tpe match {
                case intermediate.Type.Apply("Option", Seq(innerTpe)) =>
                  (innerTpe, false)
                case _ =>
                  (tpe, true)
              }
              Param(intermediate.RouteParam(
                name = Some(name),
                tpe = paramTpe,
                required = required,
                desc = desc))
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
            case Lit(stringSegm: String) =>
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
      authenticated = authenticated,
      returns = tpeToIntermediate(returnTpe),
      body = dirOut.collectFirst({ case Body(b) => b }),
      ctrl = ctrl,
      desc = desc,
      name = routeName.getOrElse(ctrl))

  }

  def extractAllRoutes(models: List[intermediate.CaseClass], overrides: Map[List[String], intermediate.Route])(f: scala.meta.Source): List[intermediate.Route] = {
    val aliases = extractAliases(f)
    extractRouteTerms(f).map { routeTerm =>
      val routeCommentInfo = extractRouteCommentInfo(routeTerm.routeTpe)
      routeCommentInfo.routeName.flatMap(overrides.get _)
        .getOrElse(extractRoute(aliases, models)(routeTerm, routeCommentInfo))
    }
  }

}
