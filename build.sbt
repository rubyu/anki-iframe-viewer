enablePlugins(ScalaJSPlugin)

name := "anki-iframe-viewer"

version := "1.0"

scalaVersion := "2.11.8"

//scalaJSOptimizerOptions ~= { _.withDisableOptimizer(true) }

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.0",
  "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"
)

compile <<= (compile in Compile).
  dependsOn(fullOptJS in Compile)

dist <<= dist.dependsOn(compile)
