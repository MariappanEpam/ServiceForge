# C4 As-Is — 02 System context

Scope: Whole repository (ServiceForge-Start)
Status: partial
Updated: 2026-10-14

## System context diagram (text)

- Users interact with Angular SPA served by Angular dev server (confirmed)
	- Evidence: README.md (frontend runs on http://localhost:4200)
- Angular SPA calls backend REST API via `/api` proxy (confirmed)
	- Evidence: frontend/proxy.conf.json
	- Evidence: frontend/src/app/services/jobs-api.service.ts (calls /api/...)
	- Evidence: frontend/src/app/services/technicians-api.service.ts (calls /api/...)
- Backend is Spring Boot REST API on 127.0.0.1:8080 (confirmed)
	- Evidence: backend/src/main/resources/application.yml
	- Evidence: backend/src/main/java/com/serviceforge/ServiceForgeApplication.java

Optional tooling context:
- ServiceForge MCP server consumes backend OpenAPI and exposes tools (confirmed)
	- Evidence: backend/pom.xml (springdoc OpenAPI dependency)
	- Evidence: tools/mcp-server/openapi.json
- OpenProject MCP server integrates with OpenProject (confirmed)
	- Evidence: tools/openproject-mcp/README.md

## External systems

- OpenProject (via MCP tooling) (confirmed)
	- Evidence: tools/openproject-mcp/README.md

## Provenance
- frontend/proxy.conf.json
- backend/pom.xml
- tools/openproject-mcp/README.md
- README.md
- backend/src/main/resources/application.yml
- backend/src/main/java/com/serviceforge/ServiceForgeApplication.java
- frontend/src/app/services/jobs-api.service.ts
- frontend/src/app/services/technicians-api.service.ts
- tools/mcp-server/openapi.json
