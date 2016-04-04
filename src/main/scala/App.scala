import scala.annotation.elidable
import scala.collection.mutable
import scalajs.js
import scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.{document, window}
import org.scalajs.dom.window.screen
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

  val UIRefreshIntervalMillis = 10 //ms
  // whether app already touched or not
  var alreadyTouched = false
  lazy val minSwipeSize = baseFontSize * 2 //ms
  val centerTapRatio = 0.20
  lazy val minLongSwipeSize = math.min(screen.height, screen.width) * 0.65 //px
  val minLongTouchMillis = 1000 //ms
  val maxGestureMillis = 3000 //ms
  val dispatcherDuplicateEventWindowMillis = 1500 //ms

  @elidable(elidable.FINE)
  def dump(): Unit = {
    devices.zipWithIndex.foreach {
      case ((k, v), i) => debug(f"device setting[$i] $k -> $v")
    }
    debug(f"UIRefreshIntervalMillis: $UIRefreshIntervalMillis")
    debug(f"alreadyTouched: $alreadyTouched")
    debug(f"minSwipeSize: $minSwipeSize")
    debug(f"centerTapRatio: $centerTapRatio")
    debug(f"minLongSwipeSize: $minLongSwipeSize")
    debug(f"minLongTouchMillis: $minLongTouchMillis")
    debug(f"maxGestureMillis: $maxGestureMillis")
    debug(f"dispatcherDuplicateEventWindowMillis: $dispatcherDuplicateEventWindowMillis")
  }

  var audioQuery: Option[String] = None
  var chapterQueries = mutable.ArrayBuffer.empty[(String, String)]
  var flash: Flash = null
  var audioPlayer: AudioPlayer = null
  var viewer: Viewer = null
  var mouseEvent: MouseWheelEvent = null
  var touchEvent: TouchEvent = null
  var touchDispatcher: Dispatcher = null
  var mouseDispatcher: Dispatcher = null
  
  @JSExport
  def device(name: String, fontSize: Int) = {
    devices += (name -> fontSize)
    this
  }

  @JSExport
  def audio(query: String) = {
    audioQuery = Option(query)
    this
  }

  @JSExport
  def chapter(query: String, caption: String) = {
    chapterQueries += ((query, caption))
    this
  }

  @JSExport
  def run(): Unit = {
    info("start")
    dump()
    setBaseFontSize()
    window.addEventListener("DOMContentLoaded", DOMContentLoadedHandler)
    window.addEventListener("load", loadHandler)
  }

  def DOMContentLoadedHandler: js.Function1[dom.Event, Any] = (e: dom.Event) => {
    if (!Viewer.canRun) {
      fallback()
      fatal("AnkiIframeViewer cannot work on old browsers :(")
    } else {
      val audio = audioQuery match {
        case Some(query) => Option(document.querySelector(query))
        case None => None
      }
      debug(f"audioQuery: $audioQuery, audio: $audio")

      val chapters = chapterQueries.map {
        case (query, caption) => Option(document.querySelector(query)) match {
            case Some(elem) => new Chapter(elem.asInstanceOf[HTMLUnknownElement], caption)
            case None => null
          }
      } .filter { chapter => Option(chapter).isDefined } .toList
      debug(f"chapterQueries: $chapterQueries, chapters: $chapters")

      flash = new Flash()
      if (audio.isDefined) {
        audioPlayer = new AudioPlayer(audio.get.asInstanceOf[html.Audio])
      }
      viewer = new Viewer(flash, chapters)
      mouseEvent = new MouseWheelEvent(viewer)
      touchEvent = new TouchEvent(viewer, Option(audioPlayer))
      touchDispatcher = new Dispatcher(touchEvent)
      mouseDispatcher = new Dispatcher(touchEvent, Option(touchDispatcher))

      document.addEventListener("mousewheel", (event: dom.WheelEvent) => {
        if (event.deltaX > 0) mouseEvent.wheelUp()
        else mouseEvent.wheelDown()
        event.preventDefault()
      })

      document.addEventListener("mousedown", (event: dom.MouseEvent) => {
        mouseDispatcher.dispatchStart(0, event.pageX, event.pageY)
        event.preventDefault()
      })
      document.addEventListener("mousemove", (event: dom.MouseEvent) => {
        mouseDispatcher.dispatchMove(0, event.pageX, event.pageY)
        event.preventDefault()
      })
      document.addEventListener("mouseup", (event: dom.MouseEvent) => {
        mouseDispatcher.dispatchEnd(0, event.pageX, event.pageY)
        event.preventDefault()
      })

      document.addEventListener("touchstart", (event: dom.TouchEvent) => {
        for (i <- 0 until event.changedTouches.length) {
          val touch = event.touches(i)
          touchDispatcher.dispatchStart(touch.identifier.toInt, touch.pageX, touch.pageY)
        }
        event.preventDefault()
      })
      document.addEventListener("touchmove", (event: dom.TouchEvent) => {
        for (i <- 0 until event.changedTouches.length) {
          val touch = event.touches(i)
          touchDispatcher.dispatchMove(touch.identifier.toInt, touch.pageX, touch.pageY)
        }
        event.preventDefault()
      })
      document.addEventListener("touchend", (event: dom.TouchEvent) => {
        for (i <- 0 until event.changedTouches.length) {
          val touch = event.touches(i)
          touchDispatcher.dispatchEnd(touch.identifier.toInt, touch.pageX, touch.pageY)
        }
        event.preventDefault()
      })
    }
  }

  def loadHandler: js.Function1[dom.Event, Any] = (e: dom.Event) => {
    if (Viewer.canRun) {
      viewer.castCurrentState()
    }
  }
}
