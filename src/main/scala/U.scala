import org.scalajs.dom._

object U {
  def isAnki = N.userAgent.contains("Anki")
  object W {
    def devicePixelRatio = window.devicePixelRatio
  }
  object N {
    def userAgent = window.navigator.userAgent
  }
}
