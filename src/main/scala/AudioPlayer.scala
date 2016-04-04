
import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.html


class AudioPlayer(audio: html.Audio) extends Logger {
  var listener = 0
  var setupped = false
  def setup(): Unit = {
    if (!setupped) {
      debug(f"setup| listener: $listener")
      debug(f"trying to load audio")
      audio.load()
      audio.play()
      audio.addEventListener("ended", (e: dom.Event) => {
        debug(f"ended| listener: $listener")
        if (listener > 0) _play()
      })
      setupped = true
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
}
