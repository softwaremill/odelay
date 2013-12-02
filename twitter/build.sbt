organization := "me.lessis"

name := "deferred-twitter"

libraryDependencies += "com.twitter" % "util-core"  % "6.3.4"

description := "provides a deferred.Timer implementation backed by a com.twitter.util.Timer"

resolvers += "twitter" at "http://maven.twttr.com/"

scalaVersion := "2.10.0"
