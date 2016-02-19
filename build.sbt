organization  := "io.buildo"

name := "metarpheus"

version       := "0.0.1-SNAPSHOT"

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
  "org.scalameta" % "scalameta" % "0.1.0-SNAPSHOT" cross CrossVersion.binary,
  "org.scalatest" %% "scalatest" % "2.1.3" % "test",
  "org.json4s" %% "json4s-jackson" % "3.2.11"
)

