import org.scalajs.dom.raw.HTMLUnknownElement
import org.scalajs.dom.{document, html, window}

sealed trait LogicalChapter {
  def offsetLeft: Double
  def caption: String
}

class Chapter(element: HTMLUnknownElement, val caption: String) extends LogicalChapter {
  def offsetLeft = element.offsetLeft
  def offsetWidth = element.offsetWidth
}

object StartOfCard extends LogicalChapter {
  def offsetLeft = 0.0
  def caption = "カードのはじまり"
}

object EndOfCard extends LogicalChapter {
  def offsetLeft = document.body.scrollWidth
  def caption = "カードの終わり"
}
