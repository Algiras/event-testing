package presentation.candidate.event

import presentation.candidate._
import presentation.event.{CreateEvent, Event, UpdateEvent}
import presentation.position.{PositionId, PositionStepId}

sealed trait CandidateEvent extends Event

object CandidateEvent {
    final case class CandidateCreatedEvent(firstName: FirstName, lastName: LastName, email: Email) extends CandidateEvent with CreateEvent

    final case class NextStep(stepInstanceId: StepInstanceId, positionStepId: PositionStepId, stepType: ProcessStepType)

    final case class AppliedToPositionEvent(hiringProcessId: HiringProcessId, positionId: PositionId, startingStep: NextStep) extends CandidateEvent with UpdateEvent

    sealed trait ProgressStepEvent extends CandidateEvent with UpdateEvent {
        def hiringProcessId: HiringProcessId
    }

    final case class StepCompletedEvent(hiringProcessId: HiringProcessId, nextStep: NextStep) extends ProgressStepEvent with UpdateEvent
    final case class CandidateRejectedEvent(hiringProcessId: HiringProcessId, reason: RejectionReason) extends ProgressStepEvent with UpdateEvent
    final case class CandidateHiredEvent(hiringProcessId: HiringProcessId) extends ProgressStepEvent with UpdateEvent
}
