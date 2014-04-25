object Build extends sbt.Build {
  import sbt._, sbt.Keys._
  object Common {
    val organization = "me.lessis"
    val version = "0.1.0-SNAPSHOT"
    val crossScalaVersions = Seq("2.9.3", "2.10.4", "2.11.0")
  }
  def module(mod: String) =
    Project(mod, file(mod), 
            settings = Defaults.defaultSettings ++ Seq(
              organization := Common.organization,
              name := s"odelay-$mod",
              version := Common.version,
              crossScalaVersions := Common.crossScalaVersions,
              scalaVersion := crossScalaVersions.value.head,
              scalacOptions ++= Seq(Opts.compile.deprecation),
              licenses := Seq(("MIT",  url("https://github.com/softprops/odelay/blob/%s/LICENSE"
                                           .format(version.value))))) ++
            bintray.Plugin.bintraySettings)
  lazy val root =
    Project("root", file("."))
      .settings(crossScalaVersions := Common.crossScalaVersions, publish := {}, test := {}, publishLocal := {})
      .aggregate(core, coreTests, netty3, netty, twttr, testing)
  lazy val core: Project = module("core")
    .settings(test := {}) // see coreTests module
  lazy val testing = module("testing")
    .settings(publish := {}, test := {})
    .dependsOn(core)
  lazy val coreTests = module("core-tests")
    .settings(publish := {})
    .dependsOn(testing % "test->test;compile->compile")
  lazy val netty3 = module("netty3")
    .dependsOn(core, testing % "test->test")
  lazy val netty = module("netty")
    .dependsOn(core, testing % "test->test")
  lazy val twttr = module("twitter")
    .settings(crossScalaVersions := Seq("2.9.3", "2.10.4"))
    .dependsOn(core, testing % "test->test")
}
