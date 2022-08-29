package presentation.position

final case class Position(name: PositionName, steps: Map[PositionStepId, Step])

final case class Step(name: PositionStepName, stepType: StepType)
