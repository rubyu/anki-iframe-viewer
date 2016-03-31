enablePlugins(ScalaJSPlugin)

name := "anki-iframe-viewer"

version := "1.0"

scalaVersion := "2.11.8"

//scalaJSOptimizerOptions ~= { _.withDisableOptimizer(true) }

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"

compile <<= (compile in Compile).
  dependsOn(fastOptJS in Compile).
  dependsOn(fullOptJS in Compile)

dist <<= dist.dependsOn(compile)
