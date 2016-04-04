
class MouseWheelEvent(viewer: Viewer) extends Logger {
  def wheelUp(): Unit = viewer.goPrevPage()
  def wheelDown(): Unit = viewer.goNextPage()
}
