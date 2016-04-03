import scala.annotation.elidable
import scala.collection.mutable
import scalajs.js
import scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.{window, document, html}
import org.scalajs.dom.html.{Html, Body}

@JSExport("AnkiIframeViewerApp")
object App extends Logger {
  val devices = mutable.HashMap.empty[String, Int]
  val defaultFontSize: Double = 16
  def baseFontSize: Int = {
    debug(f"defaultFontSize: $defaultFontSize%.2f")
    debug(f"devicePixelRatio: ${U.W.devicePixelRatio}%.2f")
    def getAdjustedFontSize: Int = {
      devices.find{
        case (k, v) => U.N.userAgent.contains(k)
      } match {
        case Some((k, v)) =>
          debug(f"$k found; fontSize: $v")
          v
        case None =>
          debug(f"no preferred device found")
          (defaultFontSize - U.W.devicePixelRatio)
            .toInt match {
            case n if n % 2 == 0 =>
              n
            case n =>
              debug(f"round up: $n -> ${ n+1 }")
              n+1
          }
      }
    }
    val fontSize = getAdjustedFontSize
    info(f"baseFontSize: $fontSize")
    fontSize
  }

  lazy val viewer = new Viewer()

  def setBaseFontSize(): Unit = {
    val html = document.getElementsByTagName("html")(0).asInstanceOf[Html]
    val fontSize = baseFontSize
    debug(f"set html.style.fontSize = $fontSize")
    html.style.fontSize = fontSize.toString
  }

  def fallback(): Unit = {
    val html = document.getElementsByTagName("html")(0).asInstanceOf[Html]
    val body = document.getElementsByTagName("body")(0).asInstanceOf[Body]
    debug(f"fallback to standard style")
    html.style.overflowY = "auto"
    body.style.overflowY = "auto"
  }

  @elidable(elidable.FINE)
  def dump(): Unit = {
    devices.zipWithIndex.foreach {
      case ((k, v), i) => debug(f"device[$i] $k -> $v")
    }
  }

  @JSExport
  def device(name: String, fontSize: Int) = {
    devices += (name -> fontSize)
    this
  }

  // audio
  // chapter
  // head

  @JSExport
  def run(): Unit = {
    info("start")
    dump()
    setBaseFontSize()
    window.addEventListener("DOMContentLoaded", DOMContentLoadedHander)
    window.addEventListener("load", loadHandler)
  }
  val DOMContentLoadedHander: js.Function1[dom.Event, Any] = (e: dom.Event) => {
    if (!Viewer.canRun) {
      fallback()
      fatal("ankiIframeViewerApp cannot work on old browsers :(")
    } else {
      viewer.run()
    }
  }

  val loadHandler: js.Function1[dom.Event, Any] = (e: dom.Event) => {
    if (Viewer.canRun) {
      //
    }
  }
}
