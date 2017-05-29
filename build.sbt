enablePlugins(GitVersioning)

lazy val commonSettings = Seq(
  organization  := "io.buildo",
  scalaVersion := "2.12.1",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayOrganization := Some("buildo"),
  bintrayVcsUrl := Some("git@github.com:buildo/metarpheus"),
  releaseCrossBuild := true,
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-encoding", "utf8",
    "-feature"
  )
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(noPublishSettings)
  .aggregate(annotations)
  .dependsOn(annotations)

lazy val core = project
  .settings(commonSettings)
  .settings(
    name := "metarpheus",
    resolvers += Resolver.bintrayIvyRepo("scalameta", "maven"),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta" % "1.6.0-671",
      "org.scalameta" %% "contrib" % "1.6.0-671",
      "org.rogach" %% "scallop" % "1.0.0",
      "org.json4s" %% "json4s-jackson" % "3.5.0",
      "com.twitter" %% "util-eval" % "6.41.0",
      "org.scalatest" %% "scalatest" % "3.0.1" % Test
    )
  )

lazy val annotations = project
  .settings(commonSettings)
  .settings(
    name := "metarpheus-annotations"
  )
