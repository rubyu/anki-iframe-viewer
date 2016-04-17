
class TouchEvent(app: App, audioPlayer: Option[AudioPlayer] = None) extends Logger {
  def up(): Unit = app.viewer.goNextPage()
  def down(): Unit = app.viewer.goPrevPage()
  def left(): Unit = app.viewer.goNextPage()
  def right(): Unit = app.viewer.goPrevPage()
  def longUp(): Unit = app.viewer.goNextChapter()
  def longDown(): Unit = app.viewer.goPrevChapter()
  def longLeft(): Unit = app.viewer.goNextChapter()
  def longRight(): Unit = app.viewer.goPrevChapter()
  def leftTap(): Unit = app.viewer.goPrevPage()
  def centerTap(): Unit = if (audioPlayer.isDefined) audioPlayer.get.play()
  def rightTap(): Unit = app.viewer.goNextPage()
  def longTapStart(id: Double): Unit = if (audioPlayer.isDefined) audioPlayer.get.contract(id)
  def longTapEnd(id: Double): Unit = if (audioPlayer.isDefined) audioPlayer.get.cancel(id)
}
