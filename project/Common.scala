import sbt.{ file, Defaults, Project }
import sbt.Keys.name
//http://www.scala-sbt.org/0.13.2/docs/Getting-Started/Multi-Project.html
object Common {
  def module(mod: String) =
    Project(mod, file(mod), 
            settings = Defaults.defaultSettings ++ Seq(
              name := s"odelay-$mod") ++
            bintray.Plugin.bintraySettings)
}
