libraryDependencies <+= (scalaVersion) {
  case rewrite if rewrite.startsWith("2.9.3") =>
    "com.twitter" % "util-core_2.9.2"  % "6.13.2"
  case _ =>
    "com.twitter" %% "util-core" % "6.13.2"
}

description := "provides a deferred.Timer implementation backed by a com.twitter.util.Timer"

scalaVersion := "2.10.3"
