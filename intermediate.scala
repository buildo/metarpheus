package morpheus
package intermediate

sealed trait Type
object Type {
  case class Name(name: String) extends Type
  case class Apply(name: String, args: Seq[Type]) extends Type
}

case class CaseClass(
  name: String, members: List[CaseClass.Member])
object CaseClass {
  case class Member(
    name: String, tpe: Type, desc: Option[String])
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
  returns: Type,
  body: Option[Route.Body],
  ctrl: List[String],
  desc: Option[String])
object Route {
  case class Body(tpe: Type, desc: Option[String])
}
