import scala.collection.mutable
import scala.scalajs.js
import org.scalajs.dom.window
import org.scalajs.dom.window.screen

sealed trait GestureType
case object ND extends GestureType
case object LeftTap extends GestureType
case object CenterTap extends GestureType
case object RightTap extends GestureType
case object LongTap extends GestureType
case object SwipeLeft extends GestureType
case object SwipeRight extends GestureType
case object SwipeUp extends GestureType
case object SwipeDown extends GestureType
case object LongSwipeLeft extends GestureType
case object LongSwipeRight extends GestureType
case object LongSwipeUp extends GestureType
case object LongSwipeDown extends GestureType

class GestureLogItem(val x: Double, val y: Double, val timestamp: Double = js.Date.now())

class GestureLog(val start: GestureLogItem) {
  val moves = mutable.ArrayBuffer.empty[GestureLogItem]
  var end: Option[GestureLogItem] = None
  var tpe: GestureType = ND
}

class Gesture(touchEvent: TouchEvent) extends Logger {
  val gestures = mutable.HashMap.empty[Double, GestureLog]

  def tryFirstTouch(): Unit = {
    if (!App.alreadyTouched) {
      touchEvent.firstTouch()
      App.alreadyTouched = true
    }
  }

  def start(id: Double, x: Double, y: Double): Unit = {
    debug(f"start| id: $id, x: $x, y: $y")
    tryFirstTouch()
    val start = new GestureLogItem(x, y)
    val g = new GestureLog(start)
    gestures += (id -> g)
    window.setTimeout(() => {
      debug(f"callback of a timer to check whether or not LongTap; id: $id")
      if (g.end.isDefined) {
        debug("gesture already finished")
      } else {
        checkLongTap(g)
        if (g.tpe == LongTap) {
          debug(f"LongTap")
          touchEvent.longTapStart()
        } else {
          debug(f"not LongTap")
        }
      }
    }, App.minLongTouchMillis)
  }
  def move(id: Double, x: Double, y: Double): Unit = {
    //debug(f"move| id: $id, x: $x, y: $y")
    tryFirstTouch()
    if (!has(id)) {
      debug(f"the GestureLog corresponding to id($id) was not found")
      return
    }
    gestures(id).moves += new GestureLogItem(x, y)
  }
  def end(id: Double, x: Double, y: Double): Unit = {
    debug(f"end| id: $id, x: $x, y: $y")
    if (!has(id)) {
      debug(f"the GestureLog corresponding to id($id) was not found")
      return
    }
    val g = gestures(id)
    g.end = Some(new GestureLogItem(x, y))
    if (g.tpe == LongTap) {
      return
    }
    g.tpe = getGestureType(g.start, g.end.get)
    debug(f"type: ${ g.tpe }")
  }

  def delete(id: Double): Unit = gestures.remove(id)
  def has(id: Double) = gestures.contains(id)
  def get(id: Double) = gestures(id)

  def hasNoMoveEvents(g: GestureLog) = {
    g.moves.filter {
        event => event.timestamp - g.start.timestamp < App.minLongTouchMillis
    } forall { event =>
        math.max(math.abs(event.x - g.start.x), math.abs(event.y - g.start.y)) < App.minSwipeSize
    }
  }
  def checkLongTap(g: GestureLog): Unit = {
    if (hasNoMoveEvents(g)) {
      g.tpe = LongTap
    }
  }

  def getGestureType(start: GestureLogItem, end: GestureLogItem) =
    getSwipeType(start, end) getOrElse getTapType(end.x)

  def getTapType(x: Double): GestureType = {
    val pos = x - window.pageXOffset
    val border = screen.width / 2
    val centerBorder = screen.width * App.centerTapRatio / 2
    if (pos > border) {
      if (pos > border + centerBorder) RightTap
      else CenterTap
    } else {
      if (pos < border - centerBorder) LeftTap
      else CenterTap
    }
  }

  def getSwipeType(start: GestureLogItem, end: GestureLogItem): Option[GestureType] = {
    val dx = start.x - end.x
    val dy = start.y - end.y
    val x = math.abs(dx)
    val y = math.abs(dy)
    if (x > y) {
      if (x > App.minLongSwipeSize) {
        if (dx > 0) Some(LongSwipeLeft)
        else Some(LongSwipeRight)
      } else if (x > App.minSwipeSize) {
        if (dx > 0) Some(SwipeLeft)
        else Some(SwipeRight)
      } else {
        None
      }
    } else {
      if (y > App.minLongSwipeSize) {
        if (dy > 0) Some(LongSwipeUp)
        else Some(LongSwipeDown)
      } else if (y > App.minSwipeSize) {
        if (dy > 0) Some(SwipeUp)
        else Some(SwipeDown)
      } else {
        None
      }
    }
  }
}
