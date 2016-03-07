package morpheus
package intermediate

import scala.annotation.tailrec

sealed trait Type
object Type {
  case class Name(name: String) extends Type
  case class Apply(name: String, args: Seq[Type]) extends Type
}

sealed trait Model {
  val name: String
}
case class CaseClass(
  name: String,
  members: List[CaseClass.Member],
  desc: Option[String]) extends Model
object CaseClass {
  case class Member(
    name: String, tpe: Type, desc: Option[String])
}

case class CaseEnum(
  name: String,
  values: List[CaseEnum.Member],
  desc: Option[String]) extends Model
object CaseEnum {
  case class Member(name: String, desc: Option[String])
}

case class RouteParam(
  name: Option[String],
  tpe: Type,
  required: Boolean,
  desc: Option[String])

sealed trait RouteSegment
case object RouteSegment {
  case class Param(routeParam: RouteParam) extends RouteSegment
  case class String(str: java.lang.String) extends RouteSegment
}

case class Route(
  method: String,
  route: List[RouteSegment],
  params: List[RouteParam],
  authenticated: Boolean,
  returns: Type,
  body: Option[Route.Body],
  ctrl: List[String],
  desc: Option[String],
  name: List[String])

object Route {
  case class Body(tpe: Type, desc: Option[String])
}

case class API(
  models: List[Model],
  routes: List[Route]) {

  def stripUnusedModels: API = {
    val modelsInUse: Set[intermediate.Type] = {
      routes.flatMap { route =>
        route.route.collect {
          case RouteSegment.Param(routeParam) => routeParam.tpe
        } ++
        route.params.map(_.tpe) ++
        List(route.returns) ++
        route.body.map(b => List(b.tpe)).getOrElse(Nil)
      }
    }.toSet

    def inUseConcreteTypeNames(models: Set[intermediate.Type]): Set[String] = {
      def recurse(t: intermediate.Type): List[intermediate.Type.Name] = t match {
        case name: intermediate.Type.Name => List(name)
        case intermediate.Type.Apply(_, args) => args.flatMap(recurse).toList
      }
      models.flatMap(recurse)
    }.map(_.name).toSet

    // recursively search for types in use till fixpoint is reached
    @tailrec
    def fixpoint(inUse: Set[intermediate.Type]): Set[String] = {
      val newInUse = inUse ++
        models.filter(m => inUseConcreteTypeNames(inUse).contains(m.name)).collect {
          case CaseClass(_, members, _) => members.map(_.tpe)
        }.flatMap(o => o)
      if (newInUse == inUse) inUseConcreteTypeNames(inUse)
      else fixpoint(newInUse)
    }

    val inUseNames = fixpoint(modelsInUse)

    this.copy(models = models.filter(m => inUseNames.contains(m.name)))
  }
}
