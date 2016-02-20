package io.buildo.baseexample

package models

/**
 * Represents a camping site
 * @param name camping name
 * @param size number of tents
 */
case class Camping(name: String, size: Int)

/*
 * Location of the camping site
 */
@enum trait CampingLocation {
  /* Near the sea */
  object Seaside
  /* High up */
  object Mountains
}

/*
 * Surface of the camping site
 */
sealed trait Surface
object Surface {
  /* Sandy */
  case object Sand extends Surface
  /* Dirt */
  case object Earth extends Surface
}
