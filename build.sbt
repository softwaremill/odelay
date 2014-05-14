organization in ThisBuild := "me.lessis"

version in ThisBuild := "0.1.0-SNAPSHOT"

crossScalaVersions in ThisBuild := Seq("2.9.3", "2.10.4", "2.11.0")

scalaVersion in ThisBuild := crossScalaVersions.value.last

scalacOptions in ThisBuild ++= Seq(Opts.compile.deprecation)

licenses in ThisBuild := Seq(("MIT", url("https://github.com/softprops/odelay/blob/${version.value}/LICENSE")))

lazy val root = project.in(file("."))
  .settings(
    publish := {}, test := {}, publishLocal := {}
  )
  .aggregate(core, coreTests, netty3, netty, twttr, testing)

lazy val core = Common.module("core")

lazy val testing = Common.module("testing")
  .dependsOn(core)

lazy val coreTests = Common.module("core-tests")
  .settings(publish := {})
  .dependsOn(testing % "test->test;compile->compile")
 
lazy val netty3 = Common.module("netty3")
  .dependsOn(core, testing % "test->test")

lazy val netty = Common.module("netty")
  .dependsOn(core, testing % "test->test")
  
lazy val twttr = Common.module("twitter")
  .dependsOn(core, testing % "test->test")
