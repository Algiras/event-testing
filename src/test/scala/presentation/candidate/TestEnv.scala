package presentation.candidate

import presentation.candidate.event.CandidateEvent

case class TestEnv[F[_]](candidateStore: EventStore[F, CandidateId, CandidateEvent],
                         generator: Generator[F],
                         queries: TestQueryDSL[F]) extends HasCandidateStore[F] with HasGenerator[F] with HasTestQueryDSL[F]

object TestEnv {
  type EventEnv[F[_]] = HasCandidateStore[F] with HasGenerator[F]
}

trait HasCandidateStore[F[_]] {
  def candidateStore: EventStore[F, CandidateId, CandidateEvent]
}
