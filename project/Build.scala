import sbt._
import Keys._

object JSBuild extends Build {
  override def projects = Seq(root)

  lazy val root = Project("root", file("."),
    settings = Defaults.coreDefaultSettings ++ rootSettings)

  lazy val rootSettings = Seq(
    dist := {
      import IO._
      val dist = file("./dist/")
      val resources = file("./src/main/resources/")
      val files = Seq(
        ("./target/scala-2.11/anki-iframe-viewer-fastopt.js", s"$dist/anki-iframe-viewer.js"),
        ("./target/scala-2.11/anki-iframe-viewer-fullopt.js", s"$dist/anki-iframe-viewer-opt.js")
      ) map {case (a, b) =>
        (file(a), file(b))
      }
      if (dist.exists && dist.isFile) {
        throw new IllegalStateException(s"'$dist' is not a directory")
      }
      dist.delete()
      dist.mkdir()
      copyDirectory(resources, dist, overwrite = true, preserveLastModified = true)
      copy(files, overwrite = true, preserveLastModified = true)
    }
  )
  lazy val dist = TaskKey[Unit]("dist")
}
