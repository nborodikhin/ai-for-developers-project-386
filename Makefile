.PHONY: init clean api-init api-generate frontend-init frontend-dev frontend-build backend-build backend-run docker-up docker-down

# === Full project ===

init: api-init frontend-init ## Initialize all dependencies
	@echo "All dependencies installed"

clean: ## Remove generated files and dependencies
	rm -rf api/node_modules api/generated
	rm -rf frontend/node_modules frontend/dist
	rm -rf backend/build backend/.gradle

docker-up: ## Build and start everything in Docker
	docker compose up --build

docker-down: ## Stop Docker containers
	docker compose down

# === API (TypeSpec) ===

api-init: ## Install TypeSpec dependencies
	cd api && npm install

api-generate: ## Compile TypeSpec → OpenAPI YAML
	cd api && npx tsp compile . --output-dir generated

# === Frontend ===

frontend-init: ## Install frontend dependencies
	cd frontend && npm install

frontend-dev: ## Run frontend dev server (localhost:5173)
	cd frontend && npm run dev

frontend-build: ## Build frontend for production
	cd frontend && npm run build

# === Backend ===

backend-build: ## Build backend (codegen + compile)
	cd backend && ./gradlew build

backend-run: ## Run backend locally (localhost:8080)
	cd backend && ./gradlew bootRun

# === Help ===

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
