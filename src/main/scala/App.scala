import scala.annotation.elidable
import scala.collection.mutable
import scalajs.js
import scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.{document, window}
import org.scalajs.dom.html
import org.scalajs.dom.raw.HTMLUnknownElement

@JSExport("AnkiIframeViewerApp")
object App extends Logger {
  val devices = mutable.HashMap.empty[String, Int]
  val defaultFontSize: Double = 16
  def baseFontSize: Int = {
    debug(f"defaultFontSize: $defaultFontSize%.2f")
    debug(f"devicePixelRatio: ${window.devicePixelRatio}%.2f")
    def getAdjustedFontSize: Int = {
      devices.find {
        case (k, v) => window.navigator.userAgent.contains(k)
      } match {
        case Some((k, v)) =>
          debug(f"$k found; fontSize: $v")
          v
        case None =>
          debug(f"no preferred device found")
          (defaultFontSize - window.devicePixelRatio).toInt match {
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

  def setBaseFontSize(): Unit = {
    val h = document.getElementsByTagName("html")(0).asInstanceOf[html.Html]
    debug(f"set html.style.fontSize = $baseFontSize")
    h.style.fontSize = baseFontSize.toString
  }

  def fallback(): Unit = {
    val h = document.getElementsByTagName("html")(0).asInstanceOf[html.Html]
    val b = document.getElementsByTagName("body")(0).asInstanceOf[html.Body]
    debug(f"fallback to standard style")
    h.style.overflowY = "auto"
    b.style.overflowY = "auto"
  }

  val flashRefreshInterval = 10 // ms
  var alreadyTouched = false // whether app already touched or not

  @elidable(elidable.FINE)
  def dump(): Unit = {
    devices.zipWithIndex.foreach {
      case ((k, v), i) => debug(f"device[$i] $k -> $v")
    }
  }

  var chapters = mutable.ArrayBuffer.empty[Chapter]
  var viewer, audioPlayer = null

  def reset(): Unit = {
    mutable.ArrayBuffer.empty[Chapter]
    viewer = null
  }

  @JSExport
  def device(name: String, fontSize: Int) = {
    devices += (name -> fontSize)
    this
  }

  @JSExport
  def chapter(id: String, caption: String): Unit = {
    document.getElementById(id) match {
      case elem if js.isUndefined(elem) =>
        fatal(f"no element matched with the given id: $id")
      case _ =>
        chapters += new Chapter(document.getElementById(id).asInstanceOf[HTMLUnknownElement], caption)
    }
  }

  // audio
  // head

  @JSExport
  def run(): Unit = {
    info("start")
    dump()
    setBaseFontSize()
    window.addEventListener("DOMContentLoaded", DOMContentLoadedHander)
    window.addEventListener("load", loadHandler)
    reset()
  }

  val DOMContentLoadedHander: js.Function1[dom.Event, Any] = (e: dom.Event) => {
    if (!Viewer.canRun) {
      fallback()
      fatal("ankiIframeViewerApp cannot work on old browsers :(")
    } else {
      viewer = new Viewer(, chapters.toList)
      audioPlayer = new AudioPlayer()
    }
  }

  val loadHandler: js.Function1[dom.Event, Any] = (e: dom.Event) => {
    if (Viewer.canRun) {
      //
    }
  }
}
