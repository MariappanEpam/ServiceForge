# C4 As-Is — 07 Runtime

Scope: Whole repository
Status: populated
Updated: 2026-10-14

## Local runtime

### Backend
- Default bind: `127.0.0.1:8080` (confirmed)
	- Evidence: backend/src/main/resources/application.yml

### Frontend
- Angular dev server runs on `http://localhost:4200` (confirmed)
	- Evidence: README.md

### Proxying behavior
- Frontend proxies `/api` to backend `http://127.0.0.1:8080` (confirmed)
	- Evidence: frontend/proxy.conf.json

## Startup commands

- Backend:
	- `cd backend` then `mvn spring-boot:run` (confirmed)
		- Evidence: README.md
- Frontend:
	- `cd frontend` then `npm install` then `npm start` (confirmed)
		- Evidence: README.md

## Provenance
- README.md
- backend/src/main/resources/application.yml
- frontend/README.md
- frontend/proxy.conf.json
