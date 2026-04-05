.PHONY: init clean api-init api-generate \
        frontend-init frontend-generate-types frontend-lint frontend-dev frontend-dev-stop frontend-build \
        backend-generate backend-build backend-run backend-run-stop \
        prism prism-stop stop \
        docker-up docker-down

PRISM_PID_FILE   := .prism.pid
DEV_PID_FILE     := .dev.pid
BACKEND_PID_FILE := .backend.pid

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

backend-generate: ## Run openapi-generator only (produces interfaces under backend/build/)
	cd backend && ./gradlew openApiGenerate

backend-build: ## Build backend (codegen + compile + test)
	cd backend && ./gradlew build

backend-run: ## Start backend in background (localhost:8082, avoids conflict with syncthing on 8080)
	cd backend && ./gradlew bootRun --args='--server.port=8082' & echo $$! > ../$(BACKEND_PID_FILE)

backend-run-stop: ## Stop background backend process
	@if [ -f $(BACKEND_PID_FILE) ]; then \
	  kill $$(cat $(BACKEND_PID_FILE)) 2>/dev/null && echo "Backend stopped"; \
	  rm -f $(BACKEND_PID_FILE); \
	else \
	  echo "No backend PID file found"; \
	fi

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

stop: prism-stop frontend-dev-stop backend-run-stop ## Stop all local dev processes

# === Help ===

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-28s\033[0m %s\n", $$1, $$2}'
