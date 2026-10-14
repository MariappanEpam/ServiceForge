# C4 As-Is — 03 Containers

Scope: Whole repository (ServiceForge-Start)
Status: partial
Updated: 2026-10-14

## Containers
- Angular SPA (frontend)
  - Evidence: frontend/angular.json
  - Evidence: frontend/src/main.ts
- Spring Boot API (backend)
  - Evidence: backend/pom.xml
  - Evidence: backend/src/main/java/com/serviceforge/ServiceForgeApplication.java
- MCP servers (tools)
  - Evidence: tools/mcp-server/README.md
  - Evidence: tools/openproject-mcp/README.md

## Container responsibilities

- Angular SPA
  - Renders UI routes for availability, parts reservations, parts management, technician management (confirmed)
    - Evidence: frontend/src/app/app.routes.ts
  - Calls backend via `/api` (confirmed)
    - Evidence: frontend/src/app/services/*.ts
    - Evidence: frontend/proxy.conf.json

- Spring Boot API
  - Exposes REST endpoints under `/api/*` for technicians, jobs, parts, reservations (confirmed)
    - Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
    - Evidence: backend/src/main/java/com/serviceforge/controller/JobController.java
    - Evidence: backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
    - Evidence: backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
    - Evidence: backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
  - Persists data to SQLite via JDBC + Flyway (confirmed)
    - Evidence: backend/src/main/resources/application.yml
    - Evidence: backend/src/main/resources/db/migration/V1__init.sql

- MCP servers
  - ServiceForge MCP server: exposes tool wrappers around backend API (confirmed)
    - Evidence: tools/mcp-server/openapi.json
  - OpenProject MCP server: exposes tool wrappers around OpenProject API (confirmed)
    - Evidence: tools/openproject-mcp/README.md

## Provenance
- frontend/angular.json
- frontend/src/main.ts
- backend/pom.xml
- tools/mcp-server/README.md
- tools/openproject-mcp/README.md
- frontend/src/app/app.routes.ts
- frontend/src/app/services/jobs-api.service.ts
- frontend/src/app/services/technicians-api.service.ts
- frontend/src/app/services/parts-api.service.ts
- frontend/src/app/services/parts-reservations-api.service.ts
- frontend/proxy.conf.json
- backend/src/main/java/com/serviceforge/ServiceForgeApplication.java
- backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- backend/src/main/java/com/serviceforge/controller/JobController.java
- backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
- backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
- backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
- backend/src/main/resources/application.yml
- backend/src/main/resources/db/migration/V1__init.sql
- tools/mcp-server/openapi.json
