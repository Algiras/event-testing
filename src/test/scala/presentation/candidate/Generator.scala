package presentation.candidate

import cats.Monad
import cats.data.Kleisli
import cats.effect.Sync
import cats.effect.std.Random
import cats.implicits._
import org.scalacheck.Gen.Parameters
import org.scalacheck.{Arbitrary, Prop}
import weaver.Log

trait Generator[F[_]] {
  def apply[T: Arbitrary]: F[T]
}

trait HasGenerator[F[_]] {
  def generator: Generator[F]
}

object HasGenerator {
  def apply[F[_]](gen: Generator[F]): HasGenerator[F] = new HasGenerator[F] {
    val generator: Generator[F] = gen
  }

  def random[F[_]: Monad, A: Arbitrary]: Kleisli[F, HasGenerator[F], A] = Kleisli.ask[F, HasGenerator[F]]
    .flatMapF(_.generator.apply[A])
}

object Generator {
  def apply[F[_]: Sync](log: Log[F], seed: Option[Long] = None): F[Generator[F]] = for {
    seedLong <- seed match {
      case Some(value) => Sync[F].pure(value)
      case None => Random.scalaUtilRandom[F].flatMap(_.nextLong)
    }
    _ <- log.debug(s"SEED: $seedLong")
    seedRandom <- Random.scalaUtilRandomSeedLong(seedLong)
  } yield new  Generator[F] {
    def apply[T: Arbitrary]: F[T] = seedRandom.nextLong.flatMap(seed => Sync[F].blocking {
      val (p, s) = Prop.startSeed(Parameters.default.withInitialSeed(seed))
      Arbitrary.arbitrary[T].pureApply(p, s)
    })
  }
}
