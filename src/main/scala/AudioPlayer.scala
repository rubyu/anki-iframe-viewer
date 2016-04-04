
import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.html


class AudioPlayer(audio: html.Audio) extends Logger {
  var listener = 0
  def canPlay = audio.readyState.toString.toInt > 0
  def setup(): Unit = {
    debug(f"setup| listener: $listener")
    debug(f"canPlay: $canPlay")
    if (!canPlay) {
      debug(f"trying to load audio")
      audio.load()
      audio.addEventListener("ended", audioEndedHandler)
    }
  }
  def _play(): Unit = {
    debug(f"play| listener: $listener")
    audio.play()
  }
  def contract(): Unit = {
    debug(f"contract| listener: $listener")
    if (listener == 0) _play()
    listener += 1
    debug(f"listener: $listener")
  }
  def cancel(): Unit = {
    debug(f"cancel| listener: $listener")
    listener -= 1
    debug(f"listener: $listener")
  }

  var audioEndedHandler: js.Function1[dom.Event, Any] = (e: dom.Event) => {
    debug(f"ended| listener: $listener")
    if (listener > 0) _play()
  }
}
