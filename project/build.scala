
object Build extends sbt.Build {
  import sbt._, sbt.Keys._
  object Common {
    val organization = "me.lessis"
    val version = "0.1.0-SNAPSHOT"
  }
  def module(mod: String) =
    Project(mod, file(mod), 
            settings = Defaults.defaultSettings ++ Seq(
              organization := Common.organization,
              name := s"odelay-$mod",
              version := Common.version))
  lazy val root =
    Project("root", file("."))
      .settings(publish := { })
      .aggregate(core, netty3, netty, twttr)
  lazy val core = module("core")
  lazy val netty3 = module("netty3")
  lazy val netty = module("netty")
  lazy val twttr = module("twitter")
}
