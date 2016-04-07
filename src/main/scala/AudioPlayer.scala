import org.scalajs.dom
import org.scalajs.dom.html
import scala.collection.mutable

class AudioPlayer(app: App, audio: html.Audio) extends Logger {
  private val listeners = mutable.HashSet.empty[Double]
  private var loaded = false
  private var played = false

  private def loadedDataHandler = (event: dom.Event) => {
    debug(f"loaded| listeners: $listeners")
    loaded = true
  }
  private def canPlayHandler = (event: dom.Event) => {
    debug(f"canplay| listeners: $listeners")
  }
  private def canPlahThroughHandler = (event: dom.Event) => {
    debug(f"canplaythrough| listeners: $listeners")
  }
  private def durationChangeHandler = (event: dom.Event) => {
    debug(f"durationChange| listeners: $listeners")
  }
  private def stalledHandler = (event: dom.Event) => {
    debug(f"stalled| listeners: $listeners")
  }
  private def errorHandler = (event: dom.Event) => {
    debug(f"error| listeners: $listeners")
  }
  private def abortHandler = (event: dom.Event) => {
    debug(f"abort| listeners: $listeners")
  }
  private def endedHandler = (event: dom.Event) => {
    debug(f"ended| listeners: $listeners")
    played = true
    if (app.repeatAudio && listeners.nonEmpty) {
      _play()
    }
  }

  def load(): Unit = {
    debug(f"load| listeners: $listeners")
    _load()
    if (app.autoPlayAudio && !played) {
      debug(f"trying to play audio")
      _play()
    }
  }

  def _load(): Unit = {
    debug(f"_load| listeners: $listeners")
    if (!loaded) {
      debug(f"trying to load audio")
      audio.addEventListener("canplay", canPlayHandler)
      audio.addEventListener("canplaythrough", canPlahThroughHandler)
      audio.addEventListener("durationchange", durationChangeHandler)
      audio.addEventListener("stalled", stalledHandler)
      audio.addEventListener("error", errorHandler)
      audio.addEventListener("abort", abortHandler)
      audio.addEventListener("loadeddata", loadedDataHandler)
      audio.addEventListener("ended", endedHandler)
      audio.load()
    }
  }

  private def _play(): Unit = {
    debug(f"_play| listeners: $listeners")
    audio.play()
  }
  def contract(id: Double): Unit = {
    debug(f"contract| listeners: $listeners")
    _load()
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
