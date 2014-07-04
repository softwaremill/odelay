import sbt.{ file, Defaults, Project }
//http://www.scala-sbt.org/0.13.2/docs/Getting-Started/Multi-Project.html
object Common {
  import sbt.Keys._
  def module(mod: String) =
    Project(mod, file(mod), 
            settings = Defaults.defaultSettings ++ Seq(
              name := s"odelay-$mod") ++
            bintray.Plugin.bintraySettings)
}
