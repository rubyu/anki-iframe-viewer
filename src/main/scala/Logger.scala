
import scala.annotation.elidable
import scala.scalajs.js.Dynamic.global

trait LoggerKey {
  implicit val loggerKey = this
}

object Logger {
  @elidable(elidable.FINEST)
  def trace(x: Any)(implicit key: LoggerKey): Unit =
    global.console.log(s"[${key.getClass.getName}][trace] ", x.toString)

  @elidable(elidable.FINE)
  def debug(x: Any)(implicit key: LoggerKey): Unit =
    global.console.log(s"[${key.getClass.getName}][debug] ", x.toString)

  @elidable(elidable.INFO)
  def info(x: Any)(implicit key: LoggerKey): Unit =
    global.console.log(s"[${key.getClass.getName}][info] ", x.toString)

  @elidable(elidable.WARNING)
  def warn(x: Any)(implicit key: LoggerKey): Unit =
    global.console.log(s"[${key.getClass.getName}][warn] ", x.toString)

  @elidable(elidable.SEVERE)
  def error(x: Any)(implicit key: LoggerKey): Unit =
    global.console.log(s"[${key.getClass.getName}][error] ", x.toString)

  @elidable(elidable.OFF)
  def fatal(x: Any)(implicit key: LoggerKey): Unit =
    global.console.log(s"[${key.getClass.getName}][fatal] ", x.toString)
}
