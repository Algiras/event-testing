package presentation.candidate

import eu.timepit.refined.string.Url
import org.joda.time.DateTime
import presentation.shared.UserId

sealed trait ProcessStepType

object ProcessStepType {
  final case class CVReview(cvUrl: Url) extends ProcessStepType
  final case class Interview(at: DateTime, interviewer: UserId) extends ProcessStepType
  final case object Offer extends ProcessStepType
}