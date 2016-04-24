import org.scalajs.dom
import org.scalajs.dom.raw
import org.scalajs.dom.{document, window}

object Viewer {
  def canRun: Boolean = {
    val elem = document.createElement("div").asInstanceOf[dom.html.Div]
    val value = "100px"
    elem.style.columnWidth = value
    value == elem.style.columnWidth.toString
  }
}

class Viewer(app: App, chapterElements: List[raw.HTMLUnknownElement]) extends Logger {

  private val unsortedChapters =
    chapterElements
      .map(new Chapter(_))
  private var chapters: List[Chapter] = _
  private var lastMarker: Option[Marker] = None

  private def updateState(): Unit = {
    trace("updateState")
    chapters =
      unsortedChapters
      .sortBy(_.offsetLeft)
  }
  updateState()

  def totalSize: Double = document.body.scrollWidth
  def viewSize: Double = window.innerWidth
  def position: Double = window.pageXOffset

  private def ordinalPosition(m: Double): Double = {
    val n = viewSize
    if (m > totalSize - viewSize) {
      error(f"$m is greater than application total size")
      totalSize - viewSize
    } else {
      m % n match {
        case q if q > 0 =>
          trace(f"q > 0")
          val o = math.floor((m / n) + 0.5) * n
          trace(f"m: $m, n: $n, q: $q, fixed position: $o")
          o
        case q =>
          trace(f"q == 0")
          trace(f"m: $m, n: $n, q: $q")
          m
      }
    }
  }

  private def setPosition(d: Double): Unit = {
    debug(f"setPosition: $d")
    val o = ordinalPosition(d)
    debug(f"ordinalPosition: $o")
    val (chapter, relative) = activeChapters(o) match {
      case xs if xs.isEmpty => (None, o / totalSize)
      case xs => chapterSize(xs.last) match {
        case n if n > 0 => (Some(xs.head), (o - xs.head.offsetLeft) / n)
        case n => (Some(xs.head), 0.0)
      }
    }
    val marker = new Marker(this, o, totalSize, chapter, relative)
    debug(f"set marker: $marker")
    lastMarker = Some(marker)
    window.scrollTo(o.toInt, 0)
    app.flash.updateProgressBar()
  }

  /* Calculate the size of given chapter by subtracting offsetLeft of the next chapter from
   * given chapter's.
   * Return value may be zero.
   * */
  private def chapterSize(c: Chapter) = {
    val next =
      if (chapters.last == c) EndOfCard
      else chapters(chapters.indexOf(c)+1)
    next.offsetLeft - c.offsetLeft
  }

  private def activeChapters(d: Double): List[Chapter] = {
    chapters.reverse.find(_.offsetLeft <= d) match {
      case Some(lower) if d == lower.offsetLeft => chapters.filter(_.offsetLeft == lower.offsetLeft)
      case Some(lower) => lower :: Nil
      case None => Nil
    }
  }
  private def activeChapters: List[Chapter] = activeChapters(position)

  private def atFirstPageOf(c: Chapter) = position == c.offsetLeft

  def go(chapter: LogicalChapter): Unit = {
    debug(f"go: $chapter")
    updateState()
    setPosition(chapter.offsetLeft)
  }
  def goPrevPage(): Unit = {
    debug(f"goPrevPage")
    updateState()
    setPosition(position - viewSize)
  }
  def goNextPage(): Unit = {
    debug(f"goNextPage")
    updateState()
    setPosition(position + viewSize)
  }
  def goPrevChapter(): Unit = {
    debug(f"goPrevChapter")
    updateState()
    if (chapters.isEmpty) {
      go(StartOfCard)
      return
    }
    activeChapters match {
      case Nil => go(StartOfCard)
      case xs => xs.head match {
        case c if !atFirstPageOf(c) => go(c)
        case c if c == chapters.head => go(StartOfCard)
        case c => go(chapters(chapters.indexOf(c)-1))
      }
    }
  }
  def goNextChapter(): Unit = {
    debug(f"goNextChapter")
    updateState()
    if (chapters.isEmpty) {
      go(EndOfCard)
      return
    }
    activeChapters match {
      case Nil => go(EndOfCard)
      case xs => xs.last match {
        case c if c == chapters.last => go(EndOfCard)
        case c => go(chapters(chapters.indexOf(c)+1))
      }
    }
  }
  def goFirstChapter(): Unit = {
    debug(f"goFirstChapter")
    updateState()
    if (chapters.isEmpty) go(StartOfCard)
    else go(chapters.head)
  }
  def goLastChapter(): Unit = {
    debug(f"goLastChapter")
    updateState()
    if (chapters.isEmpty) go(EndOfCard)
    else go(chapters.last)
  }

  private def viewChangeHandler = (event: dom.Event) => {
    debug(f"callback of ${ event.`type` }")
    debug(f"position: $position")
    updateState()
    lastMarker match {
      case Some(marker) if marker.hasChangedTotal =>
        debug("the total size of the view has changed")
        marker.chapter match {
          case Some(c) =>
            debug(f"restore the position from previous relative value on the last chapter")
            setPosition(c.offsetLeft + chapterSize(c) * marker.relative)
          case None =>
            debug(f"restore the position from previous relative value to the entire view size")
            setPosition(totalSize * marker.relative)
        }
      case Some(marker) if marker.hasDisturbedMark =>
        debug("the position changed by disturbance factors")
        debug(f"restore the previous position")
        setPosition(marker.mark)
      case _ =>
        val op = ordinalPosition(position)
        if (op != position) {
          debug(f"the position is not ordinal")
          debug(f"fixed position: $op")
          setPosition(op)
        }
    }
  }

  debug(f"unsortedChapters: $unsortedChapters")
  debug(f"chapters: $chapters")
  debug(f"setting event listeners for `scroll` and `resize`")
  document.addEventListener("scroll", viewChangeHandler)
  window.addEventListener("resize", viewChangeHandler)
}
