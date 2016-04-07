
import scala.scalajs.js
import org.scalajs.dom.{document, html, window}

class Flash(app: App) extends Logger {
  // container
  private val container = document.createElement("div").asInstanceOf[html.Div]
  container.id = "flash-container"
  hide()
  document.body.appendChild(container)
  // info
  private val info = document.createElement("div").asInstanceOf[html.Div]
  info.id = "flash-container-info"
  private val infoText = document.createElement("div").asInstanceOf[html.Div]
  infoText.id = "flash-container-info-text"
  info.appendChild(infoText)
  container.appendChild(info)
  // page
  private val pageState = document.createElement("div").asInstanceOf[html.Div]
  pageState.id = "flash-container-page-state"
  private val pageStateLeft = document.createElement("div").asInstanceOf[html.Div]
  pageStateLeft.id = "flash-container-page-state-left"
  private val pageStateRight = document.createElement("div").asInstanceOf[html.Div]
  pageStateRight.id = "flash-container-page-state-right"
  private val pageStateNumerator = document.createElement("div").asInstanceOf[html.Div]
  pageStateNumerator.id = "flash-container-page-state-numerator"
  private val pageStateDenominator = document.createElement("div").asInstanceOf[html.Div]
  pageStateDenominator.id = "flash-container-page-state-denominator"
  private val pageStateText = document.createElement("div").asInstanceOf[html.Div]
  pageStateText.id = "flash-container-page-state-text"
  pageStateLeft.appendChild(pageStateText)
  pageStateRight.appendChild(pageStateNumerator)
  pageStateRight.appendChild(pageStateDenominator)
  pageState.appendChild(pageStateLeft)
  pageState.appendChild(pageStateRight)
  container.appendChild(pageState)

  private var timer: Option[Int] = None

  private def clearTimer(): Unit = {
    if (timer.isDefined) {
      window.clearInterval(timer.get)
      timer = None
    }
  }

  private def init(): Unit = {
    hide()
    clearTimer()
    info.style.display = "none"
    pageState.style.display = "none"
  }

  private def informationInit(): Unit = {
    init()
    info.style.display = "block"
  }

  private def pageStateInit(): Unit = {
    init()
    val page = math.floor(window.pageXOffset / window.innerWidth).toInt + 1
    val pages = math.floor(document.body.scrollWidth / window.innerWidth).toInt
    debug(f"page: $page, pages: $pages")
    pageStateNumerator.innerHTML = page.toString
    pageStateDenominator.innerHTML = pages.toString
    pageState.style.display = "block"
  }

  private def hide(): Unit = {
    container.style.visibility = "hidden"
  }

  private def show(): Unit = {
    container.style.opacity = "1"
    container.style.visibility = "visible"
  }

  private def fadeTimer(t1: Double) = () => {
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
  }

  private def cast(): Unit = {
    show()
    val id = window.setInterval(fadeTimer(js.Date.now()), app.UIRefreshIntervalMillis)
    timer = Some(id)
  }

  def castInformation(s: String): Unit = {
    informationInit()
    infoText.innerHTML = s
    cast()
  }

  def castPageState(caption: Option[String]): Unit = {
    pageStateInit()
    pageStateText.innerHTML = caption.getOrElse("")
    cast()
  }
}
