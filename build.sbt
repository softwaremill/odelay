organization in ThisBuild := "me.lessis"

version in ThisBuild := "0.1.0-SNAPSHOT"

crossScalaVersions in ThisBuild := Seq("2.9.3", "2.10.4", "2.11.1")

scalaVersion in ThisBuild := crossScalaVersions.value.last

scalacOptions in ThisBuild ++= Seq(Opts.compile.deprecation) ++
  Seq("-Ywarn-unused-import", "-Ywarn-unused", "-Xlint", "-feature").filter(
    Function.const(scalaVersion.value.startsWith("2.11")))

licenses in ThisBuild := Seq(
  ("MIT", url("https://github.com/softprops/odelay/blob/${version.value}/LICENSE")))

//lazy val root = project.in(file("."))
//  .settings(
//    publish := {}, test := {}, publishLocal := {}
//  )
//  .aggregate(core, coreTests, netty3, netty, twttr, testing)

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
