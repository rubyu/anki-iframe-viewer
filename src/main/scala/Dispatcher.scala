import scala.scalajs.js

class Dispatcher(app: App, preferredDispatcher: Option[Dispatcher] = None) extends Logger {
  val gesture = new Gesture(app)

  private def isDuplicateEvent(x: Double, y: Double, timestamp: Double) =
    preferredDispatcher match {
      case Some(dispatcher) => dispatcher.gesture.hasDuplicateEvent(x, y, timestamp)
      case None => false
    }

  def dispatchStart(id: Double, x: Double, y: Double): Unit = {
    debug(f"dispatch start")
    if (!isDuplicateEvent(x, y, js.Date.now())) {
      gesture.start(id, x, y)
    } else {
      debug(f"is a duplicate event")
    }
  }
  def dispatchMove(id: Double, x: Double, y: Double): Unit = {
    //debug(f"dispatch move")
    gesture.move(id, x, y)
  }
  def dispatchEnd(id: Double, x: Double, y: Double): Unit = {
    debug(f"dispatch end")
    gesture.end(id, x, y)
  }
}
