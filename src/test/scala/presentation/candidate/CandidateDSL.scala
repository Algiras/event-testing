package presentation.candidate

import cats.MonadThrow
import cats.effect.kernel.Ref
import cats.implicits._
import org.joda.time.DateTime
import presentation.candidate.event.CandidateEvent.CandidateCreatedEvent
import presentation.candidate.readModel.{Candidate, CandidateState}
import presentation.event.EventMetadata
import java.util.UUID

trait CandidateQueryDSL[F[_]] {
  def getCandidate(candidateId: CandidateId): F[Option[Candidate]]
  def getCandidates: F[Map[CandidateId, Candidate]]
}

trait CandidateDSL[F[_]] {
  def createCandidate(firstName: FirstName, lastName: LastName, email: Email, at: DateTime): F[CandidateId]
}

object CandidateDSL {
  sealed trait TestError extends Throwable

  case object InvalidState extends TestError

  case class TestState(candidates: Map[CandidateId, Candidate])

  object TestState {
    val empty: TestState = TestState(Map.empty)
  }

  def impl[F[_]: MonadThrow: Ref.Make](random: Generator[F]): F[CandidateDSL[F] with CandidateQueryDSL[F]] = for {
    state <- Ref.of[F, TestState](TestState.empty)
  } yield new CandidateDSL[F] with CandidateQueryDSL[F] {
    override def createCandidate(firstName: FirstName, lastName: LastName, email: Email, at: DateTime): F[CandidateId] = {
      for {
        id <- random[UUID].map(CandidateId.apply)
        candidate <- MonadThrow[F].fromOption(
          CandidateState(None, List((CandidateCreatedEvent(firstName, lastName, email), EventMetadata.SystemEventMetadata(at)))),
          InvalidState
        )
        _ <- state.update(state => state.copy(candidates = state.candidates + (id -> candidate)))
      } yield id
    }

    override def getCandidate(candidateId: CandidateId): F[Option[Candidate]] = state.get.map(_.candidates.get(candidateId))

    override def getCandidates: F[Map[CandidateId, Candidate]] = state.get.map(_.candidates)
  }
}