package presentation.candidate

import derevo.cats._
import derevo.derive
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.string.Url
import org.joda.time.DateTime
import eu.timepit.refined.cats._
import presentation.shared._

@derive(show)
sealed trait ProcessStepType

object ProcessStepType {
  type UrlR = String Refined Url

  @derive(show)
  object UrlR extends RefinedTypeOps[UrlR, String]


  final case class CVReview(cvUrl: UrlR) extends ProcessStepType
  final case class Interview(at: DateTime, interviewer: UserId) extends ProcessStepType
  final case object Offer extends ProcessStepType
}