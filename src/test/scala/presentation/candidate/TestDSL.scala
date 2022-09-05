package presentation.candidate

import cats.data.Kleisli
import cats.implicits._
import cats.{Applicative, MonadThrow}
import com.fortysevendeg.scalacheck.datetime.joda.ArbitraryJoda._
import org.joda.time.DateTime
import presentation.candidate.Common._
import presentation.candidate.TestEnv.EventEnv
import presentation.candidate.event.CandidateEvent.CandidateCreatedEvent
import presentation.event.EventMetadata

trait TestDSL[F[_]] {
  val random: Generator[F]

  def createCandidate(firstName: F[FirstName] = random[FirstName],
                      lastName: F[LastName] = random[LastName],
                      email: F[Email] = random[Email],
                      at: F[DateTime] = random[DateTime]): F[CandidateDSL[F]]
}

object TestDSL {
  def make[F[_]: MonadThrow]: Kleisli[F, EventEnv[F], TestDSL[F]] = Kleisli.ask[F, EventEnv[F]].map(new TestDSLLive[F](_))

  class TestDSLLive[F[_]: MonadThrow](env: EventEnv[F]) extends TestDSL[F] {
    override val random: Generator[F] = env.generator

    override def createCandidate(firstName: F[FirstName], lastName: F[LastName], email: F[Email], at: F[DateTime]): F[CandidateDSL[F]] = {
      for {
        (fName, lName, emailF, atF) <- Applicative[F].tuple4(firstName, lastName, email, at)
        id <- env.candidateStore.create(CandidateCreatedEvent(fName, lName, emailF), EventMetadata(atF))
      } yield CandidateDSL.make[F](id, env)
    }
  }
}