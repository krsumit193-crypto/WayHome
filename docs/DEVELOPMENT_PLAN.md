# Railhop — Complete Development Plan

> **Pitch:** "Waitlist dekhi? Ek aur raasta hai."
> Find a way home when direct train tickets are unavailable.
>
> This document is the working roadmap for the code in this repository.
> Stack: Spring Boot + PostgreSQL + React.

---

## 1. Vision

Railhop is not a generic booking app. It helps a passenger who cannot get a direct
ticket (festival rush, waitlist) by offering:

- **Connecting routes** — e.g. Jaipur → Delhi → Koderma multi-train combinations.
- **Split tickets** — same train, but two tickets broken at an intermediate station.
- **Smart flexibility** — nearby stations, nearby dates, and a "Plan B" if a train is late.

Target user: students and families going home.

## 2. Honest constraints (state these up front)

- Free/public APIs cannot guarantee live seat availability. **v1 does not check availability.**
  For every segment, the user opens the official IRCTC link themselves.
- **No IRCTC scraping.** Only official links and authorised data.
- Fares are **estimates**, always labelled "estimated, not a booking price".
- Break of journey (stopover) and split ticketing are different things:
  - Break of journey = one ticket, stop and continue (rules: ticket > 500 km, one break up to
    1000 km, two breaks beyond that, max 2 days per break, station-master endorsement).
  - It does **not** solve availability. Re-verify rules with an official Indian Railways source
    before launch.
- Booking window and boarding-point rules change. Keep them in **config**, never hardcode.

## 3. Feature list and priority

| # | Feature | Why | Data | Phase |
|---|---|---|---|---|
| 1 | Connecting routes (RAPTOR) | Core | Timetable | Done |
| 2 | Alternatives (ban-and-research) | More options | Timetable | Done |
| 3 | Fare estimate + risk score | Decision help | Timetable | Done |
| 4 | Split-ticket finder | Most unique feature | Timetable | C |
| 5 | Plan B at every transfer | Reduces missed-connection fear | Timetable | B |
| 6 | Flexible-date grid (±3 days) | Useful in festival rush | Timetable | B |
| 7 | Nearby stations | More routes | Station lat/lng | B |
| 8 | Booking-open reminder | Practical for students | Config | C |
| 9 | WhatsApp share | Easy, real use | None | C |
| 10 | Layover guide | Helpful for long waits | Static JSON | D |
| 11 | Stopover planner (break of journey) | For ticket holders | Timetable + rules | E |
| 12 | Delay-based confidence score | Better risk | Delay history | E |
| 13 | Live availability ranking | Best ranking | Authorised API | E |
| 14 | Bus + train hybrid | More coverage | Bus data | E |

Ground rules:

- The `engine/` package contains **no Spring imports**. This keeps it unit-testable and the
  design clean.
- Timetable is cached in memory (loaded on first search after startup).
- Every new feature is its own service class; the controller only maps JSON.

## 4. Architecture

`React → Spring Boot API → search services (RouteSearch, SplitFinder, PlanB, FlexDates)
→ pure-Java engine → PostgreSQL (Redis cache later).`

## 5. Data model

Existing:

- `station(code, name, lat, lng)`
- `train(number, name, runs_on_mask)`
- `train_stop(train_number, seq, station_code, arrival_min, departure_min, distance_km)`

Times are minutes from midnight of the train's origin day. Next-day 06:45 = `1440 + 405 = 1845`.

Additions:

- `fare_table(class, from_km, to_km, base, per_km)` — real fare slabs.
- `station_info(code, retiring_room, cloakroom, food, notes)` — layover guide.
- `saved_search(id, from, to, date, class, created_at)` — reminders.

Config keys: `railhop.advance-booking-days`, `railhop.min-transfer-minutes`,
`railhop.nearby-radius-km`.

## 6. Engine and algorithms

### 6.1 Connecting routes (done)

RAPTOR rounds: round `k` = exactly `k` trains. Transfer rule: next departure ≥ previous
arrival + minimum transfer. A station is updated only when arrival is better (pruning).

### 6.2 Alternatives (done)

Pruning leaves only the best path, so each train of a found route is banned once and the
search is rerun. Duplicate routes are removed.

### 6.3 Split-ticket finder (new)

Idea: for a train passing origin `O` and destination `D`, try every intermediate stop `X`
as a split: `(O→X, X→D)`.

1. Take each trip instance that day where `O` comes before `D`.
2. Create a candidate for every intermediate stop `X`.
3. Estimate each segment's fare, total = sum; set `seatChange = true`.
4. Rank by less total time, lower fare, more "hub-like" split points (heuristic; validate).
5. Output a separate official IRCTC link per segment plus: "Seat change may be needed at X."

Variation: try split points on each leg of a connecting route too (2 trains + 1 split).
Complexity is trains × stops. When availability data arrives, filter by confirmed seats.

### 6.4 Plan B at every transfer (new)

At each interchange `X`, arrival `t`, show the next 2 departures towards the destination
with `departure ≥ t + min transfer`. UI: "Agar train late ho gayi: next option 17:40 (wait 2h 10m)."

