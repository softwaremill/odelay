libraryDependencies <+= (scalaVersion) {
  case rewrite if rewrite.startsWith("2.9.3") =>
    "com.twitter" % "util-core_2.9.2"  % "6.18.0"
  case _ =>
    "com.twitter" %% "util-core" % "6.34.0"
}

description := "an odelay.Timer implementation backed by a com.twitter.util.Timer"

scalaVersion := crossScalaVersions.value.last
