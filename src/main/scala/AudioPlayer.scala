import org.scalajs.dom
import org.scalajs.dom.html
import scala.collection.mutable

class AudioPlayer(app: App, audio: html.Audio) extends Logger {
  private val listeners = mutable.HashSet.empty[Double]
  private var loaded = false

  private def endedHandler = (event: dom.Event) => {
    debug(f"ended| listeners: $listeners")
    if (app.repeatAudio && listeners.nonEmpty) _play()
  }

  def load(): Unit = {
    if (!loaded) {
      debug(f"setup| listeners: $listeners")
      debug(f"trying to load audio")
      audio.load()
      if (app.autoPlayAudio) {
        debug(f"trying to play audio")
        _play()
      }
      audio.addEventListener("ended", endedHandler)
      loaded = true
    }
  }
  private def _play(): Unit = {
    debug(f"play| listeners: $listeners")
    audio.play()
  }
  def contract(id: Double): Unit = {
    debug(f"contract| listeners: $listeners")
    load()
    if (listeners.isEmpty ||
        !app.repeatAudio && audio.ended) _play()
    listeners.add(id)
    debug(f"listeners: $listeners")
  }
  def cancel(id: Double): Unit = {
    debug(f"cancel| listeners: $listeners")
    listeners.remove(id)
    debug(f"listeners: $listeners")
  }
}
