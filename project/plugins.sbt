resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.3")

// scala.js
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.15")
