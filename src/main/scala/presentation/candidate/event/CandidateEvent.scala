package presentation.candidate.event

import presentation.candidate._
import presentation.event.Event
import presentation.position.{PositionId, PositionStepId}

sealed trait CandidateEvent extends Event

object CandidateEvent {
    final case class CandidateCreatedEvent(firstName: FirstName, lastName: LastName, email: Email) extends CandidateEvent

    final case class NextStep(stepInstanceId: StepInstanceId, positionStepId: PositionStepId, stepType: ProcessStepType)

    final case class AppliedToPositionEvent(hiringProcessId: HiringProcessId, positionId: PositionId, startingStep: NextStep) extends CandidateEvent

    sealed trait ProgressStepEvent extends CandidateEvent {
        def hiringProcessId: HiringProcessId
    }

    final case class StepCompletedEvent(hiringProcessId: HiringProcessId, nextStep: NextStep) extends ProgressStepEvent
    final case class CandidateRejectedEvent(hiringProcessId: HiringProcessId, reason: RejectionReason) extends ProgressStepEvent
    final case class CandidateHiredEvent(hiringProcessId: HiringProcessId) extends ProgressStepEvent
}
