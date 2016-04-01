

import scala.scalajs.js.JSApp
import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom.{window, document}
import org.scalajs.dom.html._

@JSExport
object AnkiIframeViewer extends JSApp {
  def isAnki = window.navigator.userAgent.contains("Anki")

  def toStandardMode(): Unit = {
    val html = document.getElementsByTagName("html")(0).asInstanceOf[Html]
    val body = document.getElementsByTagName("body")(0).asInstanceOf[Body]
    html.style.overflowY = "auto"
    body.style.overflowY = "auto"
  }

  @JSExport
  def main(): Unit = {
    if (isAnki) {
      this.toStandardMode()
      global.console.log("anki-iframe-viewer cannot work on old browsers :(")
      println("anki-iframe-viewer cannot work on old browsers :(")
      return
    }

    assert(false)

    global.console.log("ok")
    println("ok")

  }
}
