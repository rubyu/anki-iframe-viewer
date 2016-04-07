
class MouseWheelEvent(app: App) extends Logger {
  def wheelUp(): Unit = app.viewer.goPrevPage()
  def wheelDown(): Unit = app.viewer.goNextPage()
}
