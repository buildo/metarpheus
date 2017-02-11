organization  := "io.buildo"

name := "metarpheus"

version       := "0.1.0"

scalaVersion  := "2.12.1"

scalacOptions := Seq("-unchecked",
                     "-deprecation",
                     "-encoding", "utf8",
                     "-feature")

resolvers ++= Seq(
  Resolver.bintrayIvyRepo("scalameta", "maven")
)

libraryDependencies ++= Seq(
  "org.scalameta" %% "scalameta" % "1.6.0-671",
  "org.scalameta" %% "contrib" % "1.6.0-671",
  "org.rogach" %% "scallop" % "1.0.0",
  "org.json4s" %% "json4s-jackson" % "3.5.0",
  "com.twitter" %% "util-eval" % "6.41.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

bintrayOrganization := Some("buildo")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
