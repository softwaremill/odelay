organization in ThisBuild := "me.lessis"

version in ThisBuild := "0.1.0-SNAPSHOT"

crossScalaVersions in ThisBuild := Seq("2.9.3", "2.10.4", "2.11.1")

scalaVersion in ThisBuild := crossScalaVersions.value.last

scalacOptions in ThisBuild ++= Seq(Opts.compile.deprecation) ++
  Seq("-Ywarn-unused-import", "-Ywarn-unused", "-Xlint", "-feature").filter(
    Function.const(scalaVersion.value.startsWith("2.11")))

licenses in ThisBuild := Seq(
  ("MIT", url("https://github.com/softprops/odelay/blob/${version.value}/LICENSE")))

// a default aggregated build should exist for all defined projects
// https://github.com/sbt/sbt/blob/137805ed019b24dcbdefeef730d07f9c22c4e493/main/src/main/scala/sbt/Load.scala#L422-L428
// https://github.com/sbt/sbt/blob/c5d0c4cd29621c64532b4bce5150ef3b2e2b86e7/main/src/main/scala/sbt/Build.scala#L60
// https://github.com/sbt/sbt/blob/c5d0c4cd29621c64532b4bce5150ef3b2e2b86e7/main/src/main/scala/sbt/Build.scala#L48

val commonSettings = bintraySettings

lazy val `odelay-core` =
  project

lazy val `odelay-testing` =
  project.dependsOn(`odelay-core`)
         .settings(commonSettings:_*)

lazy val `odelay-core-tests` =
  project.settings(publish := {})
         .dependsOn(`odelay-testing` % "test->test;compile->compile")
         .settings(commonSettings:_*)
 
lazy val `odelay-netty3` =
  project.dependsOn(`odelay-core`, `odelay-testing` % "test->test")
         .settings(commonSettings:_*)

lazy val `odelay-netty` =
  project.dependsOn(`odelay-core`, `odelay-testing` % "test->test")
         .settings(commonSettings:_*)
  
lazy val `odelay-twitter` =
  project.dependsOn(`odelay-core`, `odelay-testing` % "test->test")
         .settings(Seq(crossScalaVersions := Seq("2.9.3", "2.10.4"))
                   ++ commonSettings:_*)
