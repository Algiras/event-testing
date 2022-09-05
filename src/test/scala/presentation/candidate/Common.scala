package presentation.candidate

import cats.Show
import derevo.scalacheck.arbitrary
import eu.timepit.refined.types.all.NonEmptyString
import org.joda.time.DateTime
import org.scalacheck.{Arbitrary, Gen}
import presentation.candidate.readModel.Details
import eu.timepit.refined.scalacheck.all._
import io.estatico.newtype.ops.toCoercibleIdOps
import presentation.candidate.ProcessStepType.UrlR
import presentation.candidate.RejectionReason.RejectionDescription
import presentation.position.{PositionId, PositionStepId}
import presentation.shared.UserId

import java.util.UUID

object Common {
  implicit val dateShow: Show[DateTime] = Show.fromToString[DateTime]

  implicit val hiringProcessIdA: Arbitrary[HiringProcessId] = implicitly[Arbitrary[UUID]].coerce[Arbitrary[HiringProcessId]]
  implicit val candidateId: Arbitrary[CandidateId] = implicitly[Arbitrary[UUID]].coerce[Arbitrary[CandidateId]]
  implicit val positionIdA: Arbitrary[PositionId] = implicitly[Arbitrary[UUID]].coerce[Arbitrary[PositionId]]
  implicit val stepInstanceIdA: Arbitrary[StepInstanceId] = implicitly[Arbitrary[UUID]].coerce[Arbitrary[StepInstanceId]]
  implicit val positionStepIdA: Arbitrary[PositionStepId] = implicitly[Arbitrary[UUID]].coerce[Arbitrary[PositionStepId]]
  implicit val userIdA: Arbitrary[UserId] = implicitly[Arbitrary[UUID]].coerce[Arbitrary[UserId]]
  implicit val rejectionDescriptionA: Arbitrary[RejectionDescription] = {
    Arbitrary(
      Gen.alphaNumStr.filter(_.nonEmpty).map(from => RejectionDescription(NonEmptyString.unsafeFrom(from)))
    )
  }
  implicit val urlA: Arbitrary[UrlR] = Arbitrary(for {
    protocol <- Gen.oneOf("http", "https")
    subdomain <- Gen.alphaNumStr.filter(_.nonEmpty)
    host <- Gen.alphaNumStr.filter(_.nonEmpty)
    ending <- Gen.alphaStr.filter(_.nonEmpty)
  } yield UrlR.unsafeFrom(s"$protocol://$subdomain.$host.$ending"))

  implicit val firstNameA: Arbitrary[FirstName] = implicitly[Arbitrary[NonEmptyString]].coerce[Arbitrary[FirstName]]
  implicit val lastNameA: Arbitrary[LastName] = implicitly[Arbitrary[NonEmptyString]].coerce[Arbitrary[LastName]]
  implicit val emailA: Arbitrary[Email] = Arbitrary(for {
    name <- Gen.alphaStr.filter(_.nonEmpty)
    domain <- Gen.alphaStr.filter(_.nonEmpty)
  } yield Email(EmailR.unsafeFrom(s"$name@$domain.com")))

  implicit val detailsA: Arbitrary[Details] = arbitrary.instance[Details]
}
