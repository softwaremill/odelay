organization in ThisBuild := "me.lessis"

version in ThisBuild := "0.1.0"

crossScalaVersions in ThisBuild := Seq("2.10.4", "2.11.1")

scalaVersion in ThisBuild := crossScalaVersions.value.last

scalacOptions in ThisBuild ++= Seq(Opts.compile.deprecation) ++
  Seq("-Ywarn-unused-import", "-Ywarn-unused", "-Xlint", "-feature").filter(
    Function.const(scalaVersion.value.startsWith("2.11")))

licenses in ThisBuild := Seq(
  ("MIT", url("https://github.com/softprops/odelay/blob/${version.value}/LICENSE")))

val commonSettings = bintraySettings ++ lsSettings ++ Seq(
 bintray.Keys.packageLabels in bintray.Keys.bintray := Seq("delay", "scheduling", "future"),
 LsKeys.tags in LsKeys.lsync := (bintray.Keys.packageLabels in bintray.Keys.bintray).value,
 externalResolvers in LsKeys.lsync := (resolvers in bintray.Keys.bintray).value
)

val unpublished = Seq(publish := {}, publishLocal := {})

lazy val `odelay-core` =
  project.settings(commonSettings:_*)

lazy val `odelay-testing` =
  project.dependsOn(`odelay-core`)
         .settings(unpublished:_*)

lazy val `odelay-core-tests` =
  project.dependsOn(`odelay-testing` % "test->test;compile->compile")
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

pomExtra := (
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
