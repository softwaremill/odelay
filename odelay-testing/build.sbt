libraryDependencies += 
  (if (scalaVersion.value.startsWith("2.9.3")) "org.scalatest" %% "scalatest" % "1.9.2" else "org.scalatest" %% "scalatest" % "2.1.3")

publish := {}

test := {}
