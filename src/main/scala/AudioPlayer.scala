import org.scalajs.dom
import org.scalajs.dom.html
import scala.collection.mutable

class AudioPlayer(app: App, audio: html.Audio) extends Logger {
  private val listeners = mutable.HashSet.empty[Double]
  private var prepared = false
  private var playing = false

  private def playHandler = (event: dom.Event) => {
    debug(f"callback playHandler| listeners: $listeners")
    debug(f"duration: ${audio.duration}")
  }
  private def loadedDataHandler = (event: dom.Event) => {
    debug(f"callback loadedDataHandler| listeners: $listeners")
    debug(f"duration: ${audio.duration}")
  }
  private def canPlayHandler = (event: dom.Event) => {
    debug(f"callback canPlayHandler| listeners: $listeners")
  }
  private def canPlayThroughHandler = (event: dom.Event) => {
    debug(f"callback canPlayThroughHandler| listeners: $listeners")
  }
  private def durationChangeHandler = (event: dom.Event) => {
    debug(f"callback durationChangeHandler| listeners: $listeners")
  }
  private def stalledHandler = (event: dom.Event) => {
    debug(f"callback stalledHandler| listeners: $listeners")
  }
  private def errorHandler = (event: dom.Event) => {
    debug(f"callback errorHandler| listeners: $listeners")
  }
  private def abortHandler = (event: dom.Event) => {
    debug(f"callback abortHandler| listeners: $listeners")
  }
  private def endedHandler = (event: dom.Event) => {
    debug(f"callback endedHandler| listeners: $listeners")
    debug(f"duration: ${audio.duration}")
    playing = false
    if (app.holdReplayAudio && listeners.nonEmpty) {
      _play()
    }
  }

  def play(): Unit = {
    debug(f"play| listeners: $listeners")
    _play()
  }
  private def prepare(): Unit = {
    debug(f"prepare| listeners: $listeners")
    if (!prepared) {
      debug(f"setting event listeners to the audio element")
      audio.addEventListener("play", playHandler)
      audio.addEventListener("canplay", canPlayHandler)
      audio.addEventListener("canplaythrough", canPlayThroughHandler)
      audio.addEventListener("durationchange", durationChangeHandler)
      audio.addEventListener("stalled", stalledHandler)
      audio.addEventListener("error", errorHandler)
      audio.addEventListener("abort", abortHandler)
      audio.addEventListener("loadeddata", loadedDataHandler)
      audio.addEventListener("ended", endedHandler)
      prepared = true
    }
  }
  private def _play(): Unit = {
    debug(f"_play| listeners: $listeners")
    prepare()
    if (listeners.isEmpty || !playing) {
      audio.play()
    }
  }
  def contract(id: Double): Unit = {
    debug(f"contract| listeners: $listeners")
    play()
    listeners.add(id)
    debug(f"listeners: $listeners")
  }
  def cancel(id: Double): Unit = {
    debug(f"cancel| listeners: $listeners")
    listeners.remove(id)
    debug(f"listeners: $listeners")
  }
}
