import sbt._
import Keys._

object JSBuild extends Build {
  override def projects = Seq(root)

  lazy val root = Project("root", file("."),
    settings = Defaults.coreDefaultSettings ++ rootSettings)

  lazy val rootSettings = Seq(
    dist := {
      import IO._
      val dir = file("./dist/")
      val files = Seq(
        ("./target/scala-2.11/anki-iframe-viewer-fastopt.js", s"$dir/anki-iframe-viewer.js"),
        ("./src/main/resources/anki-iframe-viewer.css",       s"$dir/anki-iframe-viewer.css"),
        ("./src/main/resources/viewer.html",                  s"$dir/viewer.html")
      ) map {case (a, b) =>
        (file(a), file(b))
      }
      if (dir.exists && dir.isFile) {
        throw new IllegalStateException(s"'$dir' is not a directory")
      }
      dir.delete()
      dir.mkdir()
      copy(files, overwrite = false, preserveLastModified = true)
    }
  )
  lazy val dist = TaskKey[Unit]("dist")
}
