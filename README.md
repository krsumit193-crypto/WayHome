# WayHome

> **"Waitlist dekhi? Ek aur raasta hai."**

WayHome (code name **Railhop**) helps students and families find a way home when a direct
train ticket is unavailable. It searches multi-train routes (Jaipur → Koderma style),
checks transfer times, estimates fares and ranks the options.

**Stack:** Java 17 + Spring Boot 3 · PostgreSQL + Flyway · React (Vite) · Docker Compose

## Docs

- [Complete Development Plan](docs/DEVELOPMENT_PLAN.md) — vision, constraints, features, algorithms, API, roadmap, risks and definition of done.
- [Project Explained](docs/PROJECT_EXPLAINED.md) — walkthrough of the skeleton, layer by layer.

## Run it

```bash
docker compose up --build        # UI: http://localhost:5173   API: http://localhost:8080
```

Or locally:

```bash
docker compose up -d db
cd backend && mvn spring-boot:run
cd frontend && npm install && npm run dev
```

The repository ships with **fictional demo trains** (`V2__demo_data.sql`, numbers 90001–90007)
so the Jaipur → Koderma search works immediately. They are **not** a real timetable.

## Layout

```
backend/
  engine/    pure Java routing (no Spring): RaptorEngine, JourneyFactory, FareEstimator, JourneyRanker
  repo/      JDBC access to stations and timetable
  service/   expands the weekly timetable into dated trips, runs search + alternatives
  web/       REST controller, DTOs, error handling, CORS
  resources/db/migration/   Flyway: V1 schema, V2 demo data
frontend/    React UI (search form, ticket-style cards, client-side sorting)
tools/       import_timetable.py — CSV to Flyway SQL
data/        sample CSVs showing the import format
docs/        development plan + project walkthrough
```

## API

| Method | Path | Notes |
|---|---|---|
| GET | `/api/stations?q=jai` | autocomplete, min 2 chars |
| POST | `/api/routes/search` | `{from, to, date, travelClass, maxChanges, sort}` |
| GET | `/api/health` | liveness |

```json
{ "from": "JP", "to": "KQR", "date": "2026-12-25", "travelClass": "SL", "maxChanges": 2, "sort": "FASTEST" }
```

`travelClass`: ANY, SL, AC3, AC2, GEN · `sort`: FASTEST, CHEAPEST, FEWEST_CHANGES, SAFEST

## How the route engine works

1. `TimetableService` expands each train into dated trips (start day −1 … +3 around the
   journey date), so overnight trains and multi-day journeys work. Times are minutes since
   midnight of the journey date.
2. `RaptorEngine` runs rounds. Round `k` = best arrival using exactly `k` trains, so
   `max changes = rounds − 1`. A transfer needs
   `next departure >= arrival + min-transfer-minutes` (default 60).
3. `RouteSearchService` bans each train of a found route once and searches again to surface
   alternatives.
4. `JourneyFactory` adds a fare estimate, transfer waits, risk (LOW / MEDIUM / HIGH) and an
   overnight-wait flag.

## Limits (be honest in the UI and in interviews)

- No live seat availability. **No IRCTC scraping.** Every leg links to the official site.
- Fares are estimates, never confirmed prices.
- Transfers only at the same station (no walking between stations).
- Demo data is fictional; a licensed real timetable still has to be imported.

## Roadmap

- [x] Phase A — skeleton: Spring Boot + React + Docker + CI + demo data
- [x] Phase 1–3 — station search, timetable import tool, route engine + tests, REST API + UI
- [ ] Phase B — real timetable, Plan B at transfers, ±3 day grid, nearby stations
- [ ] Phase C — split-ticket finder, WhatsApp share, booking-open reminder
- [ ] Phase D — real fare table, layover guide, Redis cache, deploy
- [ ] Phase E — availability ranking, delay score, stopover planner, bus + train hybrid

See the [full plan](docs/DEVELOPMENT_PLAN.md) for acceptance criteria and risks.
