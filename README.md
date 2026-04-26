### Hexlet tests and linter status:
[![Actions Status](https://github.com/nborodikhin/ai-for-developers-project-386/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/nborodikhin/ai-for-developers-project-386/actions)
[![CI](https://github.com/nborodikhin/ai-for-developers-project-386/actions/workflows/ci.yml/badge.svg)](https://github.com/nborodikhin/ai-for-developers-project-386/actions/workflows/ci.yml)

**Live app:** https://calendar-app-x1qn.onrender.com

---

## Calendar Booking App

A mini-Calendly: the owner creates event types (e.g. "30-minute call"), guests browse the catalog, pick a date and available slot, and book it.

## Tech stack

| Layer | Tech |
|-------|------|
| API contract | TypeSpec → OpenAPI YAML |
| Backend | Kotlin, Spring Boot 3, Exposed ORM, SQLite (in-memory) |
| Frontend | React, TypeScript, Vite, Mantine UI |
| Proxy | nginx (serves static files, proxies `/api/` to Spring Boot) |
| Deploy | Single Docker container on Render |

## Running locally

```sh
make docker-up       # build images and start at http://localhost:3000
make docker-down     # stop
```

## Development

```sh
make init                    # install dependencies and generate types
make backend-run             # start backend on localhost:8082
make frontend-dev            # start Vite dev server with HMR
make prism                   # start Prism mock server on port 4010
make stop                    # stop all local dev processes
```

## Tests

```sh
make backend-test            # JUnit 5 unit + integration tests
make frontend-test           # Vitest + React Testing Library + MSW
make frontend-e2e            # Playwright E2E tests (requires make docker-up first)
```

## Architecture

The production Docker image is a single container: nginx listens on `$PORT`, serves the React SPA as static files, and reverse-proxies `/api/` to Spring Boot running on `$((PORT + 1000))` internally. SQLite runs in-memory — data resets on restart.
