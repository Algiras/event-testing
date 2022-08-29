package presentation

import eu.timepit.refined.types.all.NonEmptyString
import io.estatico.newtype.macros.newtype

import java.util.UUID

package object position {
  @newtype case class PositionId(id: UUID)
  @newtype case class PositionName(name: NonEmptyString)
  @newtype case class PositionStepName(name: NonEmptyString)
  @newtype case class PositionStepId(id: UUID)
}
