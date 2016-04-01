import sbt._
import Keys._
import org.scalajs.sbtplugin._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Build extends sbt.Build {
  lazy val commonSettings =
    Defaults.coreDefaultSettings ++
      Seq(
        version := "1.0",
        scalaVersion := "2.11.8",
        name := "anki-iframe-viewer"
      )

  lazy val dev = Project("dev", file(".")).
    settings(commonSettings: _*).
    settings(Seq(
      name := "anki-iframe-viewer-dev"
    )).
    settings(Seq(
      target := file("./target/dev/")
    )).
    settings(Seq(
      dist := distTask.dependsOn(fullOptJS in Compile).value
    )).
    settings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.0",
        "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"
      )
    ).
    enablePlugins(ScalaJSPlugin)

  lazy val prod = Project("prod", file(".")).
    settings(commonSettings: _*).
    settings(Seq(
      target := file("./target/prod/")
    )).
    settings(Seq(
      dist := distTask.dependsOn(fullOptJS in Compile).value
    )).
    settings(Seq(
      scalacOptions := Seq("-Xdisable-assertions")
    )).
    settings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.0",
        "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"
      )
    ).
    enablePlugins(ScalaJSPlugin)

  val dist = TaskKey[Unit]("dist")

  private def distTask = Def.task {
    import IO._
    val dist = file("./dist/")
    val resources = file("./src/main/resources/")
    val files = crossTarget.value.listFiles.collect {
      case f if f.isFile && f.name.contains("opt.js") =>
        (f, file(s"$dist/${f.name}"))
    }
    if (dist.exists && dist.isFile) {
      throw new IllegalStateException(s"'$dist' is not a directory")
    }
    dist.delete()
    dist.mkdir()
    copyDirectory(resources, dist, overwrite = true, preserveLastModified = true)
    copy(files, overwrite = true, preserveLastModified = true)
  }
}
