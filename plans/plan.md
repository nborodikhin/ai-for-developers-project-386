# Plan: Calendar Booking App (Mini-Calendly)

## Context
Educational Hexlet project. A calendar booking app where one owner creates event types and guests book time slots. No auth. The spec mandates API-first design with TypeSpec. Must be deployable via `docker compose up`.

## Repo Structure
```
/
├── api/                    # TypeSpec → OpenAPI spec
│   ├── package.json
│   ├── tspconfig.yaml
│   ├── main.tsp            # Entry point
│   ├── models.tsp          # Data models
│   ├── routes.tsp          # Endpoint definitions
│   └── generated/openapi.yaml
├── backend/                # Kotlin + Spring Boot + Exposed + SQLite
│   ├── build.gradle.kts
│   ├── Dockerfile
│   └── src/main/kotlin/com/calendar/...
├── frontend/               # React + TypeScript + Vite
│   ├── package.json
│   ├── vite.config.ts
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/...
├── spec/                   # Requirements + UI screenshots
└── docker-compose.yml
```

---

## Phase 1: TypeSpec API Definition

### Code generation notes
- TypeSpec compiles to OpenAPI YAML — that's the contract
- **Frontend**: auto-generate TypeScript types from OpenAPI via `openapi-typescript`
- **Backend (Spring Boot)**: auto-generate controller interfaces + data classes from OpenAPI via `openapi-generator` Gradle plugin. We only implement the business logic.
- Both backend and frontend reference `../api/generated/openapi.yaml` directly — no copying, single source of truth
- Build order: `api` first (generates OpenAPI), then backend/frontend (consume it)
- Docker: build context is repo root so all dirs are accessible

### Models
- **EventType**: `id: int64`, `name`, `description`, `durationMinutes: int32`
- **Booking**: `id: int64`, `eventTypeId: int64`, `eventTypeName`, `guestName`, `guestEmail`, `comment?`, `startTime: utcDateTime`, `endTime: utcDateTime`
- **AvailableSlot**: `startTime: utcDateTime`, `endTime: utcDateTime`, `available: boolean`
- **CreateEventTypeRequest**: `name`, `description`, `durationMinutes: int32`
- **CreateBookingRequest**: `guestName`, `guestEmail`, `comment?`, `startTime: utcDateTime`
- **UpdateBookingRequest**: `guestName?`, `comment?` (only these fields are editable)
- **ErrorResponse**: `message: string`

### Endpoints
| Method | Path | Request Body | Response Body | Status |
|--------|------|-------------|---------------|--------|
| POST | /api/event-types | CreateEventTypeRequest | EventType | 201 |
| GET | /api/event-types | — | EventType[] | 200 |
| GET | /api/event-types/{id} | — | EventType | 200/404 |
| PUT | /api/event-types/{id} | CreateEventTypeRequest | EventType | 200/404 |
| DELETE | /api/event-types/{id} | — | — | 204/404 |
| GET | /api/bookings | — | Booking[] | 200 |
| GET | /api/bookings?email={email} | — | Booking[] | 200 |
| POST | /api/event-types/{id}/bookings | CreateBookingRequest | Booking | 201/404/409 |
| PUT | /api/bookings/{id} | UpdateBookingRequest | Booking | 200/404 |
| DELETE | /api/bookings/{id} | — | — | 204/404 |
| GET | /api/event-types/{id}/available-slots?date= | — | AvailableSlot[] | 200/404 |

### Endpoint notes
- **POST /api/event-types** — owner creates a new event type (e.g., "Встреча 15 минут")
- **PUT /api/event-types/{id}** — id comes from URL path only. Request body has no id field. Returns 404 if not found.
- **DELETE /api/event-types/{id}** — soft delete: sets `deleted` flag, hides from catalog, existing bookings remain intact
- **GET /api/event-types** — returns only non-deleted event types
- **GET /api/bookings** — owner view: all upcoming bookings. With `?email=` query param: guest can look up their own bookings
- **PUT /api/bookings/{id}** — guest can update only `guestName` and `comment` (not time, not email)
- **DELETE /api/bookings/{id}** — guest cancels their booking

### Files
- `api/package.json`, `api/tspconfig.yaml`, `api/main.tsp`, `api/models.tsp`, `api/routes.tsp`

### Verify
- `cd api && npx tsp compile .` succeeds, `generated/openapi.yaml` has all endpoints

