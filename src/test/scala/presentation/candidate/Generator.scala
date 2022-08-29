package presentation.candidate

import cats.effect.Sync
import cats.effect.std.Random
import org.scalacheck.Gen.Parameters
import org.scalacheck.{Arbitrary, Prop}
import cats.implicits._
import weaver.Log

trait Generator[F[_]] {
  def apply[T: Arbitrary]: F[T]
}

object Generator {
  def apply[F[_]: Sync](log: Log[F], seed: Option[Long] = None): F[Generator[F]] = for {
    seedLong <- seed match {
      case Some(value) => Sync[F].pure(value)
      case None => Random.scalaUtilRandom[F].flatMap(_.nextLong)
    }
    _ <- log.debug(s"Used SEED: $seedLong")
    seedRandom <- Random.scalaUtilRandomSeedLong(seedLong)
  } yield new  Generator[F] {
    def apply[T: Arbitrary]: F[T] = seedRandom.nextLong.flatMap(seed => Sync[F].blocking {
      val (p, s) = Prop.startSeed(Parameters.default.withInitialSeed(seed))
      Arbitrary.arbitrary[T].pureApply(p, s)
    })
  }
}
