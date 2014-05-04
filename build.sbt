organization := "me.lessis"

version := "0.1.0-SNAPSHOT"

crossScalaVersions := Seq("2.9.3", "2.10.4", "2.11.0")

lazy val root = project.in(file("."))
  .settings(
    publish := {}, test := {}, publishLocal := {}
  )
  .aggregate(core, coreTests, netty3, netty, twttr, testing)

lazy val core = Common.module("core")
  .settings(test := {})

lazy val testing = Common.module("testing")
  .settings(publish := {}, test := {})
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