---

## Phase 2: Frontend (React + TypeScript + Vite)

Build UI first with hardcoded stub data, then wire up real API later.

### TypeScript types
- Auto-generated from OpenAPI spec via `openapi-typescript`

### Pages (from UI screenshots)
1. **HomePage** (`/`) — hero with gradient, "Записаться" CTA → `/book`
2. **BookCatalogPage** (`/book`) — host profile + event type cards grid
3. **BookEventPage** (`/book/:id`) — 3-panel: event info | calendar picker | time slots + booking form
4. **AdminPage** (`/admin`) — event type CRUD + upcoming bookings list

### Components
- Navbar ("Calendar" logo, "Записаться"/"Админка" links)
- EventTypeCard, CalendarPicker, SlotButton, BookingForm

### Stub API layer
- `src/api/` — functions that return hardcoded data initially, swapped for real fetch calls later

### Styling
- Mantine UI component library (Card, Button, Calendar, etc.) to match the mockup design

### Verify
- `cd frontend && npm run build` succeeds
- Pages render with stub data, navigation works

---

## Phase 3: Backend (Kotlin + Spring Boot)

### Auto-generated code (from OpenAPI via openapi-generator Gradle plugin)
- **Controller interfaces**: `EventTypeApi`, `BookingApi`, `SlotApi` — define all routes, params, status codes
- **Data classes**: `EventType`, `Booking`, `CreateEventTypeRequest`, etc.
- We implement the interfaces and write only business logic

### DB (Exposed, SQLite in-memory)
- Connection: `jdbc:sqlite::memory:` — no file, no volume, data resets on restart
- Tables created on startup via Exposed's `SchemaUtils.create()`
- **EventTypes**: id (autoincrement), name, description, durationMinutes, deleted (boolean, default false)
- **Bookings**: id (autoincrement), eventTypeId (FK), guestName, guestEmail, comment, startTime (ISO-8601), endTime (ISO-8601)

### Key Business Logic
- **Conflict check**: `WHERE startTime < :newEnd AND endTime > :newStart` across ALL bookings. 409 if any overlap.
- **Slot generation**: working hours 09:00–18:00 (hardcoded constant). Generate slots in `durationMinutes` increments. Check each against all existing bookings for availability.
- All times in UTC.

### File Layout
```
backend/
  build.gradle.kts          # openapi-generator plugin + Spring Boot deps
  src/main/kotlin/com/calendar/
    Application.kt           # @SpringBootApplication entry point
    controllers/
      EventTypeController.kt # implements generated EventTypeApi
      BookingController.kt   # implements generated BookingApi
      SlotController.kt      # implements generated SlotApi
    db/{DatabaseFactory,Tables}.kt
    services/{EventTypeService,BookingService,SlotService}.kt
  build/generate-resources/  # auto-generated interfaces + models (not committed)
```

### Verify
- `cd backend && ./gradlew build` compiles (codegen + compile)
- Endpoints return correct responses (manual curl or Spring test)

---

## Phase 4: Docker

### docker-compose.yml
- **backend**: builds `./backend`, exposes 8080 internally (no volume needed — in-memory DB)
- **frontend**: builds `./frontend`, maps port `3000:80`, depends on backend

### Nginx (frontend/nginx.conf)
- `location /` → static files with `try_files $uri /index.html` (SPA)
- `location /api/` → proxy to `http://backend:8080`

Single entry point: `http://localhost:3000`

### Verify
- `docker compose up --build` starts without errors
- App accessible at localhost:3000

---

## Phase 5: End-to-End Verification

1. `docker compose up --build` — both services start
2. Admin: create event types (15 min, 30 min)
3. Guest: browse catalog → select type → pick date → see slots → book
4. Confirm booked slot shows as busy
5. Book overlapping slot via different event type → 409 conflict
6. Guest: look up bookings by email, update name, cancel booking
7. Admin: see booking in upcoming list
8. Delete event type → disappears from catalog, bookings remain

---

## Key Design Decisions
- Working hours 09:00–18:00, hardcoded backend constant
- All times stored/transmitted in UTC, frontend converts to local
- No auth — admin page openly accessible
- SQLite in-memory — no persistence, data resets on restart (fine for demo)
- Nginx reverse proxy eliminates CORS issues
- Slots computed on-the-fly, not stored in DB
- Event type deletion is soft delete (bookings preserved)
