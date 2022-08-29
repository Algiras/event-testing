package presentation

import io.estatico.newtype.macros.newtype
import java.util.UUID

package object shared {
  @newtype case class UserId(id: UUID)
}
