package morpheus
package extractors

import scala.meta._
import scala.meta.dialects.Scala211

package object route {

  case class Alias(term: Term, desc: Option[String])

  /**
   * Extract aliases: directives assigned to vals for use in routes.
   */
  def extractAliases(source: scala.meta.Source): Map[String, Alias] = {

    source.collect {
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
      (name, Alias(term, comment.map(token => stripCommentMarkers(token.show[Syntax]).trim)))
    }.toMap
  }

  case class RouteTermInfo(
    pathPrefix: List[String],
    authenticated: Boolean,
    routeTpe: Term.ApplyInfix,
    routeTerm: Term)

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
  def extractRouteTerms(source: scala.meta.Source, authRouteTermNames: List[String]): List[RouteTermInfo] = {

    val routesTerms: List[(Term, Boolean)] = // (term, authenticated)
      source.collect {
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
          val routeStat = routeTerm match {
            case Term.Block(List(routeStat: Term)) => routeStat
            case routeStat: Term.Apply => routeStat
          }
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
          List(Term.Block(List(t: Term)))
        ) => recurse(prefix :+ addPrefix)(t, authenticated)
        case x@(Term.Apply(
          Term.Name(termName),
          List(Term.Block(List(Term.Select(t: Term, _)))))
        ) if authRouteTermNames.contains(termName) => recurse(prefix)(t, true)
        case Term.Apply(
          Term.Apply(Term.Name(termName), _),
          List(Term.Block(List(t: Term)))
        ) if authRouteTermNames.contains(termName) => recurse(prefix)(t, true)
        case Term.Apply(
          Term.Apply(Term.Name(termName), _),
          List(Term.Block(List(t: Term)))
        ) if authRouteTermNames.contains(termName) => recurse(prefix)(t, true)
        case Term.Function(_, Term.Block(List(t: Term))) => recurse(prefix)(t, authenticated)
        case Term.Apply(routeTpe : Term.ApplyInfix, List(routeTerm : Term)) =>
          List(
            RouteTermInfo(prefix, authenticated, routeTpe, routeTerm)
          )
        case otherwise => println(otherwise.show[Structure]); println(authRouteTermNames); ???
      }
    }

    routesTerms.flatMap(x => (recurse(Nil) _).tupled(x))
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

  case class RouteCommentInfo(
    desc: Option[String],
    paramDescs: Map[String, Option[String]],
    pathParamNamesAndDescs: List[(String, Option[String])],
    routeName: Option[List[String]])

  /**
   * Extract relevant information from the route comment.
   */
  def extractRouteCommentInfo(rtpe: Term.ApplyInfix): RouteCommentInfo = {
    val (desc, tags) = extractDescAndTagsFromComment(
      rtpe.tokens.find(_.is[Token.Comment]))

    val paramDescs = tags.collect { case ParamDesc(name, d) => name -> d }.toMap
    val pathParamNamesAndDescs = tags.collect { case PathParamDesc(name, d) => name -> d }
    val routeName = tags.collectFirst { case RouteName(name) => name }

    RouteCommentInfo(desc, paramDescs, pathParamNamesAndDescs, routeName)
  }

  /**
   * Extract the intermediate representation for a route from the output
   * of extractRouteTerms
   */
  def extractRoute(
    aliases: Map[String, Alias],
    models: List[intermediate.CaseClass],
    routeMatcherToIntermediate: PartialFunction[(String, Option[intermediate.Type]), intermediate.Type])(
    route: RouteTermInfo,
    routeCommentInfo: RouteCommentInfo): intermediate.Route = {

    val RouteTermInfo(prefix, authenticated, rtpe, rterm) = route

    val RouteCommentInfo(desc, paramDescs, pathParamNamesAndDescs, routeName) = routeCommentInfo

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
        Term.Select(Lit(paramSym: scala.Symbol), Term.Name("as")),
        List(paramTpe: Type)
      ) = applyType
      val name = paramSym.name
      val desc = paramDescs.get(name).getOrElse(aliasDesc)
      intermediate.RouteParam(
        name = Some(name),
        tpe = tpeToIntermediate(paramTpe),
        required = !optional,
        desc = desc)
    }

    def extractSimpleParamTerm(name: String, aliasDesc: Option[String]): intermediate.RouteParam =
      intermediate.RouteParam(
        name = Some(name),
        tpe = intermediate.Type.Name("String"),
        required = true,
        desc = paramDescs.get(name).getOrElse(aliasDesc)
      )

    val defaultRouteMatchers = Map(
      "IntNumber" -> intermediate.Type.Name("Int"),
      "Segment" -> intermediate.Type.Name("String")
    )

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
            case Lit(sym: scala.Symbol) => Param(extractSimpleParamTerm(sym.name, aliasDesc))
            case Lit(name: String) => Param(extractSimpleParamTerm(name, aliasDesc))
          }
        case Term.Apply(Term.Select(p, Term.Name("as")), _)  => extract(p, aliasDesc)
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
                tpe = defaultRouteMatchers.get(segmentMatcher).getOrElse(
                  routeMatcherToIntermediate((segmentMatcher, None))),
                required = true,
                desc = None))
            case Lit(stringSegm: String) =>
              intermediate.RouteSegment.String(stringSegm)
            case Term.ApplyType(Term.Name(segmentMatcher), Seq(typ)) =>
              intermediate.RouteSegment.Param(intermediate.RouteParam(
                name = None,
                tpe = routeMatcherToIntermediate((segmentMatcher, Some(tpeToIntermediate(typ)))),
                required = true,
                desc = None))
          }
          val result = pathParamNamesAndDescs match {
            case Nil => route
            case _ => (route.foldLeft((pathParamNamesAndDescs, List[intermediate.RouteSegment]())) {
              case (((name, desc) :: pps, acc), intermediate.RouteSegment.Param(routeParam)) =>
                (pps,
                  acc :+ intermediate.RouteSegment.Param(routeParam.copy(name = Some(name), desc = desc)))
              case ((pps, acc), segment) => (pps, acc :+ segment)
            })._2
          }
          List(Route(result))
        case Term.Apply(Term.Name("entity"), List(Term.ApplyType(Term.Name("as"), List(tpe: Type)))) =>
          List(Body(intermediate.Route.Body(
            tpeToIntermediate(tpe),
            None)))
        case Term.Name(name) if aliases.contains(name) =>
          extract(aliases(name).term, aliasDesc = aliases(name).desc)
        case otherwise => println(otherwise.show[Structure]); ???
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

    val ctrl: List[String] = ctrlTerm match {
      case Term.Eta(Term.Select(Term.Name(x), Term.Name(y))) => List(x, y)
      case Term.Select(Term.Name(x), Term.Name(y)) => List(x, y)
      case Term.Name(x) => List(x)
      case _ => println(ctrlTerm.show[Structure]); ???
    }

    val segments = prefix.map(intermediate.RouteSegment.String) ++
      dirOut.getOne({ case Route(r) => r })

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

  def extractAllRoutes(models: List[intermediate.CaseClass], overrides: Map[List[String], intermediate.Route], routeMatcherToIntermediate: PartialFunction[(String, Option[intermediate.Type]), intermediate.Type], authRouteTermNames: List[String])(f: scala.meta.Source): List[intermediate.Route] = {
    val aliases = extractAliases(f)
    extractRouteTerms(f, authRouteTermNames).map { routeTerm =>
      val routeCommentInfo = extractRouteCommentInfo(routeTerm.routeTpe)
      routeCommentInfo.routeName.flatMap(overrides.get _)
        .getOrElse(extractRoute(aliases, models, routeMatcherToIntermediate)(routeTerm, routeCommentInfo))
    }
  }

}
