import org.scalajs.dom
import org.scalajs.dom.raw
import org.scalajs.dom.{document, window}

sealed trait LogicalChapter {
  def offsetLeft: Double
}

class Chapter(element: raw.HTMLUnknownElement) extends LogicalChapter {
  def offsetLeft = element.offsetLeft
}

object StartOfCard extends LogicalChapter {
  def offsetLeft = 0.0
}

object EndOfCard extends LogicalChapter {
  def offsetLeft = document.body.scrollWidth
}
