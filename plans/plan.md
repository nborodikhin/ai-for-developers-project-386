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

## Phase 3: Backend (Kotlin + Spring Boot) ✅

### Step 0: Add `@tag` to TypeSpec routes

The current OpenAPI spec has `tags: []` and no per-operation tags — the generator would put everything in `DefaultApi`. Fix by adding `@tag` to each interface in `api/routes.tsp`:

```typespec
@tag("EventTypes")        interface EventTypes        { ... }
@tag("EventTypeBookings") interface EventTypeBookings { ... }
@tag("AvailableSlots")    interface AvailableSlots    { ... }
@tag("Bookings")          interface Bookings          { ... }
```

Then run `make api-generate` + `make frontend-generate-types` to update the spec and regenerate frontend types.

### File Layout
```
backend/
  build.gradle.kts
  settings.gradle.kts
  gradle/wrapper/gradle-wrapper.properties + gradlew + gradlew.bat
  Dockerfile
  src/main/kotlin/com/calendar/
    Application.kt
    controllers/
      EventTypeController.kt      # implements EventTypesApi
      BookingController.kt        # implements BookingsApi + EventTypeBookingsApi
      SlotController.kt           # implements AvailableSlotsApi
      GlobalExceptionHandler.kt   # @RestControllerAdvice — maps ConflictException → 409
    db/
      Tables.kt                   # Exposed Table objects
      DatabaseFactory.kt          # @PostConstruct: Database.connect + SchemaUtils.create
    services/
      EventTypeService.kt
      BookingService.kt           # throws ConflictException on overlap
      SlotService.kt
  src/main/resources/
    application.properties
```

### `settings.gradle.kts`
```kotlin
rootProject.name = "calendar-backend"
```

### `build.gradle.kts` — key sections
```kotlin
plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    id("org.openapi.generator") version "7.4.0"
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    // System property allows Dockerfile to override path without code changes
    inputSpec.set(System.getProperty("openApiSpecPath",
        "$rootDir/../api/generated/@typespec/openapi3/openapi.yaml"))
    outputDir.set("${layout.buildDirectory.get()}/generate-resources/main")
    apiPackage.set("com.calendar.generated.api")
    modelPackage.set("com.calendar.generated.model")
    configOptions.set(mapOf(
        "interfaceOnly"        to "true",
        "useSpringBoot3"       to "true",
        "useTags"              to "true",
        "dateLibrary"          to "java8",
        "serializationLibrary" to "jackson"
    ))
    generateModelTests.set(false); generateApiTests.set(false)
    generateModelDocumentation.set(false); generateApiDocumentation.set(false)
}

sourceSets { main { kotlin {
    srcDir("${layout.buildDirectory.get()}/generate-resources/main/src/main/kotlin")
}}}

tasks.withType<KotlinCompile> {
    dependsOn("openApiGenerate")
    kotlinOptions { freeCompilerArgs += "-Xjsr305=strict"; jvmTarget = "17" }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")  // OffsetDateTime serialization
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    val exposedVersion = "0.49.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")
    // Required by generated code
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
}
```

