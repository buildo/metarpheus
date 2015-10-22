package io.buildo.baseexample

import morpheus.annotation.{publishroute, alias}

import models._

import spray.routing._
import spray.routing.Directives._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.ExecutionContext.Implicits.global

trait CampingRouterModule extends io.buildo.base.MonadicCtrlRouterModule
  with io.buildo.base.MonadicRouterHelperModule
  with io.buildo.base.ConfigModule
  with JsonSerializerModule
  
  with CampingControllerModule {

  import ExampleJsonProtocol._
  import RouterHelpers._

  /** whether there's a beach */
  @alias val `?hasBeach` = parameter('hasBeach.as[Boolean])

  @publishroute(authenticated = false)
  val campingRoute = {
    pathPrefix("campings") {
      (get & pathEnd & parameters('coolness.as[String], 'size.as[Int].?) /**
        get campings matching the requested coolness and size
        @param coolness how cool it is
        @param size the number of tents
      */) (returns[List[Camping]].ctrl(campingController.getByCoolnessAndSize _)) ~
      withUserAuthentication {
        (get & path(IntNumber) /**
          get a camping by id
        */) (returns[Camping].ctrl(campingController.getById _)) ~
      } ~
      (get & pathEnd & `?hasBeach` /**
        get campings based on whether they're close to a beach
      */) (returns[List[Camping]].ctrl(campingController.getByHasBeach _)) ~
      (post & pathEnd & entity(as[Camping]) /**
        create a camping
      */) (returns[Camping].ctrl(campingController.create _))
    }
  }
}

