import org.scalajs.dom._
import org.scalajs.dom.html._
import scala.scalajs.js.Dynamic._

class Viewer extends Logger {

  def fallback(): Unit = {
    val html = document.getElementsByTagName("html")(0).asInstanceOf[Html]
    val body = document.getElementsByTagName("body")(0).asInstanceOf[Body]
    html.style.overflowY = "auto"
    body.style.overflowY = "auto"
  }

  def run(): Unit ={
    if (U.isAnki) {
      this.fallback()
      fatal("anki-iframe-viewer cannot work on old browsers :(")
      return
    }
    global.console.log("ok")
    global.console.log("a", "b")
    println("ok")
    info("logtest before")
    info("logtest after")
    info("a", "b")
  }
}