### `application.properties`
```properties
server.port=8080
spring.datasource.url=jdbc:sqlite::memory:
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=UTC
# Disable Spring's DataSource/JPA autoconfiguration — Exposed manages DB directly
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

### DB Layer

**`db/Tables.kt`**
```kotlin
object EventTypes : Table("event_types") {
    val id              = long("id").autoIncrement()
    val name            = varchar("name", 255)
    val description     = text("description")
    val durationMinutes = integer("duration_minutes")
    val deleted         = bool("deleted").default(false)
    override val primaryKey = PrimaryKey(id)
}
object Bookings : Table("bookings") {
    val id            = long("id").autoIncrement()
    val eventTypeId   = long("event_type_id").references(EventTypes.id)
    val eventTypeName = varchar("event_type_name", 255)   // denormalized
    val guestName     = varchar("guest_name", 255)
    val guestEmail    = varchar("guest_email", 255)
    val comment       = text("comment").nullable()
    val startTime     = varchar("start_time", 50)         // ISO-8601 UTC string
    val endTime       = varchar("end_time", 50)
    override val primaryKey = PrimaryKey(id)
}
```

Times as ISO-8601 varchar: lexicographic sort = chronological order for UTC strings → SQL `<`/`>` comparisons work for conflict detection.

**`db/DatabaseFactory.kt`** — `@Component` with `@PostConstruct`:
```kotlin
Database.connect("jdbc:sqlite::memory:", "org.sqlite.JDBC")
transaction { SchemaUtils.create(EventTypes, Bookings) }
```
Single `Database.connect()` — no pool, Exposed reuses the same connection for in-memory SQLite.

### Services

**`EventTypeService`**
- `create(req)` → `EventTypes.insert { }` → return EventType
- `list()` → `EventTypes.select { deleted eq false }`
- `findById(id)` → select WHERE id AND NOT deleted, returns `null` if missing
- `update(id, req)` → `EventTypes.update(where = { id eq x AND NOT deleted })`, returns `null` if 0 rows
- `softDelete(id)` → UPDATE SET deleted=true, returns `false` if 0 rows

**`BookingService`**
- `create(eventTypeId, req)`:
  1. Load EventType → return `null` if not found
  2. `endTime = req.startTime + durationMinutes` (OffsetDateTime arithmetic)
  3. Conflict check (scoped to same eventTypeId):
     `SELECT COUNT WHERE eventTypeId=x AND startTime < newEnd AND endTime > newStart`
     → throw `ConflictException` if count > 0
  4. INSERT with denormalized `eventTypeName`; return Booking
- `list(email?)` → SELECT all or WHERE guestEmail=email
- `update(id, req)` → UPDATE only non-null fields (guestName, comment); returns `null` if not found
- `delete(id)` → DELETE; returns `false` if 0 rows

**`SlotService`**
- `getSlots(eventTypeId, date)`:
  1. Load EventType → return `null` if not found
  2. `dayStart = date@09:00Z`, `dayEnd = date@18:00Z`
  3. Load existing bookings for this eventType overlapping the day
  4. Walk cursor from dayStart to dayEnd in `durationMinutes` steps
  5. Each slot: `available = no existing booking overlaps [cursor, cursor+duration)`
  6. Stop when `cursor + durationMinutes > dayEnd`

### Controllers

Controllers implement generated interfaces — Spring picks up all `@RequestMapping` annotations from the interface, so **don't re-declare** them on overriding methods.

- `EventTypeController : EventTypesApi` → delegates to EventTypeService, returns 404 on null
- `BookingController : BookingsApi, EventTypeBookingsApi` → delegates to BookingService, relies on GlobalExceptionHandler for 409
- `SlotController : AvailableSlotsApi` → delegates to SlotService, returns 404 on null

**`GlobalExceptionHandler`** (`@RestControllerAdvice`):
```kotlin
@ExceptionHandler(ConflictException::class)
fun handleConflict(ex: ConflictException) =
    ResponseEntity.status(409).body(ErrorResponse(ex.message ?: "Conflict"))
```

### Implementation Sequence

1. Add `@tag` to `api/routes.tsp` → `make api-generate` → `make frontend-generate-types`
2. Create `backend/settings.gradle.kts`
3. Create `backend/build.gradle.kts`
4. Init Gradle wrapper: `cd backend && gradle wrapper --gradle-version 8.7`
5. Create `application.properties`, `Application.kt`
6. Create `db/Tables.kt`, `db/DatabaseFactory.kt`
7. Run `./gradlew openApiGenerate` → **inspect** `build/generate-resources/main/.../api/` for actual interface names
8. Create 3 service classes + `ConflictException`
9. Create 3 controllers + `GlobalExceptionHandler`
10. `make backend-build` — fix any compilation errors
11. `make backend-run` + smoke-test with curl (see Verify below)
12. Create `backend/Dockerfile`
13. Create `docker-compose.yml` at repo root
14. `make docker-up` — full end-to-end test

### Verify (local)
```bash
# Create event type
curl -s -X POST localhost:8080/api/event-types \
  -H 'Content-Type: application/json' \
  -d '{"name":"15-min call","description":"Quick sync","durationMinutes":15}'

# List slots
curl -s 'localhost:8080/api/event-types/1/available-slots?date=2026-04-10'

# Book a slot
curl -s -X POST localhost:8080/api/event-types/1/bookings \
  -H 'Content-Type: application/json' \
  -d '{"guestName":"Alice","guestEmail":"alice@example.com","startTime":"2026-04-10T09:00:00Z"}'

# Verify conflict → must return 409
curl -s -o /dev/null -w "%{http_code}" -X POST localhost:8080/api/event-types/1/bookings \
  -H 'Content-Type: application/json' \
  -d '{"guestName":"Bob","guestEmail":"bob@example.com","startTime":"2026-04-10T09:00:00Z"}'
```

---

## Phase 4: Docker ✅

### `backend/Dockerfile`
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace
COPY api/generated/@typespec/openapi3/openapi.yaml /api-spec/openapi.yaml
COPY backend/gradlew backend/gradlew.bat ./
COPY backend/gradle ./gradle
COPY backend/build.gradle.kts backend/settings.gradle.kts ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true
COPY backend/src ./src
RUN ./gradlew bootJar --no-daemon -DsopenApiSpecPath=/api-spec/openapi.yaml

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build context is repo root so both `api/` and `backend/` are accessible.

### `docker-compose.yml` (repo root)
```yaml
version: "3.9"
services:
  backend:
    build:
      context: .
      dockerfile: backend/Dockerfile
    restart: on-failure

  frontend:
    build:
      context: .
      dockerfile: frontend/Dockerfile
    ports:
      - "3000:80"
    depends_on:
      - backend
    restart: on-failure
```

`frontend/nginx.conf` already has `proxy_pass http://backend:8080` — service name matches.

### Verify
- `make docker-up` starts without errors
- `http://localhost:3000` — full app with real backend, no Prism

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
