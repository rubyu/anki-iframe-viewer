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
  // User settings.
  private var userMinLongSwipeSize                    : Option[Double] = None
  private var userMinSwipeSize                        : Option[Double] = None
  private var userMinLongTouchMillis                  : Option[Double] = None
  private var userMaxGestureMillis                    : Option[Double] = None
  private var userDispatcherDuplicateEventWindowMillis: Option[Double] = None
  private var userTapCenterRatio                      : Option[Double] = None
  private var userAutoLoadAudio                       : Option[Boolean] = None
  private var userAutoPlayAudio                       : Option[Boolean] = None
  private var userRepeatAudio                         : Option[Boolean] = None
  private var userAudioQueryString                    : Option[String] = None
  private var userChapterQueryStrings = mutable.ArrayBuffer.empty[(String, String)]

  @JSExport def MinLongSwipeSize(d: Double)                      = { userMinLongSwipeSize = Option(d); this }
  @JSExport def MinSwipeSize(d: Double)                          = { userMinSwipeSize = Option(d); this }
  @JSExport def MinLongTouchMillis(d: Double)                    = { userMinLongTouchMillis = Option(d); this }
  @JSExport def MaxGestureMillis(d: Double)                      = { userMaxGestureMillis = Option(d); this }
  @JSExport def DispatcherDuplicateEventWindowMillis(d: Double)  = { userDispatcherDuplicateEventWindowMillis = Option(d); this }
  @JSExport def TapCenterRatio(d: Double)                        = { userTapCenterRatio = Option(d); this }
  @JSExport def AutoLoadAudio(b: Boolean)                        = { userAutoLoadAudio = Option(b); this }
  @JSExport def AutoPlayAudio(b: Boolean)                        = { userAutoPlayAudio = Option(b); this }
  @JSExport def RepeatAudio(b: Boolean)                          = { userRepeatAudio = Option(b); this }
  @JSExport def audio(query: String)                             = { userAudioQueryString = Option(query); this }
  @JSExport def chapter(query: String, caption: String)          = { userChapterQueryStrings += ((query, caption)); this }

  private def reset(): Unit = {
    userMinLongSwipeSize                     = None
    userMinSwipeSize                         = None
    userMinLongTouchMillis                   = None
    userMaxGestureMillis                     = None
    userDispatcherDuplicateEventWindowMillis = None
    userTapCenterRatio                       = None
    userAutoLoadAudio                        = None
    userAutoPlayAudio                        = None
    userRepeatAudio                          = None
    userAudioQueryString                     = None
    userChapterQueryStrings = mutable.ArrayBuffer.empty[(String, String)]
  }

  @JSExport
  def run(): Unit = {
    info("start")
    val app = new App(
      userMinLongSwipeSize,
      userMinSwipeSize,
      userMinLongTouchMillis,
      userMaxGestureMillis,
      userDispatcherDuplicateEventWindowMillis,
      userTapCenterRatio,
      userAutoLoadAudio,
      userAutoPlayAudio,
      userRepeatAudio,
      userAudioQueryString,
      userChapterQueryStrings.toList
    )
    app.dump()
    window.addEventListener("DOMContentLoaded", app.DOMContentLoadedHandler)
    window.addEventListener("load", app.loadHandler)
    reset()
  }
}

