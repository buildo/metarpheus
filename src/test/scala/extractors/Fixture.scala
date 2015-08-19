package morpheus.extractors

object Fixture {
  val routeCode = """
  |package io.buildo.baseexample

  |import morpheus.annotation.publishroute

  |import models._

  |import spray.routing._
  |import spray.routing.Directives._
  |import spray.httpx.SprayJsonSupport._

  |import scala.concurrent.ExecutionContext.Implicits.global

  |trait CampingRouterModule extends io.buildo.base.MonadicCtrlRouterModule
  |  with io.buildo.base.MonadicRouterHelperModule
  |  with io.buildo.base.ConfigModule
  |  with JsonSerializerModule
  |  
  |  with CampingControllerModule {

  |  import ExampleJsonProtocol._
  |  import RouterHelpers._

  |  @publishroute
  |  val campingRoute = {
  |    pathPrefix("campings") {
  |      (get & pathEnd & parameters('coolness.as[String], 'size.as[Int].?) /**
  |        get campings matching the requested coolness and size
  |        @param coolness how cool it is
  |        @param size the number of tents
  |      */) (returns[List[Camping]].ctrl(campingController.getByCoolnessAndSize _)) ~
  |      (get & path(IntNumber) /**
  |        get a camping by id
  |      */) (returns[Camping].ctrl(campingController.getById _)) ~
  |      (post & pathEnd & entity(as[Camping]) /**
  |        create a camping
  |      */) (returns[Camping].ctrl(campingController.create _))
  |    }
  |  }
  |}
  |""".stripMargin

  val modelCode = """
  |package io.buildo.baseexample
  |
  |package models
  |
  |case class Camping(name: String, size: Int)
  |""".stripMargin
}
