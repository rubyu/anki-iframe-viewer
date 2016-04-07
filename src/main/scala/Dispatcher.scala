import scala.scalajs.js

class Dispatcher(app: App, touchEvent: TouchEvent, preferredDispatcher: Option[Dispatcher] = None) extends Logger {
  val gesture = new Gesture(app, touchEvent)

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
        case ND =>
        case LeftTap => touchEvent.leftTap()
        case CenterTap => touchEvent.centerTap()
        case RightTap => touchEvent.rightTap()
        case LongTap => touchEvent.longTapEnd(id)
        case SwipeLeft => touchEvent.left()
        case SwipeRight => touchEvent.right()
        case SwipeUp => touchEvent.up()
        case SwipeDown => touchEvent.down()
        case LongSwipeLeft => touchEvent.longLeft()
        case LongSwipeRight => touchEvent.longRight()
        case LongSwipeUp => touchEvent.longUp()
        case LongSwipeDown => touchEvent.longDown()
      }
      gesture.delete(id)
    }
  }
}
