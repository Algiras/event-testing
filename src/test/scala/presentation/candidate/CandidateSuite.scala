package presentation.candidate

import cats.data.Kleisli
import cats.effect.IO
import com.fortysevendeg.scalacheck.datetime.joda.ArbitraryJoda._
import derevo.scalacheck.arbitrary.instance
import org.joda.time.DateTime
import org.scalacheck.Arbitrary
import org.specs2.matcher.MatchResult
import presentation.candidate.Common._
import presentation.candidate.event.CandidateEvent
import presentation.candidate.event.CandidateEvent.NextStep
import presentation.candidate.readModel.{ActiveHiringProcess, Candidate, Details, HiredHiringProcess}
import weaver.scalacheck._
import weaver.specs2compat.IOMatchers
import weaver.{Log, SimpleIOSuite}

object CandidateSuite extends SimpleIOSuite with IOMatchers with Checkers {
  implicit def anyF[T](value: T): IO[T] = IO.pure(value)

  private def random[T: Arbitrary]: Kleisli[IO, HasGenerator[IO], T] = HasGenerator.random[IO, T]

  private def test[A](name: String)(testCase: Kleisli[F, TestEnv[IO], MatchResult[A]]): Unit = loggedTest(name) { log: Log[IO] =>
    for {
      generator <- Generator[IO](log)
      store <- EventStore.inMemory[IO, CandidateId, CandidateEvent](generator)
      queries = TestQueryDSL.make[F](store)
      env = TestEnv[IO](store, generator, queries)
      res <- testCase.run(env)
    } yield toExpectations(res)
  }

  test("candidate gets created") {
    for {
      details <- random[Details]
      date <- random[DateTime]
      cId <- TestDSL.make[IO].flatMapF(
        _.createCandidate(details.firstName, details.lastName, details.email, date).flatMap(_.id)
      )
      candidate <- TestQueryDSL.getCandidate[F](cId)
    } yield candidate must beSome(Candidate(details, Map.empty, date))
  }

  test("new hiring process is started for candidate") {
    for {
      setHpId <- random[HiringProcessId]
      (cId, hId) <- TestDSL.make[IO].flatMapF(_.createCandidate().flatMap(dsl => for {
        cId <- dsl.id
        hpId <- dsl.applyToPosition(setHpId).flatMap(_.id)
      } yield (cId, hpId)))
      candidate <- TestQueryDSL.getCandidate[F](cId)
    } yield {
      val process = candidate.flatMap(_.processes.get(hId)) .collect {
        case hp: ActiveHiringProcess => hp
      }
      process must beSome
    }
  }

  test("hiring process can be hired") {
    for {
      hiredAt <- random[DateTime]
      step <- random[NextStep].map(a => a.copy(stepType = ProcessStepType.Offer))
      (cId, hId) <- TestDSL.make[IO].flatMapF(_.createCandidate().flatMap(cDsl => for {
        cId <- cDsl.id
        hpId <- cDsl.applyToPosition(startingStep = step).flatMap(hDsl => for {
          id <- hDsl.id
          _ <- hDsl.hired(hiredAt)
        } yield id)
      } yield (cId, hpId)))
      candidate <- TestQueryDSL.getCandidate[F](cId)
    } yield {
      val process = candidate.flatMap(_.processes.get(hId)).collect {
        case HiredHiringProcess(_, _, _, hiredAt) => hiredAt
      }
      process must beSome(hiredAt)
    }
  }
}
