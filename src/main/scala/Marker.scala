
class Marker(viewer: Viewer, val mark: Double, val total: Double, val chapter: Option[Chapter], val relative: Double) {
  def hasChangedTotal = total != viewer.totalSize
  def hasDisturbedMark = mark != viewer.position
  override def toString = f"Marker(mark=$mark, total=$total, chapter=$chapter, relative=$relative)"
}
