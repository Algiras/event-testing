package presentation

import cats.Order
import derevo.cats._
import derevo.derive
import eu.timepit.refined._
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.predicates.all.MatchesRegex
import eu.timepit.refined.types.all.NonEmptyString
import io.estatico.newtype.macros.newtype
import eu.timepit.refined.cats.refTypeShow

import java.util.UUID

package object candidate {
  @newtype case class CandidateId(id: UUID)

  @derive(show)
  @newtype case class FirstName(firstName: NonEmptyString)

  @derive(show)
  @newtype case class LastName(lastName: NonEmptyString)

  type EmailR = String Refined MatchesRegex[W.`"""[A-z0-9]+@[A-z0-9]+\\.[A-z0-9]{2,}"""`.T]

  @derive(show)
  object EmailR extends RefinedTypeOps[EmailR, String]

  @derive(show)
  @newtype case class Email(email: EmailR)

  @newtype case class HiringProcessId(id: UUID)

  @newtype case class StepInstanceId(id: UUID)

  object StepInstanceId {
    implicit val ordering: Ordering[StepInstanceId] = (x: StepInstanceId, y: StepInstanceId) => Ordering[UUID].compare(x.id, y.id)
    implicit val order: Order[StepInstanceId] = Order.fromOrdering(ordering)
  }
}
