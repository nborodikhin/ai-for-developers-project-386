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

## Phase 1: TypeSpec API Definition ✅

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
- `make api-generate` succeeds, `api/generated/@typespec/openapi3/openapi.yaml` has all endpoints

---

## Phase 2: Frontend (React + TypeScript + Vite) ✅

Use real API from the start via Prism mock server (reads openapi.yaml, returns realistic responses). No hand-written stubs.

### Directory structure
```
frontend/
├── package.json, tsconfig.json, vite.config.ts, index.html
├── Dockerfile, nginx.conf, .gitignore
└── src/
    ├── main.tsx                   # MantineProvider + RouterProvider
    ├── App.tsx                    # Route definitions
    ├── api/
    │   ├── types.ts               # Re-exports from generated/ with readable aliases
    │   ├── client.ts              # apiFetch<T> wrapper (throws ApiError on non-2xx)
    │   ├── eventTypes.ts          # listEventTypes, getEventType, createEventType, etc.
    │   ├── bookings.ts            # listBookings, createBooking, updateBooking, deleteBooking
    │   ├── slots.ts               # getAvailableSlots(eventTypeId, date)
    │   └── index.ts               # Re-exports from all api modules
    ├── components/
    │   ├── Navbar.tsx             # AppShell.Header + "Записаться"/"Админка" links
    │   ├── EventTypeCard.tsx      # Card with name, description, duration Badge
    │   ├── CalendarPicker.tsx     # @mantine/dates Calendar, minDate=today
    │   ├── SlotList.tsx           # Slots with "Свободен"/"Занят" badges + confirm button
    │   ├── BookingForm.tsx        # Name, email, comment inputs + submit
    │   └── EventInfoPanel.tsx     # Left panel: event name, host, duration, selected date/time
    ├── pages/
    │   ├── HomePage.tsx           # Gradient hero + features card
    │   ├── BookCatalogPage.tsx    # Host profile + SimpleGrid of EventTypeCards
    │   ├── BookEventPage.tsx      # 3-column Grid layout (most complex)
    │   └── AdminPage.tsx          # Tabs: event type CRUD + upcoming bookings table
    ├── hooks/
    │   ├── useEventTypes.ts       # { data, loading, error } pattern
    │   ├── useEventType.ts
    │   ├── useAvailableSlots.ts   # refetches when (eventTypeId, date) changes
    │   └── useBookings.ts
    └── generated/
        └── api.ts                 # openapi-typescript output (gitignored, generated at build time)
```

### API during development — Prism mock server
No hand-written stubs. Instead, use [Prism](https://stoplight.io/open-source/prism) — an OpenAPI mock server that auto-generates realistic responses from `openapi.yaml`.

```
make prism          # starts Prism on port 4010
make frontend-dev   # Vite proxies /api → localhost:4010
```

When backend is ready, `make frontend-dev` proxies `/api` → `localhost:8080` instead — no code changes needed.

### TypeScript types
`src/api/types.ts` aliases `components['schemas']['X']` from the generated file — isolates the rest of the codebase from generated naming conventions.
Generated via: `npm run generate-types` → `openapi-typescript ../api/generated/@typespec/openapi3/openapi.yaml -o src/generated/api.ts`

### Key Mantine components used
- **HomePage**: `Box` (gradient bg), `Grid`, `Title`, `Button`, `Card`, `List`
- **BookCatalogPage**: `Avatar`, `SimpleGrid`, `EventTypeCard` with `Badge` for duration
- **BookEventPage**: `Grid` 3-col, `@mantine/dates Calendar`, `ScrollArea`, `Badge` (green/red), `TextInput`, `Textarea`
- **AdminPage**: `Tabs`, `Table`, `Modal`, `NumberInput`

### BookEventPage state machine
```
selectedDate → fetch slots → selectedSlot (free only) → show BookingForm → submit → success notification
```
State: `{ selectedDate, slots, selectedSlot, loadingSlots }` — all local `useState`.

### Docker (multi-stage, repo-root context)
```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY api/generated/.../openapi.yaml /api-spec/openapi.yaml
COPY frontend/package*.json ./
RUN npm ci && npx openapi-typescript /api-spec/openapi.yaml -o src/generated/api.ts
COPY frontend/ .
RUN npm run build

FROM nginx:1.25-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
```
`docker-compose.yml` sets `context: .` (repo root), `dockerfile: frontend/Dockerfile`.

### Nginx
- `location /api/` → `proxy_pass http://backend:8080` (preserves `/api/` prefix)
- `location /` → `try_files $uri $uri/ /index.html` (SPA routing)

### New Makefile targets
- `frontend-generate-types` — run `npm run generate-types` in frontend/
- `frontend-lint` — `tsc --noEmit`
- `prism` — start Prism mock server on port 4010 from `api/generated/.../openapi.yaml`
- `prism-stop` — stop the Prism mock server process
- `stop` — stop all local dev processes (Prism + backend Spring Boot)
- Update `init` to depend on `api-generate frontend-generate-types`
- `frontend-dev` proxies `/api` to `localhost:4010` (Prism) during development; switch to `localhost:8080` once backend is ready

### Verify
- `make frontend-build` succeeds, zero TypeScript errors
- `make frontend-dev` (with `make prism` running): all 4 pages render correctly with Prism-generated mock data
- `/book/:id` — date selection fetches slots, free slot click enables form, submit shows success notification
- `/admin` — event type table and bookings table render
- `docker compose build frontend` succeeds, `curl localhost:3000` returns index.html

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
