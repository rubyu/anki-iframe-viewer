import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.html
import scala.collection.mutable

class AudioPlayer(audio: html.Audio) extends Logger {
  val listeners = mutable.HashSet.empty[Double]
  var loaded = false
  def load(): Unit = {
    if (!loaded) {
      debug(f"setup| listeners: $listeners")
      debug(f"trying to load audio")
      audio.load()
      audio.play()
      audio.addEventListener("ended", (event: dom.raw.Event) => {
        debug(f"ended| listeners: $listeners")
        if (listeners.nonEmpty) _play()
      })
      loaded = true
    }
  }
  def _play(): Unit = {
    debug(f"play| listeners: $listeners")
    audio.play()
  }
  def contract(id: Double): Unit = {
    debug(f"contract| listeners: $listeners")
    if (listeners.isEmpty) _play()
    listeners.add(id)
    debug(f"listeners: $listeners")
  }
  def cancel(id: Double): Unit = {
    debug(f"cancel| listeners: $listeners")
    listeners.remove(id)
    debug(f"listeners: $listeners")
  }
}
