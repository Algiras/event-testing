package presentation

import cats.Show
import derevo.cats.show
import derevo.derive
import io.estatico.newtype.macros.newtype
import org.joda.time.DateTime
import java.util.UUID

package object shared {
  implicit val dateTimeShow: Show[DateTime] = Show.fromToString[DateTime]

  @derive(show)
  @newtype case class UserId(id: UUID)
}
