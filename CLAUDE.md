# Project Instructions

## Plans
Keep all plans in the project's `plans/` directory (e.g., `plans/plan.md`), not in the global `~/.claude/plans/`.

## Build & Package Management
Always use Makefile targets for npm install, build, type generation, and other maintenance tasks. Never run `npm install`, `npm run build`, `npx`, etc. directly — use the corresponding `make` target instead (e.g., `make frontend-init`, `make frontend-build`, `make frontend-generate-types`).
