# Testing complex event sourced systems

Domain:

```scala
sealed trait CandidateEvent

case class CandidateCreatedEvent(email: Email) extends CandidateEvent

case class AppliedToHiringProcessEvent(
    hiringProcessId: HiringProcessId, 
    positionId: PositionId
    ) extends CandidateEvent

case class AssignHiringManagerToProcessEvent(
    hiringProcessId: HiringProcessId,
    assignee: UserId
) extends CandidateEvent

```

Naive approach


```scala
def random[T]: T = ???
def process(state: Option[Candidate], event: CandidateEvent): Option[Candidate] = ???
def process(event: CandidateEvent, events: CandidateEvent*): Option[Candidate] = {
    (event :: events.toList).foldLeft(Option.empty[Candidate])(process)
}

val hiringProcessId = random[HiringProcessId]

val candidateState = process(
    CandidateCreatedEvent(random[Email]),
    AppliedToHiringProcessEvent(hiringProcessId, random[PositionId]),
    AssignHiringManagerToProcessEvent(hiringProcessId, random[UserId]),
)

// do some assertion on state
```


Context driven approach

```scala
trait TestDSL[F[_]] {
    def createCandidate(email: Email): F[(CandidateId, CandidateDSL[F])]
}

trait CandidateDSL[F[_]] {
    def startHiringProcess(hiringProcessId: HiringProcessId,positionId: PositionId): F[(HiringProcessId, HiringProcessDSL[F])]
}

trait HiringProcessDSL[F[_]] {
    def assignAssignee(userId: UserId): F[Unit]
}

trait QueryContext[F[_]] {
    def getAssignees(candidateId: CandidateId, hpId: HiringProcessId): F[Set[UserId]]
}

def example[F[_] : Monad](ctx: TestDSL[F], queries: QueryContext[F]) = for {
    (cId, cDsl) <- ctx.createCandidate(random[Email])
    (hId, hpDsl) <- cDsl.startHiringProcess(random[HiringProcessId], random[PositionId])
    userId = random[UserId]
    _ <- hpDsl.assignAssignee(userId)
    assignees <- queries.getAssignees(cId, hId)
} yield assignees === Set(userId)

```

Extendable design

```scala
trait Generator[F[_]] {
  def apply[T: Arbitrary]: F[T]
}
object Generator {
    def apply[F[_]](implicit gen: Generator[F]): Generator[F] = gen
}
```


Randomness

```scala 
trait TestDSL[F[_]] {
    val random: Generator[F]
    def createCandidate(email: F[Email] = random[Email]): F[(CandidateId, CandidateDSL[F])]
}

trait CandidateDSL[F[_]] {
    val random: Generator[F]
    def startHiringProcess(
        hiringProcessId: F[HiringProcessId] = random[HiringProcessId],
        positionId: F[PositionId] = random[PositionId]
    ): F[(HiringProcessId, HiringProcessDSL[F])]
}

trait HiringProcessDSL[F[_]] {
    val random: Generator[F]
    def assignAssignee(userId: F[UserId] = random[UserId]): F[Unit]
}

def example[F[_] : Sync](ctx: TestDSL[F], queries: QueryContext[F]) = for {
    (cId, cDsl) <- ctx.createCandidate()
    (hId, hpDsl) <- cDsl.startHiringProcess()
    userId = Generator[F].random[UserId]
    _ <- hpDsl.assignAssignee(userId)
    assignees <- queries.getAssignees(cId, hId)
} yield assignees === Set(userId)

```