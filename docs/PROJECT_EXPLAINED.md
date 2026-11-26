# Railhop — Project Explained

Walkthrough of the skeleton in this repository.

## The whole flow in one line

User searches → React calls the API → backend turns the timetable into dated trips →
the engine finds routes → fare and risk are added → ticket-style cards are shown.

## Layer by layer

### 1. Database (PostgreSQL + Flyway)

Three tables: `station`, `train`, `train_stop`. Times are stored as minutes from midnight
of the train's origin day, so next-day 06:45 = `1440 + 405 = 1845`. Midnight crossing needs
no date math.

### 2. TimetableService

A train is a weekly schedule, but a search is for one date. The service turns each train
into five dated copies (journey date −1 … +3), so a train that left last night and passes
your station this morning is still found. `runs_on_mask` decides whether the train runs on
that weekday.

### 3. RaptorEngine (the actual brain)

The engine runs in rounds:

- Round 1: everywhere reachable with exactly one train.
- Round 2: take a second train from those stations. Rule: next departure ≥ previous
  arrival + 60 min (configurable).
- Round `k`: exactly `k` trains, so `max changes = rounds − 1`.

A station is updated only when the arrival is earlier than what was already found. That
pruning keeps the engine fast but means only the "best" paths survive.

Jaipur → Koderma demo:

| Round | Stations reached | Notes |
|---|---|---|
| Start | Jaipur | origin |
| After 1 train | New Delhi 13:30 (90001), Kota 11:30 (90003), Gaya 18:00 +1d (90006) | direct legs from Jaipur |
| After 2 trains | Koderma 06:45 +1d (90002 via New Delhi), Gaya 05:30 +1d (improved) | goal reached |

Kota → Gaya (90004) arriving 12:00 +1d is dropped because Gaya was already reached at
05:30 +1d.

### 4. RouteSearchService (alternatives)

Because pruning returns one route, this service bans each train of a found route once and
searches again. In the demo this yields three routes: via New Delhi, via the Gaya night
train, and via Kota–Gaya.

### 5. JourneyFactory

Each route gets a fare estimate (per-km model), transfer waits and risk:

- wait ≥ 150 min → LOW
- 75–149 min → MEDIUM
- under 75 min → HIGH
- wait over 6 hours → overnight flag

### 6. Controller + React

The controller maps everything to JSON (`HH:mm`, `+1d`, station names). React builds
ticket-style cards; sorting (Fastest / Cheapest / Fewest changes / Safest) happens in the
browser with no new request.

## Things to remember

- The engine contains no Spring code — the algorithm is separate and unit-testable.
- Demo data is fictional. Importing a real timetable is the first big task.
- Fares are estimates. There is no availability check.
