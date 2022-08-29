package presentation.position

sealed trait StepType

object StepType {
  final case object CVReview extends StepType
  final case object Interview extends StepType
  final case object Offer extends StepType
}

