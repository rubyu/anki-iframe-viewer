import scala.scalajs.js

class Dispatcher(app: App, preferredDispatcher: Option[Dispatcher] = None) extends Logger {
  val gesture = new Gesture(app)

  var lastDispatchTime = js.Date.now()

  def dispatchStart(id: Double, x: Double, y: Double): Unit = gesture.start(id, x, y)
  def dispatchMove(id: Double, x: Double, y: Double): Unit = gesture.move(id, x, y)
  def dispatchEnd(id: Double, x: Double, y: Double): Unit = {
    lastDispatchTime = js.Date.now()
    if (preferredDispatcher.isDefined &&
        lastDispatchTime - preferredDispatcher.get.lastDispatchTime < app.dispatcherDuplicateEventWindowMillis) {
      debug(f"duplicate event: $id")
      return
    }
    if (gesture.has(id)) {
      gesture.end(id, x, y)
      gesture.get(id).tpe match {
        case ND => // do nothing
        case LeftTap => app.touchEvent.leftTap()
        case CenterTap => app.touchEvent.centerTap()
        case RightTap => app.touchEvent.rightTap()
        case LongTap => app.touchEvent.longTapEnd(id)
        case SwipeLeft => app.touchEvent.left()
        case SwipeRight => app.touchEvent.right()
        case SwipeUp => app.touchEvent.up()
        case SwipeDown => app.touchEvent.down()
        case LongSwipeLeft => app.touchEvent.longLeft()
        case LongSwipeRight => app.touchEvent.longRight()
        case LongSwipeUp => app.touchEvent.longUp()
        case LongSwipeDown => app.touchEvent.longDown()
        case TimeOut => // do nothing
      }
      gesture.delete(id)
    }
  }
}
