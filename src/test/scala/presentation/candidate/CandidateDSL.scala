package presentation.candidate

import cats.implicits._
import cats.{Applicative, Monad}
import com.fortysevendeg.scalacheck.datetime.joda.ArbitraryJoda._
import derevo.scalacheck.arbitrary.instance
import org.joda.time.DateTime
import presentation.candidate.event.CandidateEvent.{AppliedToPositionEvent, NextStep}
import presentation.event.EventMetadata
import presentation.position.PositionId
import Common._
import presentation.candidate.TestEnv.EventEnv

trait CandidateDSL[F[_]] {
  val random: Generator[F]

  def id: F[CandidateId]
  def applyToPosition(hiringProcessId: F[HiringProcessId] = random[HiringProcessId],
                      positionId: F[PositionId] = random[PositionId],
                      startingStep: F[NextStep] = random[NextStep],
                      at: F[DateTime] = random[DateTime]): F[HiringProcessDSL[F]]
}

object CandidateDSL {
  def make[F[_]: Monad](candidateId: CandidateId, env: EventEnv[F]): CandidateDSL[F] = new CandidateDSLLive[F](candidateId, env)

  class CandidateDSLLive[F[_]: Monad](candidateId: CandidateId, env: EventEnv[F]) extends CandidateDSL[F] {
    override val random: Generator[F] = env.generator

    override val id: F[CandidateId] = Applicative[F].pure(candidateId)

    override def applyToPosition(hiringProcessId: F[HiringProcessId],
                                 positionId: F[PositionId],
                                 startingStep: F[NextStep],
                                 at: F[DateTime]): F[HiringProcessDSL[F]] = for {
      (hpId, pId, step, atF) <- Applicative[F].tuple4(hiringProcessId, positionId, startingStep, at)
      _ <- env.candidateStore.update(candidateId, AppliedToPositionEvent(hpId, pId, step), EventMetadata(atF))
    } yield HiringProcessDSL.make[F](candidateId, hpId, env)
  }
}