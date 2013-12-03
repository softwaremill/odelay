
object Build extends sbt.Build {
  import sbt._
  lazy val root = Project("root", file(".")).aggregate(
    core, netty3, netty, twttr)
  lazy val core = Project("core", file("core"))
  lazy val netty3 = Project("netty3", file("netty3")).dependsOn(core)
  lazy val netty = Project("netty", file("netty")).dependsOn(core)
  lazy val twttr = Project("twitter", file("twitter")).dependsOn(core)
}
