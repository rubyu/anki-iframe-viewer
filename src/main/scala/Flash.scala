
import scala.scalajs.js
import org.scalajs.dom.{document, html, window}
import org.scalajs.dom.window.screen

class Flash extends Logger {
  val container = document.createElement("div").asInstanceOf[html.Div]
  container.id = "flash-container"
  hide()
  val left = document.createElement("div").asInstanceOf[html.Div]
  left.id = "flash-container-left"
  val right = document.createElement("div").asInstanceOf[html.Div]
  right.id = "flash-container-right"
  val numerator = document.createElement("div").asInstanceOf[html.Div]
  numerator.id = "flash-container-numerator"
  val denominator = document.createElement("div").asInstanceOf[html.Div]
  denominator.id = "flash-container-denominator"
  val text = document.createElement("div").asInstanceOf[html.Div]
  text.id = "flash-container-text"
  left.appendChild(text)
  right.appendChild(numerator)
  right.appendChild(denominator)
  container.appendChild(left)
  container.appendChild(right)
  document.body.appendChild(container)

  var timer: Option[Int] = None

  def refresh(): Unit = {
    val page = math.floor(window.pageXOffset / screen.width).toInt + 1
    val pages = math.floor(document.body.scrollWidth / screen.width).toInt
    debug(f"page: $page, pages: $pages")
    numerator.innerHTML = page.toString
    denominator.innerHTML = pages.toString
  }

  def clearTimer(): Unit = {
    if (timer.isDefined) {
      window.clearInterval(timer.get)
      timer = None
    }
  }

  def init(): Unit = {
    clearTimer()
    refresh()
    container.style.opacity = "1"
    container.style.visibility = "visible"
  }

  def hide(): Unit = {
    container.style.visibility = "hidden"
  }

  def cast(caption: Option[String]): Unit = {
    text.innerHTML = caption.getOrElse("")
    init()
    val t1 = js.Date.now()
    val id = window.setInterval(() => {
      val t2 = js.Date.now()
      val delta = t2 - t1
      //debug(f"timer published at: $t1; current: $t2")
      if (delta < 500) {
        // do nothing
      } else if (delta < 10000) {
        val alpha = 1 - math.pow((delta - 500) / 500, 2)
        if (alpha < 0) {
          clearTimer()
          hide()
        } else {
          container.style.opacity = f"$alpha%.5f"
        }
      }
    }, App.UIRefreshIntervalMillis)
    timer = Some(id)
  }
}
