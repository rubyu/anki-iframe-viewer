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

  private def chapters =
    unsortedChapters
      .sortWith(_.offsetLeft < _.offsetLeft)

  debug(f"unsortedChapters: $unsortedChapters")
  debug(f"chapters: $chapters")

  def viewSize: Double = window.innerWidth
  def position: Double = window.pageXOffset
  def ordinaryPosition(m: Double): Double = {
    val n = viewSize
    m % n match {
      case q if q > 0 =>
        debug(f"m: $m, n: $n, q: $q>0 ordinaryPosition: ${ math.floor((m / n) + 0.5) * n }")
        math.floor((m / n) + 0.5)
      case q =>
        debug(f"m: $m, n: $n, q: $q==0 ordinaryPosition: $m")
        m
    }
  }
  def setPosition(pos: Double, caption: String): Unit = setPosition(pos, Some(caption))
  def setPosition(pos: Double): Unit = setPosition(pos, None)
  def setPosition(pos: Double, caption: Option[String]): Unit = {
    debug(f"set position: $pos")
    window.scrollTo(pos.toInt, 0)
    app.flash.updateProgressBar()
  }
  def activeChapters: List[Chapter] = {
    val pos = ordinaryPosition(position)
    chapters.reverse.find(_.offsetLeft <= pos) match {
      case Some(lower) if pos == lower.offsetLeft => chapters.filter(_.offsetLeft == lower.offsetLeft)
      case Some(lower) => lower :: Nil
      case None => Nil
    }
  }
  def atFirstPageOf(c: Chapter) = position == c.offsetLeft
  def go(chapter: LogicalChapter): Unit = {
    debug(f"go: $chapter")
    chapter match {
      case c: Chapter =>
        val p = c.offsetLeft
        val op = ordinaryPosition(p)
        if (p != op) {
          error(f"given position is not ordinary: $p; nearest ordinary position: $op")
        }
        setPosition(op)
      case c => setPosition(c.offsetLeft)
    }
  }
  def goPrevPage(): Unit = setPosition(ordinaryPosition(position) - viewSize)
  def goNextPage(): Unit = setPosition(ordinaryPosition(position) + viewSize)
  def goPrevChapter(): Unit = {
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
    if (chapters.isEmpty) go(StartOfCard)
    else go(chapters.head)
  }
  def goLastChapter(): Unit = {
    if (chapters.isEmpty) go(EndOfCard)
    else go(chapters.last)
  }
}
