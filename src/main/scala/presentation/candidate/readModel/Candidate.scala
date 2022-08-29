package presentation.candidate.readModel

import cats.data.NonEmptyMap
import com.github.nscala_time.time.Imports.DateTime
import derevo.cats.show
import derevo.derive
import presentation.candidate._
import presentation.position.{PositionId, PositionStepId}

final case class Candidate(details: Details, processes: Map[HiringProcessId, HiringProcess], createdAt: DateTime)

@derive(show)
case class Details(firstName: FirstName, lastName: LastName, email: Email)

sealed trait HiringProcess {
  def positionId: PositionId
  def startedAt: DateTime
}

final case class ActiveHiringProcess(positionId: PositionId,
                                     activeStepId: StepInstanceId,
                                     activeStep: ActiveProcessStep,
                                     steps: Map[StepInstanceId, SuccessfulProcessStep],
                                     startedAt: DateTime) extends HiringProcess

final case class HiredHiringProcess(positionId: PositionId,
                                    steps: NonEmptyMap[StepInstanceId, SuccessfulProcessStep],
                                    startedAt: DateTime,
                                    hiredAt: DateTime) extends HiringProcess

final case class RejectedHiringProcess(positionId: PositionId,
                                       rejectionStepId: StepInstanceId,
                                       step: RejectedProcessStep,
                                       steps: Map[StepInstanceId, SuccessfulProcessStep],
                                       startedAt: DateTime,
                                       rejectedAt: DateTime) extends HiringProcess

sealed trait ProcessStep {
  def stepId: PositionStepId

  def stepType: ProcessStepType

  def startedAt: DateTime
}

final case class ActiveProcessStep(stepId: PositionStepId,
                                   stepType: ProcessStepType,
                                   startedAt: DateTime) extends ProcessStep

sealed trait CompletedProcessStep extends ProcessStep {
  def startedAt: DateTime
}

final case class SuccessfulProcessStep(stepId: PositionStepId,
                                       stepType: ProcessStepType,
                                       startedAt: DateTime,
                                       completedAt: DateTime) extends CompletedProcessStep

final case class RejectedProcessStep(stepId: PositionStepId,
                                     stepType: ProcessStepType,
                                     reason: RejectionReason,
                                     startedAt: DateTime) extends CompletedProcessStep
