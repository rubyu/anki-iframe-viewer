
class TouchEvent(viewer: Viewer, audioPlayer: AudioPlayer) extends Logger {
  def up(): Unit = viewer.goNextPage()
  def down(): Unit = viewer.goPrevPage()
  def left(): Unit = viewer.goNextPage()
  def right(): Unit = viewer.goPrevPage()
  def longUp(): Unit = viewer.goNextChapter()
  def longDown(): Unit = viewer.goPrevChapter()
  def longLeft(): Unit = viewer.goNextChapter()
  def longRight(): Unit = viewer.goPrevChapter()
  def leftTap(): Unit = viewer.goNextPage()
  def centerTap(): Unit = {}
  def rightTap(): Unit = viewer.goPrevPage()
  def longTapStart(): Unit = audioPlayer.contract()
  def longTapEnd(): Unit = audioPlayer.cancel()
  def firstTouch(): Unit = audioPlayer.setup()
}
