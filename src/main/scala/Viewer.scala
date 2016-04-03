
import scala.collection.mutable
import org.scalajs.dom.{window, document, html}
import org.scalajs.dom.raw.HTMLUnknownElement

object Viewer {
  def canRun: Boolean = {
    val elem = document.createElement("div").asInstanceOf[html.Div]
    val value = "100px"
    elem.style.columnWidth = value
    value == elem.style.columnWidth.toString
  }
}

class Viewer(flash: Flash) extends Logger {
  val chapters = mutable.ArrayBuffer.empty[Chapter]
  def registerChapterById(id: String, caption: String): Unit = {
    chapters += Chapter(document.getElementById(id).asInstanceOf[HTMLUnknownElement], caption)
  }
  def castCurrentState(): Unit = {
    activeChapter match {
      case Some(c) => flash.cast(Some(c.caption))
      case None =>
    }
  }
  def setViewLeft(left: Double, caption: String) = setViewLeft(left, Some(caption))
  def setViewLeft(left: Double) = setViewLeft(left, None)
  def setViewLeft(left: Double, caption: Option[String]): Unit = {
    window.scrollTo(left.toInt, 0)
    flash.cast(Some(caption))
  }
  def run(): Unit ={
    info("start")
  }
  def activeChapter = {
    chapters.reverse.find { chapter =>
      if (window.pageXOffset >= chapter.element.offsetLeft) true
      else false
    }
  }
  def go(chapter: Chapter): Unit = setViewLeft(chapter.element.offsetLeft, chapter.caption)
  def goPrevPage(): Unit = setViewLeft(window.pageXOffset - window.screen.width)
  def goNextPage(): Unit = setViewLeft(window.pageXOffset + window.screen.width)
  def goPrevChapter(): Unit = {
    chapters.reverse.find { chapter =>
      if (window.pageXOffset > chapter.element.offsetLeft) true
      else false
    } match {
      case Some(chapter) => go(chapter)
      case None => setViewLeft(chapters.head.element.offsetLeft, "カードの先頭")
    }
  }
  def goNextChapter(): Unit = {
    chapters find { chapter =>
      if (window.pageXOffset < chapter.element.offsetLeft) true
      else false
    } match {
      case Some(chapter) => go(chapter)
      case None => setViewLeft(document.body.scrollWidth, "カードの終わり")
    }
  }
  def goFirstChapter(): Unit = go(chapters.head)
  def goLastChapter(): Unit = go(chapters.last)
}
