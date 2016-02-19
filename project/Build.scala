import sbt._
import Keys._

object NozzleBuild extends Build {
  lazy val root = project.in(file("."))

  lazy val annotations = project.in(file("annotations"))
}
