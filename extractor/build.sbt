organization  := "io.buildo"

name := "metarpheus"

version       := "0.1.0"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-unchecked",
                     "-deprecation",
                     "-encoding", "utf8",
                     "-feature")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  DefaultMavenRepository
)

libraryDependencies ++= Seq(
  "org.scalameta" % "scalameta" % "0.20.0" cross CrossVersion.binary,
  "org.rogach" %% "scallop" % "1.0.0",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.twitter" %% "util-eval" % "6.33.0",
  "org.scalatest" %% "scalatest" % "2.1.3" % "test"
)

bintrayOrganization := Some("buildo")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
