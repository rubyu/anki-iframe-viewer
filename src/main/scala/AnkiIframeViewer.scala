

import scala.scalajs.js.JSApp
import scala.scalajs.js.Dynamic.global
import org.scalajs.dom.{window, document}
import org.scalajs.dom.html._

object AnkiIframeViewer extends JSApp with LoggerKey {

  def isAnki = window.navigator.userAgent.contains("Anki")

  def toStandardMode(): Unit = {
    val html = document.getElementsByTagName("html")(0).asInstanceOf[Html]
    val body = document.getElementsByTagName("body")(0).asInstanceOf[Body]
    html.style.overflowY = "auto"
    body.style.overflowY = "auto"
  }

  def logTest(): Unit = {
    Logger.info("ok")
    Logger.info("ok", "ok")
    Logger.trace("trace")
    Logger.debug("debug")
    Logger.info("info")
    Logger.warn("warn")
    Logger.error("error")
    Logger.fatal("fatal")
  }


  def main(): Unit = {

    if (isAnki) {
      this.toStandardMode()
      global.console.log("anki-iframe-viewer cannot work on old browsers :(")
      println("anki-iframe-viewer cannot work on old browsers :(")
      return
    }

    assert(false)

    global.console.log("ok")
    global.console.log("a", "b")
    println("ok")
    Logger.info("logtest before")
    logTest()
    Logger.info("logtest after")
    Logger.info("a", "b")
  }
}