### 6.5 Flexible dates (new)

Run the same search for date−3 … date+3 (parallel streams). Return fastest route and route
count per day. Cache results so repeat searches are fast.

### 6.6 Nearby stations (new)

Support multiple origins/destinations: add stations within `nearby-radius-km` using
Haversine distance. UI shows an "Alternate station: +30 km" badge.

### 6.7 Booking-open reminder (new)

`bookingOpenDate = journeyDate − advanceBookingDays` (config). Show a countdown; later add
email/WhatsApp reminders. Verify the current advance-booking period on the official site.

### 6.8 Stopover planner (later)

Compute ticket distance, check break eligibility from rules, warn that a break without
endorsement is invalid. This is a planner only — it does not improve availability.

## 7. API

Search request: `{from, to, date, travelClass, maxChanges, sort, includeNearby}`.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/stations?q=` | Autocomplete |
| POST | `/api/routes/search` | Connecting routes + alternatives |
| POST | `/api/routes/splits` | Split-ticket candidates |
| POST | `/api/routes/flex` | ±3 day grid |
| GET | `/api/stations/{code}/info` | Layover guide |
| POST | `/api/reminders` | Booking-open reminder |
| GET | `/api/health` | Liveness |

## 8. Frontend screens

- Search: from/to autocomplete + swap, date, class, max changes, "include nearby" toggle.
- Results: ticket-style cards, sort chips, badges ("Split at X", "Alternate station",
  "Tight transfer"), expandable Plan B per transfer.
- Flexible dates: 7-day grid; click a day to search it.
- Share: WhatsApp text link (`wa.me`) with the full route summary.
- Reminder: "Booking opens on <date>" panel.
- Mobile-first.

## 9. Roadmap

| Phase | When | Deliverable | Done when |
|---|---|---|---|
| A: Skeleton | Done | Spring Boot + React + Docker + CI + demo data | `docker compose up` runs Jaipur → Koderma demo |
| B: Real data + Plan B + dates + nearby | Week 1–2 | Real timetable import, Plan B, flex grid, nearby | 5 real journeys match official timetable |
| C: Split tickets + share + reminder | Week 3 | SplitFinder, UI badges, WhatsApp share, booking-open panel | Split candidates correct on demo and real data |
| D: Polish + deploy | Week 4 | Real fare table, layover guide, Redis, Testcontainers, deploy | Public URL + README + demo video |
| E: Advanced | Later | Availability ranking, delay score, stopover planner, bus hybrid | Data source available |

Phase B detail:

1. Run `mvn verify`, fix small compile errors (Spring side).
2. Find a licensed dataset, import with `tools/import_timetable.py`.
3. Manually verify 5–10 known journeys against official timetables.
4. Build Plan B, flex grid and nearby stations, each with unit tests.

Phase C detail:

1. `SplitFinder` (pure Java) + tests.
2. `/api/routes/splits` + UI badge.
3. WhatsApp share and booking-open panel.

Phase D detail:

1. Real fare slabs.
2. Redis cache (`spring-boot-starter-data-redis`), index trips by station.
3. Deploy (Render/Railway), env vars, health check.

## 10. Testing

- Unit: `RaptorEngine` (direct, 1 change, short-transfer reject, max changes, midnight),
  `SplitFinder`, `PlanB`, `FlexDates`.
- Integration: Testcontainers + PostgreSQL, Flyway migrations, API tests.
- Golden journeys: 5–10 real journeys, manually verified.
- Frontend: build check in CI + manual mobile pass.

## 11. DevOps

Docker Compose (db, backend, frontend), GitHub Actions CI (`mvn verify`,
`npm ci && npm build`). Config via env vars (`DB_URL`, `CORS_ORIGINS`, ...).
Deploy backend + Postgres on Render/Railway, frontend on a static host, or all via Docker.

## 12. Risks

| Risk | Impact | Response |
|---|---|---|
| No real data | Demo only | Hand-verify a small corridor (Jaipur–Delhi–Gaya–Koderma) |
| No availability data | Ranking not confirmed | v1 links, v2 authorised API |
| Rules change | Wrong guidance | Config + official-source note in UI |
| Scope creep | Nothing complete | Follow phase order, UI fancy later |
| Wrong fare | Trust lost | Always label "estimate" |

## 13. Resume / interview angle

- "Implemented transit routing (RAPTOR) as a framework-free, testable Java module."
- "Split-ticket finder and alternatives ranking: real problem, real constraints."
- Demo script: Jaipur → Koderma search → 3 alternatives → Plan B → flex grid →
  split suggestion → WhatsApp share.
- State the limits yourself (no live availability, estimates only).

## 14. Definition of done (MVP)

- Real corridor timetable imported, 5+ journeys verified.
- Connecting routes, alternatives, Plan B, flex grid and split suggestions all work.
- Fares labelled as estimates, an official link for every leg, disclaimer always visible.
- CI green, README with architecture + limits, public demo URL.
