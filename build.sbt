import BintrayPlugin.autoImport._

organization in ThisBuild := "me.lessis"

version in ThisBuild := "0.1.1"

crossScalaVersions in ThisBuild := Seq("2.10.4", "2.11.1")

scalaVersion in ThisBuild := crossScalaVersions.value.last

scalacOptions in ThisBuild ++= Seq(Opts.compile.deprecation) ++
  Seq("-Ywarn-unused-import", "-Ywarn-unused", "-Xlint", "-feature").filter(
    Function.const(scalaVersion.value.startsWith("2.11")))

licenses in ThisBuild := Seq(
  ("MIT", url(s"https://github.com/softprops/odelay/blob/${version.value}/LICENSE")))

homepage in ThisBuild := Some(url(s"https://github.com/softprops/${name.value}/"))

val commonSettings =  lsSettings ++ Seq(
  LsKeys.tags in LsKeys.lsync := Seq("delay", "scheduling", "future"),
  bintrayPackageLabels := (LsKeys.tags in LsKeys.lsync).value,
  resolvers += sbt.Resolver.bintrayRepo("softprops","maven"),
  externalResolvers in LsKeys.lsync := (resolvers in bintray).value
)

val unpublished = Seq(publish := {}, publishLocal := {})

lazy val `odelay-core` =
  project.settings(commonSettings:_*)

lazy val `odelay-testing` =
  project.dependsOn(`odelay-core`)
         .settings(unpublished:_*)

lazy val `odelay-core-tests` =
  project.dependsOn(`odelay-testing` % "test->test")
         .settings(unpublished:_*)

lazy val `odelay-netty3` =
  project.dependsOn(`odelay-core`, `odelay-testing` % "test->test")
         .settings(commonSettings:_*)

lazy val `odelay-netty` =
  project.dependsOn(`odelay-core`, `odelay-testing` % "test->test")
         .settings(commonSettings:_*)

lazy val `odelay-twitter` =
  project.dependsOn(`odelay-core`, `odelay-testing` % "test->test")
         .settings(commonSettings:_*)

pomExtra in ThisBuild := (
  <scm>
    <url>git@github.com:softprops/odelay.git</url>
    <connection>scm:git:git@github.com:softprops/odelay.git</connection>
  </scm>
  <developers>
    <developer>
      <id>softprops</id>
      <name>Doug Tangren</name>
      <url>https://github.com/softprops</url>
    </developer>
  </developers>)
