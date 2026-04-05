.PHONY: init clean api-init api-generate \
        frontend-init frontend-generate-types frontend-lint frontend-dev frontend-dev-stop frontend-build \
        backend-build backend-run \
        prism prism-stop stop \
        docker-up docker-down

PRISM_PID_FILE := .prism.pid
DEV_PID_FILE   := .dev.pid

# === Full project ===

init: api-generate frontend-generate-types ## Initialize all dependencies and generate types
	@echo "All dependencies installed and types generated"

clean: ## Remove generated files and dependencies
	rm -rf api/node_modules api/generated
	rm -rf frontend/node_modules frontend/dist frontend/src/generated
	rm -rf backend/build backend/.gradle

docker-up: ## Build and start everything in Docker
	docker compose up --build

docker-down: ## Stop Docker containers
	docker compose down

# === API (TypeSpec) ===

api-init: ## Install TypeSpec dependencies
	cd api && npm install

api-generate: api-init ## Compile TypeSpec → OpenAPI YAML
	cd api && npx tsp compile . --output-dir generated

# === Frontend ===

frontend-init: ## Install frontend dependencies
	cd frontend && npm install

frontend-generate-types: frontend-init ## Generate TypeScript types from OpenAPI spec
	cd frontend && npm run generate-types

frontend-lint: ## Type-check frontend (no emit)
	cd frontend && npx tsc --noEmit

frontend-dev: ## Start frontend dev server in background (HMR, no restart needed for code changes)
	cd frontend && npm run dev & echo $$! > ../$(DEV_PID_FILE)

frontend-dev-stop: ## Stop frontend dev server
	@if [ -f $(DEV_PID_FILE) ]; then \
	  kill $$(cat $(DEV_PID_FILE)) 2>/dev/null && echo "Dev server stopped"; \
	  rm -f $(DEV_PID_FILE); \
	else \
	  echo "No dev server PID file found"; \
	fi

frontend-build: frontend-generate-types ## Build frontend for production
	cd frontend && npm run build

# === Backend ===

backend-build: ## Build backend (codegen + compile)
	cd backend && ./gradlew build

backend-run: ## Run backend locally (localhost:8080)
	cd backend && ./gradlew bootRun

# === Dev services ===

prism: ## Start Prism mock server on port 4010 in background
	npx @stoplight/prism-cli mock api/generated/@typespec/openapi3/openapi.yaml --port 4010 & echo $$! > $(PRISM_PID_FILE)

prism-stop: ## Stop Prism mock server
	@if [ -f $(PRISM_PID_FILE) ]; then \
	  kill $$(cat $(PRISM_PID_FILE)) 2>/dev/null && echo "Prism stopped"; \
	  rm -f $(PRISM_PID_FILE); \
	else \
	  echo "No Prism PID file found"; \
	fi

stop: prism-stop frontend-dev-stop ## Stop all local dev processes (Prism + frontend + backend)
	pkill -f "gradlew bootRun" || true
	pkill -f "spring-boot" || true

# === Help ===

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-28s\033[0m %s\n", $$1, $$2}'
