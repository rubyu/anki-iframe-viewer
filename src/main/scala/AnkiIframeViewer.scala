import scala.scalajs.js.JSApp

object AnkiIframeViewerApp extends JSApp {
  def main(): Unit = {
    val viewer = new Viewer()
    viewer.run()
  }
}
