import scala.collection.mutable
import scala.scalajs.js
import org.scalajs.dom.{document, html, window}
import org.scalajs.dom.raw.HTMLUnknownElement

object Viewer {
  def canRun: Boolean = {
    val elem = document.createElement("div").asInstanceOf[html.Div]
    val value = "100px"
    elem.style.columnWidth = value
    value == elem.style.columnWidth.toString
  }
}

class Viewer(flash: Flash, chapters: List[Chapter]) extends Logger {
  def viewSize: Double = window.screen.width
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
    flash.cast(caption)
  }
  def findActiveChapter(list: Iterable[Chapter]): Option[LogicalChapter] = {
    val pos = ordinaryPosition(position)
    list.find { c =>
      if (pos >= c.offsetLeft && pos <= c.offsetLeft + c.offsetWidth) true
      else false
    } orElse {
      if (list.isEmpty) {
        if (position < document.body.scrollWidth / 2) Option(StartOfCard)
        else Option(EndOfCard)
      } else {
        if (pos < chapters.head.offsetLeft) Option(StartOfCard)
        else Option(EndOfCard)
      }
    }
  }
  def atFirstPageOf(c: Chapter) = position == c.offsetLeft
  def atLastPageOf(c: Chapter) = position > c.offsetLeft + c.offsetWidth - viewSize
  def go(chapter: LogicalChapter): Unit = {
    debug(f"go: $chapter")
    chapter match {
      case c: Chapter =>
        val p = c.offsetLeft
        val op = ordinaryPosition(p)
        if (p != op) {
          error(f"given position is not ordinary: $p; nearest ordinary position: $op")
        }
        setPosition(op, c.caption)
      case c => setPosition(c.offsetLeft, c.caption)
    }
  }
  def goPrevPage(): Unit = setPosition(ordinaryPosition(position) - viewSize)
  def goNextPage(): Unit = setPosition(ordinaryPosition(position) + viewSize)
  def goPrevChapter(): Unit = {
    findActiveChapter(chapters) match {
      case Some(c: Chapter) if c == chapters.head => go(StartOfCard)
      case Some(c: Chapter) => go(chapters(chapters.indexOf(c)-1))
      case Some(StartOfCard) => go(StartOfCard)
      case Some(EndOfCard) =>
        if (chapters.isEmpty) go(StartOfCard)
        else go(chapters.last)
      case None => fatal(f"active chapter was not found")
    }
  }
  def goNextChapter(): Unit = {
    findActiveChapter(chapters.reverse) match {
      case Some(c: Chapter) if c == chapters.last => go(EndOfCard)
      case Some(c: Chapter) => go(chapters(chapters.indexOf(c)+1))
      case Some(EndOfCard) => go(EndOfCard)
      case Some(StartOfCard) =>
        if (chapters.isEmpty) go(EndOfCard)
        else go(chapters.head)
      case None => fatal(f"active chapter was not found")
    }
  }
  def goFirstChapter(): Unit = go(chapters.head)
  def goLastChapter(): Unit = go(chapters.last)
  def castCurrentState(): Unit = {
    findActiveChapter(chapters) match {
      case Some(c) => flash.cast(Option(c.caption))
      case None => fatal(f"active chapter was not found")
    }
  }
  def run(): Unit = {
    info("start")
  }
}
