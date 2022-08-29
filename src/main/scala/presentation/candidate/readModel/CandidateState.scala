package presentation.candidate.readModel

import cats.data.NonEmptyMap
import presentation.candidate.event.CandidateEvent
import presentation.event.EventMetadata

import scala.collection.immutable.SortedMap

object CandidateState {
  case object InvalidState

  def apply(currentState: Option[Candidate], events: List[(CandidateEvent, EventMetadata)]): Option[Candidate] = events.foldLeft(currentState) {
    case (state, (event, metadata)) => from(state, event, metadata).toOption
  }

  def from(state: Option[Candidate], event: CandidateEvent, metadata: EventMetadata): Either[InvalidState.type, Candidate] = state match {
    case Some(existingState) => event match {
      case CandidateEvent.CandidateCreatedEvent(firstName, lastName, email) =>
        Right(existingState.copy(details = Details(firstName, lastName, email)))

      case CandidateEvent.AppliedToPositionEvent(hiringProcessId, positionId, step) =>
        Right(existingState.copy(
          processes = existingState.processes.updated(hiringProcessId, ActiveHiringProcess(
            positionId, activeStepId = step.stepInstanceId, activeStep = ActiveProcessStep(step.positionStepId, step.stepType, metadata.at), steps = Map.empty, startedAt = metadata.at)
          )
        ))
      case event: CandidateEvent.ProgressStepEvent => event match {
        case CandidateEvent.StepCompletedEvent(hiringProcessId, nextStep) => existingState.processes.get(hiringProcessId) match {
          case Some(hp) => hp match {
            case ActiveHiringProcess(positionId, activeStepId, activeStep, steps, startedAt) =>
              Right(existingState.copy(processes = existingState.processes + (hiringProcessId -> ActiveHiringProcess(
                positionId,
                nextStep.stepInstanceId,
                ActiveProcessStep(nextStep.positionStepId, nextStep.stepType, metadata.at),
                steps + (activeStepId -> SuccessfulProcessStep(
                  activeStep.stepId,
                  activeStep.stepType,
                  activeStep.startedAt,
                  metadata.at
                )), startedAt
              ))))
            case _: HiredHiringProcess => Left(InvalidState)
            case _: RejectedHiringProcess => Left(InvalidState)
          }
          case None => Left(InvalidState)
        }
        case CandidateEvent.CandidateRejectedEvent(hiringProcessId, reason) => existingState.processes.get(hiringProcessId) match {
          case Some(hp) => hp match {
            case ActiveHiringProcess(positionId, activeStepId, activeStep, steps, startedAt) => Right(
              existingState.copy(processes = existingState.processes + (hiringProcessId ->
                RejectedHiringProcess(positionId, activeStepId, RejectedProcessStep(
                  activeStep.stepId,
                  activeStep.stepType,
                  reason,
                  activeStep.startedAt
                ), steps, startedAt, metadata.at)
                )))
            case _: RejectedHiringProcess => Left(InvalidState)
            case _: HiredHiringProcess => Left(InvalidState)
          }
          case None => Left(InvalidState)
        }
        case CandidateEvent.CandidateHiredEvent(hiringProcessId) => existingState.processes.get(hiringProcessId) match {
          case Some(hp) => hp match {
            case ActiveHiringProcess(positionId, activeStepId, activeStep, steps, startedAt) => Right(existingState.copy(processes = existingState.processes + (hiringProcessId -> HiredHiringProcess(
              positionId, steps = NonEmptyMap(
                (activeStepId, SuccessfulProcessStep(activeStep.stepId, activeStep.stepType, activeStep.startedAt, metadata.at)),
                SortedMap.from(steps)), startedAt, metadata.at
            ))))
            case _: HiredHiringProcess => Left(InvalidState)
            case _: RejectedHiringProcess => Left(InvalidState)
          }
          case None => Left(InvalidState)
        }
      }
    }
    case None => event match {
      case CandidateEvent.CandidateCreatedEvent(firstName, lastName, email) => Right(Candidate(Details(firstName, lastName, email), Map.empty, metadata.at))
      case _ => Left(InvalidState)
    }
  }
}
