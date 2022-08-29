package presentation.candidate

import cats.Show
import derevo.scalacheck.arbitrary
import eu.timepit.refined.types.all.NonEmptyString
import org.joda.time.DateTime
import org.scalacheck.{Arbitrary, Gen}
import presentation.candidate.readModel.Details
import eu.timepit.refined.scalacheck.all._
import io.estatico.newtype.ops.toCoercibleIdOps

object Common {
  implicit val dateShow: Show[DateTime] = Show.fromToString[DateTime]
  implicit val firstNameA: Arbitrary[FirstName] = implicitly[Arbitrary[NonEmptyString]].coerce[Arbitrary[FirstName]]
  implicit val lastNameA: Arbitrary[LastName] = implicitly[Arbitrary[NonEmptyString]].coerce[Arbitrary[LastName]]
  implicit val emailA: Arbitrary[Email] = Arbitrary(for {
    name <- Gen.alphaStr
    domain <- Gen.alphaStr
  } yield Email(EmailR.unsafeFrom(s"$name@$domain.com")))

  implicit val detailsA: Arbitrary[Details] = arbitrary.instance[Details]
}
