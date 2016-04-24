import org.scalajs.dom
import org.scalajs.dom.raw
import org.scalajs.dom.{document, window}

sealed trait LogicalChapter {
  def offsetLeft: Double
}

class Chapter(element: raw.HTMLUnknownElement) extends LogicalChapter {
  def offsetLeft = element.offsetLeft
  override def toString = f"Chapter(element=$element, offsetLeft=$offsetLeft)"
}

object StartOfCard extends LogicalChapter {
  def offsetLeft = 0.0
  override def toString = f"StartOfCard"
}

object EndOfCard extends LogicalChapter {
  def offsetLeft = document.body.scrollWidth - window.innerWidth
  override def toString = f"EndOfCard"
}
