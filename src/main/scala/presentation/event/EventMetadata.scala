package presentation.event

import presentation.shared.UserId
import com.github.nscala_time.time.Imports.DateTime

sealed trait EventMetadata {
  def at: DateTime
}

object EventMetadata {
  case class SystemEventMetadata(at: DateTime) extends EventMetadata
  case class UserEventMetadata(by: UserId, at: DateTime) extends EventMetadata
}
