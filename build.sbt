import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import com.softwaremill.Publish.ossPublishSettings

val scala2_11 = "2.11.12"
val scala2_12 = "2.12.17"
val scala2_13 = "2.13.9"
val scala2 = List(scala2_11, scala2_12, scala2_13)
val scala3 = List("3.2.0")

val scalatestVersion = "3.2.13"

excludeLintKeys in Global ++= Set(ideSkipProject)

val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ Seq(
  organization := "com.softwaremill.odelay",
  licenses := Seq(("MIT", url(s"https://github.com/softprops/odelay/blob/${version.value}/LICENSE")))
)

val commonJvmSettings = commonSettings ++ Seq(
  ideSkipProject := (scalaVersion.value != scala2_13)
)

val commonJsSettings = commonSettings ++ Seq(
  ideSkipProject := true
)

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publish / skip := true, name := "odelay", scalaVersion := scala2_13)
  .aggregate(
    core.projectRefs ++ testing.projectRefs ++ coreTests.projectRefs ++ netty3.projectRefs ++ netty.projectRefs ++ twitter.projectRefs: _*
  )

lazy val core = (projectMatrix in file("odelay-core"))
  .settings(
    name := "odelay-core",
    description := "provides api and jdk times as potential default"
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings
  )
  .jsPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJsSettings
  )

lazy val testing = (projectMatrix in file("odelay-testing"))
  .settings(
    name := "odelay-testing",
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVersion % Test,
    publish / skip := true
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings
  )
  .jsPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJsSettings ++ Seq(
      libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.4.0"
    )
  )
  .dependsOn(core)

lazy val coreTests = (projectMatrix in file("odelay-core-tests"))
  .settings(
    name := "odelay-core-tests",
    publish / skip := true
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings
  )
  .dependsOn(testing % "test->test")

lazy val netty3 = (projectMatrix in file("odelay-netty3"))
  .settings(
    name := "odelay-netty3",
    description := "an odelay.Timer implementation backed by netty 3",
    libraryDependencies += "io.netty" % "netty" % "3.10.6.Final"
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings
  )
  .dependsOn(core, testing % "test->test")

lazy val netty = (projectMatrix in file("odelay-netty"))
  .settings(
    name := "odelay-netty",
    description := "an odelay.Timer implementation backed by netty 4",
    libraryDependencies += "io.netty" % "netty-common" % "4.1.82.Final"
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings
  )
  .dependsOn(core, testing % "test->test")

lazy val twitter = (projectMatrix in file("odelay-twitter"))
  .settings(
    name := "odelay-twitter",
    libraryDependencies := (scalaVersion.value match {
      case rewrite if rewrite.startsWith("2.11") =>
        Seq("com.twitter" % "util-core_2.11" % "21.2.0")
      case rewrite if rewrite.startsWith("2.12") =>
        Seq("com.twitter" %% "util-core" % "22.7.0")
      case _ => Nil
    }) ++ libraryDependencies.value,
    description := "an odelay.Timer implementation backed by a com.twitter.util.Timer"
  )
  .jvmPlatform(
    scalaVersions = List(scala2_11, scala2_12),
    settings = commonJvmSettings
  )
  .dependsOn(core, testing % "test->test")
