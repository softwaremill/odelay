libraryDependencies := (scalaVersion.value match {
  case rewrite if rewrite.startsWith("2.11") =>
    Seq("com.twitter" % "util-core_2.11" % "6.42.0")
  case rewrite if rewrite.startsWith("2.12") =>
    Seq("com.twitter" %% "util-core" % "17.12.0")
  case _ => Nil
}) ++ libraryDependencies.value

description := "an odelay.Timer implementation backed by a com.twitter.util.Timer"
