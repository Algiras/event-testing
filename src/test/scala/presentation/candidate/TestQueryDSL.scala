package presentation.candidate

import cats.Monad
import cats.data.Kleisli
import cats.implicits._
import presentation.candidate.event.CandidateEvent
import presentation.candidate.readModel.{Candidate, CandidateState}

trait TestQueryDSL[F[_]] {
  def getCandidate(candidateId: CandidateId): F[Option[Candidate]]
}

trait HasTestQueryDSL[F[_]] {
  def queries: TestQueryDSL[F]
}

object HasTestQueryDSL {
  def apply[F[_]](from: TestQueryDSL[F]): HasTestQueryDSL[F] = new HasTestQueryDSL[F] {
    override def queries: TestQueryDSL[F] = from
  }
}

object TestQueryDSL {
  def make[F[_]: Monad](store: EventStore[F, CandidateId, CandidateEvent]): TestQueryDSL[F] = new TestQueryDSLLive(store)

    class TestQueryDSLLive[F[_]: Monad](candidateStore: EventStore[F, CandidateId, CandidateEvent]) extends TestQueryDSL[F] {
      override def getCandidate(candidateId: CandidateId): F[Option[Candidate]] = candidateStore.get(candidateId)
      .map(_.flatMap(events => CandidateState(None, events.toList)))
  }

  def getCandidate[F[_]: Monad](candidateId: CandidateId): Kleisli[F, HasTestQueryDSL[F], Option[Candidate]] = {
    Kleisli.ask[F, HasTestQueryDSL[F]].andThen(_.queries.getCandidate(candidateId))
  }
}
