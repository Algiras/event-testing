package presentation.candidate

import cats.effect.IO
import com.fortysevendeg.scalacheck.datetime.joda.ArbitraryJoda._
import org.joda.time.DateTime
import presentation.candidate.Common._
import presentation.candidate.readModel.{Candidate, Details}
import weaver.{Log, SimpleIOSuite}
import weaver.scalacheck._
import weaver.specs2compat.IOMatchers

object CandidateSuite extends SimpleIOSuite with IOMatchers with Checkers {
  loggedTest("create candidate on CreateEvent") { log: Log[IO] =>
      for {
        random <- Generator[IO](log)
        dsl <- CandidateDSL.impl[IO](random)
        details <- random[Details]
        date <- random[DateTime]
        id <- dsl.createCandidate(details.firstName, details.lastName, details.email, date)
        candidate <- dsl.getCandidate(id)
      } yield candidate must beSome(Candidate(details, Map.empty, date))
  }
}
