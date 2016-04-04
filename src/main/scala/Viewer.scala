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
      case q if q > 0 => math.floor((m / n) + 0.5) * n
      case q => q * n
    }
  }
  def setPosition(pos: Double, caption: String) = setPosition(pos, Some(caption))
  def setPosition(pos: Double) = setPosition(pos, None)
  def setPosition(pos: Double, caption: Option[String]): Unit = {
    window.scrollTo(pos.toInt, 0)
    flash.cast(caption)
  }
  def findActiveChapter(list: Iterable[Chapter]): LogicalChapter = {
    val pos = ordinaryPosition(position)
    list.find { c =>
      if (pos >= c.offsetLeft && pos <= c.offsetLeft + c.offsetWidth) true
      else false
    } getOrElse {
      if (pos < chapters.head.offsetLeft) StartOfCard
      else EndOfCard
    }
  }
  def atFirstPageOf(c: Chapter) = position == c.offsetLeft
  def atLastPageOf(c: Chapter) = position > c.offsetLeft + c.offsetWidth - viewSize
  def go(chapter: LogicalChapter): Unit = {
    chapter match {
      case EndOfCard =>
      case c =>
        val p = c.offsetLeft
        val op = ordinaryPosition(p)
        if (p != op) {
          error(f"given position is not ordinary: $p; nearest ordinary position: $op")
        }
        setPosition(op, c.caption)
    }
  }
  def goPrevPage(): Unit = setPosition(ordinaryPosition(position) - viewSize)
  def goNextPage(): Unit = setPosition(ordinaryPosition(position) + viewSize)
  def goPrevChapter(): Unit = {
    findActiveChapter(chapters) match {
      case c: Chapter =>
        if (atFirstPageOf(c)) {
          if (c == chapters.head) go(StartOfCard)
          else go(chapters(chapters.indexOf(c)-1))
        }
      case c => go(c)
    }
  }
  def goNextChapter(): Unit = {
    findActiveChapter(chapters.reverse) match {
      case c: Chapter =>
        if (atLastPageOf(c)) {
          if (c == chapters.last) go(EndOfCard)
          else go(chapters(chapters.indexOf(c)+1))
        }
      case c => go(c)
    }
  }
  def goFirstChapter(): Unit = go(chapters.head)
  def goLastChapter(): Unit = go(chapters.last)
  def castCurrentState(): Unit = {
    flash.cast(findActiveChapter(chapters).caption)
  }
  def run(): Unit = {
    info("start")
  }
}
