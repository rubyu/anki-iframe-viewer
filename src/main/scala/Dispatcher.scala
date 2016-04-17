import scala.scalajs.js

class Dispatcher(app: App, preferredDispatcher: Option[Dispatcher] = None) extends Logger {
  val gesture = new Gesture(app)

  private def isDuplicateEvent(x: Double, y: Double, timestamp: Double) =
    preferredDispatcher match {
      case None => false
      case Some(dispatcher) => dispatcher.gesture.gestures.exists{ case (_, g) =>
        x == g.start.x &&
        y == g.start.y &&
        timestamp - g.start.timestamp < app.dispatcherDuplicateEventWindowMillis
      }
    }

  def dispatchStart(id: Double, x: Double, y: Double): Unit = {
    debug(f"dispatch start")
    if (!isDuplicateEvent(x, y, js.Date.now())) {
      gesture.start(id, x, y)
    } else {
      debug(f"is duplicate event")
    }
  }
  def dispatchMove(id: Double, x: Double, y: Double): Unit = {
    gesture.move(id, x, y)
  }
  def dispatchEnd(id: Double, x: Double, y: Double): Unit = {
    debug(f"dispatch end")
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
