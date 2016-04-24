import org.scalajs.dom.{document, html, window}

class Flash(app: App, audioPlayer: Option[AudioPlayer]) extends Logger {
  // container
  private val horizontalBar = document.createElement("div").asInstanceOf[html.Div]
  horizontalBar.id = "horizontal-bar"
  hide()

  document.body.appendChild(horizontalBar)
  // icon 1
  private val icon1 = document.createElement("div").asInstanceOf[html.Div]
  icon1.id = "horizontal-bar-icon1"
  private val icon1_A = document.createElement("div").asInstanceOf[html.Div]
  icon1_A.id = "horizontal-bar-icon1-A"
  icon1_A.innerHTML = defaultMusicIcon
  private val icon1_B = document.createElement("div").asInstanceOf[html.Div]
  icon1_B.id = "horizontal-bar-icon1-B"
  icon1_B.innerHTML = musicPlayingIcon
  private val icon1_C = document.createElement("div").asInstanceOf[html.Div]
  icon1_C.id = "horizontal-bar-icon1-C"
  icon1_C.innerHTML = musicAlertIcon
  icon1.appendChild(icon1_A)
  icon1.appendChild(icon1_B)
  icon1.appendChild(icon1_C)
  horizontalBar.appendChild(icon1)
  // progress bar
  private val progress = document.createElement("div").asInstanceOf[html.Div]
  progress.id = "horizontal-bar-progress-bar"
  private val total = document.createElement("div").asInstanceOf[html.Div]
  total.id = "horizontal-bar-progress-total"
  private val current = document.createElement("div").asInstanceOf[html.Div]
  current.id = "horizontal-bar-progress-current"

  progress.appendChild(total)
  progress.appendChild(current)
  horizontalBar.appendChild(progress)

  showDefaultMusicIcon()
  updateProgressBar()
  show()

  def hide(): Unit = {
    horizontalBar.style.visibility = "hidden"
  }

  def show(): Unit = {
    horizontalBar.style.visibility = "visible"
  }

  private def defaultMusicIcon =
    audioPlayer match {
      case Some(_) => "&#x266b;"
      case None =>    ""
    }
  private def musicPlayingIcon = "&#x1f50a;"
  private def musicAlertIcon   = "&#x26a0;"

  private def showDefaultMusicIcon(): Unit = {
    icon1_B.style.visibility = "hidden"
    icon1_C.style.visibility = "hidden"
    icon1_A.style.visibility = "visible"
  }
  private def showMusicPlayingIcon() {
    icon1_A.style.visibility = "hidden"
    icon1_C.style.visibility = "hidden"
    icon1_B.style.visibility = "visible"
  }
  private def showMusicAlertIcon() {
    icon1_A.style.visibility = "hidden"
    icon1_B.style.visibility = "hidden"
    icon1_C.style.visibility = "visible"
  }

  private def currentProgressLengthInPercentile =
    progress.offsetWidth * (window.innerWidth + window.pageXOffset) / document.body.scrollWidth

  def updateProgressBar(): Unit = {
    current.style.visibility = "hidden"
    current.style.width = currentProgressLengthInPercentile.toString
    current.style.visibility = "visible"
  }

  def musicPlaying(): Unit = {
    showMusicPlayingIcon()
  }

  def musicDefault(): Unit = {
    showDefaultMusicIcon()
  }

  def musicAlert(): Unit = {
    showMusicAlertIcon()
  }
}
