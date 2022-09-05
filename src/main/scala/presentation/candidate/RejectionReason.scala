package presentation.candidate

import derevo.cats.show
import derevo.derive
import eu.timepit.refined.types.all.NonEmptyString
import io.estatico.newtype.macros.newtype
import presentation.shared.UserId
import eu.timepit.refined.cats.refTypeShow

@derive(show)
sealed trait RejectionReason

object RejectionReason {
  @derive(show)
  @newtype case class RejectionDescription(description: NonEmptyString)

  final case class RejectedByUser(userId: UserId, description: RejectionDescription) extends RejectionReason

  final case object WithdrawnByCandidate extends RejectionReason
}