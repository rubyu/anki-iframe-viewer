import org.scalajs.dom
import org.scalajs.dom.html

import scala.annotation.elidable
import scala.collection.mutable

class AudioPlayer(app: App, audio: html.Audio) extends NeedUserTouchPrivilege with Logger {
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
    if (!privilegeCalled && !canPlay) {
      audio.load()
      privilegeCalled = true
    }
  }

  private def debugHandler = (event: dom.Event) => {
    dump(f"callback of ${ event.`type` }")
  }

  private def playHandler = (event: dom.Event) => {
    if (canPlay && !playing) {
      playing = true
      app.flash.musicStart()
    }
  }

  private def canPlayHandler = (event: dom.Event) => {
    canPlay = true
  }

  private def endedHandler = (event: dom.Event) => {
    if (app.holdReplayAudio && listeners.nonEmpty) {
      _play()
    } else {
      playing = false
      app.flash.musicEnd()
    }
  }

  @elidable(elidable.FINE)
  def attachDebugEventListeners(): Unit = {
    audio.addEventListener("play", debugHandler)
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
      audio.addEventListener("play", playHandler)
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
