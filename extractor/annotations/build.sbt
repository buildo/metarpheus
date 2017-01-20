organization  := "io.buildo"

name := "metarpheus-annotations"

version       := "0.1.0"

scalaVersion  := "2.11.8"

crossScalaVersions := Seq("2.11.8", "2.12.1")

scalacOptions := Seq("-unchecked",
                     "-deprecation",
                     "-encoding", "utf8",
                     "-feature")

bintrayOrganization := Some("buildo")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
