
class TouchEvent(viewer: Viewer, audioPlayer: Option[AudioPlayer] = None) extends Logger {
  def up(): Unit = viewer.goNextPage()
  def down(): Unit = viewer.goPrevPage()
  def left(): Unit = viewer.goNextPage()
  def right(): Unit = viewer.goPrevPage()
  def longUp(): Unit = viewer.goNextChapter()
  def longDown(): Unit = viewer.goPrevChapter()
  def longLeft(): Unit = viewer.goNextChapter()
  def longRight(): Unit = viewer.goPrevChapter()
  def leftTap(): Unit = viewer.goPrevPage()
  def centerTap(): Unit = {}
  def rightTap(): Unit = viewer.goNextPage()
  def longTapStart(): Unit = if (audioPlayer.isDefined) audioPlayer.get.contract()
  def longTapEnd(): Unit = if (audioPlayer.isDefined) audioPlayer.get.cancel()
  def firstTouch(): Unit = if (audioPlayer.isDefined) audioPlayer.get.load()
}
