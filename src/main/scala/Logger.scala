import scala.annotation.elidable
import scala.scalajs.js.Dynamic._

trait Logger {
  private def log(s: String): Unit =
    global.console.log(s)

  private def seqToLogString(x: Seq[_]) =
    x.map(_.toString).mkString(" ")

  private val traceText = s"[trace][${ this.getClass.getSimpleName }]"
  private val debugText = s"[debug][${ this.getClass.getSimpleName }]"
  private val infoText  = s"[info][${ this.getClass.getSimpleName }]"
  private val warnText  = s"[warn][${ this.getClass.getSimpleName }]"
  private val errorText = s"[error][${ this.getClass.getSimpleName }]"
  private val fatalText = s"[fatal][${ this.getClass.getSimpleName }]"

  @elidable(elidable.FINEST)
  def trace(x: Any*): Unit =
    log(s"${ traceText } ${ seqToLogString(x) }")

  @elidable(elidable.FINE)
  def debug(x: Any*): Unit =
    log(s"${ debugText } ${ seqToLogString(x) }")

  @elidable(elidable.INFO)
  def info(x: Any*): Unit =
    log(s"${ infoText } ${ seqToLogString(x) }")

  @elidable(elidable.WARNING)
  def warn(x: Any*): Unit =
    log(s"${ warnText } ${ seqToLogString(x) }")

  @elidable(elidable.SEVERE)
  def error(x: Any*): Unit =
    log(s"${ errorText } ${ seqToLogString(x) }")

  @elidable(elidable.OFF)
  def fatal(x: Any*): Unit =
    log(s"${ fatalText } ${ seqToLogString(x) }")
}
