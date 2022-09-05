package presentation.candidate

import cats.MonadThrow
import cats.data.NonEmptyList
import cats.effect.kernel.Ref
import cats.implicits._
import org.scalacheck.Arbitrary
import presentation.event.{CreateEvent, Event, EventMetadata, UpdateEvent}

trait EventStore[F[_], Id, E <: Event] {
  def create[CE <: E with CreateEvent](event: CE, metadata: EventMetadata): F[Id]

  def update[UE <: E with UpdateEvent](id: Id, event: UE, metadata: EventMetadata): F[Unit]

  def get(id: Id): F[Option[NonEmptyList[(E, EventMetadata)]]]
}

object EventStore {
  def inMemory[F[_]: MonadThrow: Ref.Make, K: Arbitrary, E <: Event](random: Generator[F]): F[EventStore[F, K, E]] = for {
    ref <- Ref[F].of(Map.empty[K, NonEmptyList[(E, EventMetadata)]])
  } yield new InMemoryEventStore[F, K, E](random, ref)

  class InMemoryEventStore[F[_]: MonadThrow, K: Arbitrary, E <: Event](random: Generator[F],
                                                                       ref: Ref[F, Map[K, NonEmptyList[(E, EventMetadata)]]]) extends EventStore[F, K, E] {
    override def create[CE <: E with CreateEvent](event: CE, metadata: EventMetadata): F[K] = for {
      key <- random[K]
      _ <- ref.update(store => store + (key -> NonEmptyList.of((event, metadata))))
    } yield key

    override def update[UE <: E with UpdateEvent](id: K, event: UE, metadata: EventMetadata): F[Unit] = for {
      optRecord <- ref.get.map(_.get(id))
      recorded <- MonadThrow[F].fromOption(optRecord, new RuntimeException("Aggregate does not exists"))
      _ <- ref.update(store => store + (id -> ((event, metadata) :: recorded)))
    } yield ()

    override def get(id: K): F[Option[NonEmptyList[(E, EventMetadata)]]] = ref.get.map(_.get(id).map(_.reverse))
  }
}