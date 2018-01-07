libraryDependencies := (scalaVersion.value match {
  case rewrite if rewrite.startsWith("2.11") =>
      "com.twitter" % "util-core_2.11" % "6.42.0"
  case _ =>
    "com.twitter" %% "util-core" % "17.12.0"
}) +: libraryDependencies.value

description := "an odelay.Timer implementation backed by a com.twitter.util.Timer"

