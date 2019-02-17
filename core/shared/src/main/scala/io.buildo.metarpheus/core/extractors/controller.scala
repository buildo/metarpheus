package io.buildo.metarpheus
package core
package extractors

import scala.meta._
import scala.meta.contrib._

package object controller {

  implicit val extractVarMods: Extract[Decl.Def, Mod] =
    Extract(_.mods)

  private[this] def extractMethod(m: Decl.Def): String =
    m.mods.collectFirst {
      case Mod.Annot(Ctor.Ref.Name("query")) => "get"
      case Mod.Annot(Ctor.Ref.Name("command")) => "post"
    }.get

  private[this] val authType = Type.Name("Auth")
  private[this] val operationParametersType = Type.Name("OperationParameters")

  private[this] def isAuthParam(param: scala.meta.Term.Param): Boolean =
    param.decltpe.exists(_.isEqual(authType))

  private[this] def isOperationParameters(param: scala.meta.Term.Param): Boolean =
    param.decltpe.exists(_.isEqual(operationParametersType))

  private[this] def extractAuthenticated(m: Decl.Def): Boolean =
    m.paramss.headOption.exists(_.exists(isAuthParam))

  private[this] def extractParams(
    m: Decl.Def,
    paramsDesc: List[ParamDesc],
    inBody: Boolean
  ): List[intermediate.RouteParam] = {
    m.paramss.headOption
      .map { params =>
        params
          .filterNot(isAuthParam)
          .filterNot(isOperationParameters)
          .map { p =>
            val (tpe, required) = p.decltpe.collectFirst {
              case Type.Apply(Type.Name("Option"), Seq(t)) => (tpeToIntermediate(t), false)
              case t: Type => (tpeToIntermediate(t), true)
            }.head

            val name = p.name.syntax
            intermediate.RouteParam(
              name = Option(p.name.syntax),
              tpe = tpe,
              required = required,
              desc = paramsDesc.find(_.name == name).flatMap(_.desc),
              inBody = inBody
            )
          }
      }
      .getOrElse(Nil)
      .toList
  }

  private[this] def extractReturnType(m: Decl.Def): intermediate.Type =
    m.decltpe
      .collect {
        case Type.Apply(Type.Name(_), Seq(Type.Apply(Type.Name(_), Seq(_, tpe)))) =>
          tpeToIntermediate(tpe)
        case Type.Apply(Type.Name(_), Seq(tpe)) =>
          tpeToIntermediate(tpe)
      }
      .headOption
      .getOrElse {
        throw new Exception(s"""
          |This method misses an explicit return type
          |
          |  ${m.syntax}
          |
          |The return type can be of two types:
          | - F[E[_, Result]]
          | - F[Result],
        """.stripMargin)
      }

  def extractAllRoutes(source: Source): List[intermediate.Route] =
    source.collect { case t: Defn.Trait => t }.flatMap(t => extractRoute(source, t))

  def extractRoute(source: Source, t: Defn.Trait): List[intermediate.Route] = {
    val methods = t.collect {
      case m: Decl.Def
          if (m.hasMod(mod"@query") || m.hasMod(mod"@command")) &&
            !m.hasMod(mod"@metarpheusIgnore") =>
        m
    }

    val (controllerName, name) = t.mods
      .collectFirst {
        case Mod.Annot(Term.Apply(Ctor.Ref.Name("path"), Seq(Lit(n: String)))) => (t.name.value, n)
      }
      .getOrElse(t.name.value, t.name.value)

    methods.map { m =>
      val scaladoc = findRelatedComment(source, m)
      val (desc, tagsDesc) = extractDescAndTagsFromComment(scaladoc)
      val paramsDesc = tagsDesc.collect { case d: ParamDesc => d }
      val method = extractMethod(m)
      intermediate.Route(
        method = method,
        route = List(
          intermediate.RouteSegment.String(name),
          intermediate.RouteSegment.String(m.name.syntax)
        ),
        params = extractParams(m, paramsDesc, inBody = method == "post"),
        authenticated = extractAuthenticated(m),
        returns = extractReturnType(m),
        // FIXME: this is the only case in which we don't preserve the retro-compatibility
        // This is because intermediate.Body is too limiting as it assumes the body has a single Type
        // whereas we want to support bodies composed by multiple parameters each with their own Type
        // We're moving towards removing this node completely and instead merging the body params with
        // params, flagging them with a new `inBody` property.
        body = None,
        ctrl = List(
          // FIXME: well this is kind of a retrocompatibility hack
          // When parsing the routes we use the actual name of the controller variable in scope
          // whereas here's we're deriving it from the name of the trait. They should match in the
          // usual case, nonetheless it may introduce some diffs when migrating a project to wiro
          controllerName.substring(0, 1).toLowerCase() + controllerName.substring(1),
          m.name.syntax
        ),
        desc = desc,
        name = List(
          // FIXME: same as a above, for the time being we preserved the `controllerName.method`
          // semantic, but these should really be prefixed using `name` instead of `controllerName`
          controllerName.substring(0, 1).toLowerCase() + controllerName.substring(1),
          m.name.syntax
        )
      )
    }
  }

}
