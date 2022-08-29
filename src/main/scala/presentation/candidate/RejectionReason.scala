package presentation.candidate

import eu.timepit.refined.types.all.NonEmptyString
import io.estatico.newtype.macros.newtype
import presentation.shared.UserId

sealed trait RejectionReason

object RejectionReason {
  @newtype case class RejectionDescription(description: NonEmptyString)

  final case class RejectedByUser(userId: UserId, description: RejectionDescription) extends RejectionReason

  final case object WithdrawnByCandidate extends RejectionReason
}