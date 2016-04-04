import scala.scalajs.js

class Dispatcher(touchEvent: TouchEvent, preferredDispatcher: Option[Dispatcher] = None) extends Logger {
  val gesture = new Gesture(touchEvent)

  var lastDispatchTime = js.Date.now()

  def dispatchStart(id: Int, x: Double, y: Double): Unit = gesture.start(id, x, y)
  def dispatchMove(id: Int, x: Double, y: Double): Unit = gesture.move(id, x, y)
  def dispatchEnd(id: Int, x: Double, y: Double): Unit = {
    lastDispatchTime = js.Date.now()
    if (preferredDispatcher.isDefined &&
        lastDispatchTime - preferredDispatcher.get.lastDispatchTime < App.dispatcherDuplicateEventWindowMillis) {
      debug(f"duplicate event: $id")
      return
    }
    gesture.end(id, x, y)
    gesture.get(id).tpe match {
      case ND =>
      case LeftTap => touchEvent.leftTap()
      case CenterTap => touchEvent.centerTap()
      case RightTap => touchEvent.rightTap()
      case LongTap => touchEvent.longTapEnd()
      case SwipeLeft => touchEvent.left()
      case SwipeRight => touchEvent.right()
      case SwipeUp => touchEvent.up()
      case SwipeDown => touchEvent.down()
      case LongSwipeLeft => touchEvent.longLeft()
      case LongSwipeRight => touchEvent.longRight()
      case LongSwipeUp => touchEvent.longUp()
      case LongSwipeDown => touchEvent.longDown()
    }
  }
}
