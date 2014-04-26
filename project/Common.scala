//http://www.scala-sbt.org/0.13.2/docs/Getting-Started/Multi-Project.html
object Common {
  import sbt._, sbt.Keys._
  def module(mod: String) =
    Project(mod, file(mod), 
            settings = Defaults.defaultSettings ++ Seq(
              organization := "me.lessis",
              name := s"odelay-$mod",
              version := "0.1.0-SNAPSHOT",
              crossScalaVersions := Seq("2.9.3", "2.10.4", "2.11.0"),
              scalaVersion := crossScalaVersions.value.head,
              scalacOptions ++= Seq(Opts.compile.deprecation),
              licenses := Seq(("MIT",  url("https://github.com/softprops/odelay/blob/%s/LICENSE"
                                           .format(version.value))))) ++
            bintray.Plugin.bintraySettings)
}
