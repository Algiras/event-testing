package presentation.candidate

import cats.implicits._
import cats.{Applicative, Monad}
import Common._
import com.fortysevendeg.scalacheck.datetime.joda.ArbitraryJoda._
import derevo.scalacheck.arbitrary.instance
import org.joda.time.DateTime
import presentation.candidate.TestEnv.EventEnv
import presentation.candidate.event.CandidateEvent.{CandidateHiredEvent, CandidateRejectedEvent, NextStep, StepCompletedEvent}
import presentation.event.EventMetadata

trait HiringProcessDSL[F[_]] {
  val random: Generator[F]

  def id: F[HiringProcessId]

  def hired(at: F[DateTime] = random[DateTime]): F[Unit]
  def reject(reason: F[RejectionReason] = random[RejectionReason], at: F[DateTime] = random[DateTime]): F[Unit]
  def completeStep(nextStep: F[NextStep] = random[NextStep], at: F[DateTime] = random[DateTime]): F[StepInstanceId]
}

object HiringProcessDSL {
  def make[F[_]: Monad](candidateId: CandidateId, hpId: HiringProcessId, testEnv: EventEnv[F]): HiringProcessDSL[F] = {
    new HiringProcessDSLLive[F](candidateId, hpId, testEnv)
  }

  class HiringProcessDSLLive[F[_]: Monad](candidateId: CandidateId,
                                          hiringProcessId: HiringProcessId,
                                          env: EventEnv[F]) extends HiringProcessDSL[F] {
    val random: Generator[F] = env.generator
    override val id: F[HiringProcessId] = Applicative[F].pure(hiringProcessId)

    override def hired(at: F[DateTime]): F[Unit] =  for {
      date <- at
      _ <- env.candidateStore.update(candidateId, CandidateHiredEvent(hiringProcessId), EventMetadata(date))
    } yield ()

    override def reject(reason: F[RejectionReason], at: F[DateTime]): F[Unit] = for {
      (reasonF, atF) <- Applicative[F].tuple2(reason, at)
      _ <- env.candidateStore.update(candidateId, CandidateRejectedEvent(hiringProcessId, reasonF), EventMetadata(atF))
    } yield ()

    override def completeStep(nextStep: F[NextStep], at: F[DateTime]): F[StepInstanceId] = for {
      (nextStepF, atF) <- Applicative[F].tuple2(nextStep, at)
      _ <- env.candidateStore.update(candidateId, StepCompletedEvent(hiringProcessId, nextStepF), EventMetadata(atF))
    } yield nextStepF.stepInstanceId
  }
}