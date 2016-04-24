import scala.language.implicitConversions
import scala.annotation.elidable
import scala.collection.mutable
import scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.raw
import org.scalajs.dom.{document, window}


@JSExport("AnkiIframeViewerApp")
object App extends Logger {
  // User settings.
  private var userMinLongSwipeSize                    : Option[Double] = None
  private var userMinSwipeSize                        : Option[Double] = None
  private var userMinLongTouchMillis                  : Option[Double] = None
  private var userMaxGestureMillis                    : Option[Double] = None
  private var userDispatcherDuplicateEventWindowMillis: Option[Double] = None
  private var userTapCenterRatio                      : Option[Double] = None
  private var userAutoPlayAudio                       : Option[Boolean] = None
  private var userHoldReplayAudio                     : Option[Boolean] = None
  private var userAudioQueries   = mutable.ArrayBuffer.empty[String]
  private var userChapterQueries = mutable.ArrayBuffer.empty[String]

  @JSExport def minLongSwipeSize(d: Double)                      = { userMinLongSwipeSize = Option(d); this }
  @JSExport def minSwipeSize(d: Double)                          = { userMinSwipeSize = Option(d); this }
  @JSExport def minLongTouchMillis(d: Double)                    = { userMinLongTouchMillis = Option(d); this }
  @JSExport def maxGestureMillis(d: Double)                      = { userMaxGestureMillis = Option(d); this }
  @JSExport def dispatcherDuplicateEventWindowMillis(d: Double)  = { userDispatcherDuplicateEventWindowMillis = Option(d); this }
  @JSExport def tapCenterRatio(d: Double)                        = { userTapCenterRatio = Option(d); this }
  @JSExport def autoPlayAudio(b: Boolean)                        = { userAutoPlayAudio = Option(b); this }
  @JSExport def holdReplayAudio(b: Boolean)                      = { userHoldReplayAudio = Option(b); this }
  @JSExport def audio(query: String)                             = { userAudioQueries += query; this }
  @JSExport def chapter(query: String)                           = { userChapterQueries += query; this }

  private def reset(): Unit = {
    userMinLongSwipeSize                     = None
    userMinSwipeSize                         = None
    userMinLongTouchMillis                   = None
    userMaxGestureMillis                     = None
    userDispatcherDuplicateEventWindowMillis = None
    userTapCenterRatio                       = None
    userAutoPlayAudio                        = None
    userHoldReplayAudio                      = None
    userAudioQueries   = mutable.ArrayBuffer.empty[String]
    userChapterQueries = mutable.ArrayBuffer.empty[String]
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
      userAutoPlayAudio,
      userHoldReplayAudio,
      userAudioQueries.toList,
      userChapterQueries.toList
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
  userAutoPlayAudio                       : Option[Boolean],
  userHoldReplayAudio                     : Option[Boolean],
  audioQueries                            : List[String],
  chapterQueries                          : List[String]
) extends NeedUserTouchPrivilege with Logger {

  // System settings.
  val UIRefreshIntervalMillis = 10 //ms

  // User editable settings.
  val minSwipeSize                        : Double = userMinSwipeSize.getOrElse(20)
  val minLongSwipeSize                    : Double = userMinLongSwipeSize.getOrElse(math.min(window.innerHeight, window.innerWidth) * 0.5)
  val minLongTouchMillis                  : Double = userMinLongTouchMillis.getOrElse(700)
  val maxGestureMillis                    : Double = userMaxGestureMillis.getOrElse(2000)
  val dispatcherDuplicateEventWindowMillis: Double = userDispatcherDuplicateEventWindowMillis.getOrElse(1500)
  val centerTapRatio                      : Double = userTapCenterRatio.getOrElse(0.20)
  val autoPlayAudio                       : Boolean = userAutoPlayAudio.getOrElse(true)
  val holdReplayAudio                     : Boolean = userHoldReplayAudio.getOrElse(true)

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
    debug(f"autoPlayAudio: $autoPlayAudio")
    debug(f"holdReplayAudio: $holdReplayAudio")
    debug(f"User variables.")
    debug(f"audioQueries: $audioQueries")
    debug(f"chapterQueries: $chapterQueries")
  }

