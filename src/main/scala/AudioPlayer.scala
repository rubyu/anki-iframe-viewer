import org.scalajs.dom
import org.scalajs.dom.raw
import org.scalajs.dom.{document, window}
import scala.annotation.elidable
import scala.collection.mutable

class AudioPlayer(app: App, audioElements: List[raw.HTMLAudioElement]) extends NeedUserTouchPrivilege with Logger {
  private val audio = audioElements.head
  private val listeners = mutable.HashSet.empty[Double]
  private var prepared = false
  private var privilegeCalled = false
  private var canPlay = false
  private var playing = false

  @elidable(elidable.FINE)
  private def dump(s: String): Unit = {
    debug(f"$s")
    debug(f"listeners: $listeners")
    debug(f"duration: ${ audio.duration }")
    debug(f"prepared: $prepared")
    debug(f"canPlay: $canPlay")
    debug(f"playing: $playing")
    debug(f"prepared: $prepared")
    debug(f"-" * 10)
  }

  def callWithUserTouchPrivilege(): Unit = {
    dump(f"callWithUserTouchPrivilege")
    if (!privilegeCalled && !canPlay) {
      load()
      privilegeCalled = true
    }
  }

  def load(): Unit = {
    dump(f"load")
    if (!canPlay) {
      audio.load()
    }
  }

  private def debugHandler = (event: dom.Event) => {
    dump(f"callback of ${ event.`type` }")
  }

  private def playingHandler = (event: dom.Event) => {
    playing = true
    app.flash.musicPlaying()
  }

  private def pauseHandler = (event: dom.Event) => {
    playing = false
    app.flash.musicDefault()
  }

  private def errorHandler = (event: dom.Event) => {
    playing = false
    app.flash.musicAlert()
  }

  private def canPlayHandler = (event: dom.Event) => {
    if (!audio.duration.isNaN && audio.duration > 0) {
      canPlay = true
    }
  }

  private def endedHandler = (event: dom.Event) => {
    if (app.holdReplayAudio && listeners.nonEmpty) {
      _play()
    }
  }

  /**
    * When call `load` without UserTouchPrivilege, nothing will be called.
    * When an error occurs on `load`, `abort` will be called.
    * When call `play` without UserTouchPrivilege, `suspend` will be called.
    * When an error occurs on `play`, `error` will be called.
    */

  @elidable(elidable.FINE)
  def attachDebugEventListeners(): Unit = {
    audio.addEventListener("play", debugHandler)
    audio.addEventListener("playing", debugHandler)
    audio.addEventListener("pause", debugHandler)
    audio.addEventListener("timeupdate", debugHandler)
    audio.addEventListener("suspend", debugHandler)
    audio.addEventListener("canplay", debugHandler)
    audio.addEventListener("canplaythrough", debugHandler)
    audio.addEventListener("durationchange", debugHandler)
    audio.addEventListener("stalled", debugHandler)
    audio.addEventListener("error", debugHandler)
    audio.addEventListener("abort", debugHandler)
    audio.addEventListener("loadeddata", debugHandler)
    audio.addEventListener("ended", debugHandler)
  }

  def play(): Unit = {
    dump(f"play")
    prepare()
    if (listeners.isEmpty || !playing) {
      _play()
    }
  }
  private def prepare(): Unit = {
    dump(f"prepare")
    if (!prepared) {
      debug(f"setting event listeners to the audio element")
      audio.addEventListener("play", playingHandler)
      audio.addEventListener("pause", pauseHandler)
      audio.addEventListener("error", errorHandler)
      audio.addEventListener("canplay", canPlayHandler)
      audio.addEventListener("ended", endedHandler)
      attachDebugEventListeners()
      prepared = true
    }
  }
  private def _play(): Unit = {
    dump(f"_play")
    audio.play()
  }
  def contract(id: Double): Unit = {
    dump(f"contract")
    play()
    listeners.add(id)
  }
  def cancel(id: Double): Unit = {
    dump(f"cancel")
    listeners.remove(id)
  }
}