class App(
  userMinLongSwipeSize                    : Option[Double],
  userMinSwipeSize                        : Option[Double],
  userMinLongTouchMillis                  : Option[Double],
  userMaxGestureMillis                    : Option[Double],
  userDispatcherDuplicateEventWindowMillis: Option[Double],
  userTapCenterRatio                      : Option[Double],
  userAutoLoadAudio                       : Option[Boolean],
  userAutoPlayAudio                       : Option[Boolean],
  userRepeatAudio                         : Option[Boolean],
  audioQueryString: Option[String],
  chapterQueryStrings: List[(String, String)]
) extends Logger {

  // System settings.
  val UIRefreshIntervalMillis = 10 //ms

  // User editable settings.
  val minSwipeSize                        : Double = userMinSwipeSize.getOrElse(20)
  val minLongSwipeSize                    : Double = userMinLongSwipeSize.getOrElse(math.min(window.innerHeight, window.innerWidth) * 0.5)
  val minLongTouchMillis                  : Double = userMinLongTouchMillis.getOrElse(1000)
  val maxGestureMillis                    : Double = userMaxGestureMillis.getOrElse(2000) //ms
  val dispatcherDuplicateEventWindowMillis: Double = userDispatcherDuplicateEventWindowMillis.getOrElse(1500)
  val centerTapRatio                      : Double = userTapCenterRatio.getOrElse(0.20)
  val autoLoadAudio                       : Boolean = userAutoLoadAudio.getOrElse(true)
  val autoPlayAudio                       : Boolean = userAutoPlayAudio.getOrElse(true)
  val autoRepeatAudio                     : Boolean = userRepeatAudio.getOrElse(true)

  // System *variable* flags.
  // whether app already touched or not
  var alreadyTouched = false

  @elidable(elidable.FINE)
  def dump(): Unit = {
    debug(f"System settings.")
    debug(f"UIRefreshIntervalMillis: $UIRefreshIntervalMillis")
    debug(f"User editable settings.")
    debug(f"minSwipeSize: $minSwipeSize")
    debug(f"minLongSwipeSize: $minLongSwipeSize")
    debug(f"minLongTouchMillis: $minLongTouchMillis")
    debug(f"maxGestureMillis: $maxGestureMillis")
    debug(f"dispatcherDuplicateEventWindowMillis: $dispatcherDuplicateEventWindowMillis")
    debug(f"centerTapRatio: $centerTapRatio")
    debug(f"System flags.")
    debug(f"alreadyTouched: $alreadyTouched")
    debug(f"User variables.")
    debug(f"audioQueryString: $audioQueryString")
    debug(f"chapterQueryStrings: $chapterQueryStrings")
  }

  var flash: Flash = null
  var audioPlayer: AudioPlayer = null
  var viewer: Viewer = null
  var mouseEvent: MouseWheelEvent = null
  var touchEvent: TouchEvent = null
  var touchDispatcher: Dispatcher = null
  var mouseDispatcher: Dispatcher = null

  def mouseWheelHandler = (event: dom.WheelEvent) => {
    event.deltaY match {
      case y if y < 0 => mouseEvent.wheelUp()
      case y if y > 0 => mouseEvent.wheelDown()
    }
    event.preventDefault()
  }

  def mouseDownHandler = (event: dom.MouseEvent) => {
    debug(f"mousedown: $event")
    mouseDispatcher.dispatchStart(0, event.pageX, event.pageY)
    event.preventDefault()
  }

  def mouseMoveHandler = (event: dom.MouseEvent) => {
    //debug(f"mousemove: $event")
    mouseDispatcher.dispatchMove(0, event.pageX, event.pageY)
    event.preventDefault()
  }

  def mouseUpHandler = (event: dom.MouseEvent) => {
    debug(f"mouseup: $event")
    mouseDispatcher.dispatchEnd(0, event.pageX, event.pageY)
    event.preventDefault()
  }

  def touchStartHandler = (event: dom.TouchEvent) => {
    for (i <- 0 until event.changedTouches.length) {
      val touch = event.changedTouches(i)
      debug(f"touchstart: $touch")
      touchDispatcher.dispatchStart(touch.identifier, touch.pageX, touch.pageY)
    }
    event.preventDefault()
  }

  def touchMoveHandler = (event: dom.TouchEvent) => {
    for (i <- 0 until event.changedTouches.length) {
      val touch = event.changedTouches(i)
      //debug(f"touchmove: $touch")
      touchDispatcher.dispatchMove(touch.identifier, touch.pageX, touch.pageY)
    }
    event.preventDefault()
  }

  def touchEndHandler = (event: dom.TouchEvent) => {
    for (i <- 0 until event.changedTouches.length) {
      val touch = event.changedTouches(i)
      debug(f"touchend: $touch")
      touchDispatcher.dispatchEnd(touch.identifier, touch.pageX, touch.pageY)
    }
    event.preventDefault()
  }

  def DOMContentLoadedHandler = (e: dom.Event) => {
    if (!Viewer.canRun) {
      fatal("AnkiIframeViewer cannot work on old browsers :(")
    } else {
      val audio = audioQueryString match {
        case Some(query) => Option(document.querySelector(query))
        case None => None
      }
      debug(f"audioQuery: $audioQueryString, audio: $audio")

      val chapters = chapterQueryStrings.map {
        case (query, caption) => Option(document.querySelector(query)) match {
          case Some(elem) => new Chapter(elem.asInstanceOf[HTMLUnknownElement], caption)
          case None => null
        }
      } .filter { chapter => Option(chapter).isDefined } .toList
      debug(f"chapterQueries: $chapterQueryStrings, chapters: $chapters")

      flash = new Flash(this)
      if (audio.isDefined) {
        audioPlayer = new AudioPlayer(audio.get.asInstanceOf[html.Audio])
      }
      viewer = new Viewer(this, chapters)
      mouseEvent = new MouseWheelEvent(this)
      touchEvent = new TouchEvent(this, Option(audioPlayer))
      touchDispatcher = new Dispatcher(this)
      mouseDispatcher = new Dispatcher(this, Option(touchDispatcher))

      document.addEventListener("mousewheel", mouseWheelHandler)
      document.addEventListener("mousedown", mouseDownHandler)
      document.addEventListener("mousemove", mouseMoveHandler)
      document.addEventListener("mouseup",   mouseUpHandler)
      document.addEventListener("touchstart", touchStartHandler)
      document.addEventListener("touchmove",  touchMoveHandler)
      document.addEventListener("touchend",   touchEndHandler)
    }
  }

  def loadHandler = (e: dom.Event) => {
    if (Viewer.canRun) {
      viewer.goFirstChapter()
    }
  }
}