  private def needUserTouchPrivileges: List[Option[NeedUserTouchPrivilege]] =
    List(Option(audioPlayer))

  def callWithUserTouchPrivilege(): Unit =
    needUserTouchPrivileges
      .flatMap(x => x)
      .foreach(_.callWithUserTouchPrivilege())

  implicit def NodeListToList[T <: dom.raw.Node](nodes: dom.raw.DOMList[T]): List[T] = {
    val buf = mutable.ArrayBuffer.empty[T]
    for (i <- 0 until nodes.length) {
      buf += nodes(i)
    }
    buf.toList
  }

  def audioElements =
    audioQueries
      .flatMap (document.querySelectorAll(_) .asInstanceOf[raw.NodeListOf[raw.HTMLAudioElement]])

  def chapterElements =
    chapterQueries
      .flatMap (document.querySelectorAll(_).asInstanceOf[raw.NodeListOf[raw.HTMLUnknownElement]])

  /* Global components. */
  var flash          : Flash           = null
  var audioPlayer    : AudioPlayer     = null
  var viewer         : Viewer          = null
  var touchEvent     : TouchEvent      = null
  var mouseWheelEvent: MouseWheelEvent = null

  /* Handlers. */
  def mouseWheelHandler = (event: dom.WheelEvent) => {
    event.deltaY match {
      case y if y < 0 => mouseWheelEvent.wheelUp()
      case y if y > 0 => mouseWheelEvent.wheelDown()
    }
    event.preventDefault()
  }
  def mouseHandler(f: (Double, Double, Double) => Unit)(event: dom.MouseEvent): Unit = {
    f(0, event.pageX, event.pageY)
    event.preventDefault()
  }
  def touchHandler(f: (Double, Double, Double) => Unit)(event: dom.TouchEvent): Unit = {
    for (i <- 0 until event.changedTouches.length) {
      val touch = event.changedTouches(i)
      f(touch.identifier, touch.pageX, touch.pageY)
    }
    event.preventDefault()
  }
  def DOMContentLoadedHandler = (e: dom.Event) => {
    if (!Viewer.canRun) {
      fatal("AnkiIframeViewer cannot work on old browsers :(")
      sys.exit(2)
    }
    if (audioElements.nonEmpty) {
      audioPlayer = new AudioPlayer(this, audioElements)
    }
    flash = new Flash(this, Option(audioPlayer))
    viewer = new Viewer(this, chapterElements)

    if (audioElements.nonEmpty) {
      // Trying to call `AudioPlayer.load()` and `AudioPlayer.play()` without distinction of
      // with or without UserTouchPrivilege.
      // If the call of `load()` failed, `callWithUserTouchPrivilege()` will be called on the
      // first touch by the user and retry it with UserTouchPrivilege.
      audioPlayer.load()
      if (autoPlayAudio) {
        audioPlayer.play()
      }
    }

    mouseWheelEvent = new MouseWheelEvent(this)
    debug(f"setting event listeners for `mousewheel`")
    document.addEventListener("mousewheel", mouseWheelHandler)

    touchEvent = new TouchEvent(this, Option(audioPlayer))
    val touchDispatcher = new Dispatcher(this)
    val mouseDispatcher = new Dispatcher(this, Option(touchDispatcher))
    debug(f"setting event listeners for `mousedown`, `mousemove` and `mouseup`")
    document.addEventListener("mousedown", mouseHandler(mouseDispatcher.dispatchStart) _)
    document.addEventListener("mousemove", mouseHandler(mouseDispatcher.dispatchMove) _)
    document.addEventListener("mouseup",   mouseHandler(mouseDispatcher.dispatchEnd) _)
    debug(f"setting event listeners for `touchstart`, `touchmove`, `touchend` and `touchcancel`")
    document.addEventListener("touchstart",  touchHandler(touchDispatcher.dispatchStart) _)
    document.addEventListener("touchmove",   touchHandler(touchDispatcher.dispatchMove) _)
    document.addEventListener("touchend",    touchHandler(touchDispatcher.dispatchEnd) _)
    document.addEventListener("touchcancel", touchHandler(touchDispatcher.dispatchEnd) _)
  }
  def loadHandler = (e: dom.Event) => {
    if (Viewer.canRun) {
      viewer.goFirstChapter()
    }
  }
}