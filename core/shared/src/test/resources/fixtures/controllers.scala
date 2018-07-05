package io.buildo.baseexample

package controllers

import models._

import wiro.OperationParameters
import wiro.annotation._

import scala.concurrent.{Future, ExecutionContext}

@path("campings")
trait CampingController {

  /**
    * get campings matching the requested coolness and size
    * @param coolness how cool it is
    * @param size the number of tents
    * @param nickname a friendly name for the camping
    */
  @query
  def getByCoolnessAndSize(
    coolness: String,
    size: Option[Int],
    nickname: String
  ): Future[Either[String, List[Camping]]]

  /**
    * get campings matching the requested size and distance
    * @param size the number of tents
    * @param distance how distant it is
    */
  @query
  def getBySizeAndDistance(size: Int, distance: Int): Future[Either[String, List[Camping]]]

  /**
    * get a camping by id
    * @param id camping id
    */

  @query
  def getById(id: Int, token: Auth): Future[Either[String, Camping]]

  /**
    * get a camping by typed id
    */
  @query
  def getByTypedId(token: Auth, id: `Id`[Camping]): Future[Either[String, Camping]]

  /**
    * get campings based on whether they're close to a beach
    * @param hasBeach whether there's a beach
    */
  @query
  def getByHasBeach(hasBeach: Boolean): Future[Either[String, List[Camping]]]

  /**
    * create a camping
    */
  @command
  def create(camping: Camping, parameters: OperationParameters): Future[Either[String, Camping]]
}

class CampingControllerImpl(
  implicit
  executionContext: ExecutionContext
) extends CampingController {

  @query
  def getByCoolnessAndSize(
    coolness: String,
    size: Int,
    nickname: String
  ): Future[Either[String, List[Camping]]] = ???

  @query
  def getBySizeAndDistance(size: Int, distance: Int): Future[Either[String, List[Camping]]] = ???

  @query
  def getById(id: Int): Future[Either[String, Camping]] = ???

  @query
  def getByTypedId(id: `Id`[Camping]): Future[Either[String, Camping]] = ???

  @query
  def getByHasBeach(hasBeach: Boolean): Future[Either[String, List[Camping]]] = ???

  @command
  def create(camping: Camping): Future[Either[String, Camping]] = ???
}
