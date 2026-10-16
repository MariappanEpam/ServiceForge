# Technology context — ServiceForge (system overview)

Scope: Whole repository (ServiceForge-Start)
Status: partial
Updated: 2026-10-14

## Detected stacks

### Backend
- Java 17 + Spring Boot 3 (confirmed)
  - Evidence: backend/pom.xml
- REST API + Actuator (confirmed)
  - Evidence: backend/pom.xml

### Persistence
- SQLite via JDBC + Flyway migrations (confirmed)
  - Evidence: backend/pom.xml
  - Evidence: backend/src/main/resources/application.yml

### Frontend
- Angular (confirmed)
  - Evidence: frontend/package.json
  - Evidence: frontend/angular.json

### Tooling / automation
- Node.js tooling for MCP servers and local tracker automation (confirmed)
  - Evidence: package.json
  - Evidence: tools/mcp-server/README.md
  - Evidence: tools/openproject-mcp/README.md

## Confirmed runtime configuration

### Backend bind + port
- Binds to 127.0.0.1:8080 (confirmed)
  - Evidence: backend/src/main/resources/application.yml (server.port, server.address)

### SQLite database path
- Uses SQLite JDBC URL with env override `SERVICEFORGE_SQLITE_PATH` and default `.data/serviceforge.db` (confirmed)
  - Evidence: backend/src/main/resources/application.yml (spring.datasource.url)

### Flyway migrations
- Flyway enabled; migrations loaded from `classpath:db/migration` (confirmed)
  - Evidence: backend/src/main/resources/application.yml (spring.flyway.*)

### Seed mode
- Seed mode controlled by `SERVICEFORGE_DB_SEED` with default `baseline` (confirmed)
  - Evidence: backend/src/main/resources/application.yml (serviceforge.db.seed)

### Frontend → backend proxy
- Angular dev server proxies `/api` to `http://127.0.0.1:8080` (confirmed)
  - Evidence: frontend/proxy.conf.json

## Repository shape
- backend/: Spring Boot service
- frontend/: Angular SPA
- tools/: local tracker + MCP servers + utilities
- pipeline/: orchestration + specs + decisions + test artifacts + repo-understanding

Status: confirmed

## Components and adapters
- Backend ↔ Frontend: HTTP via Angular proxy (confirmed)
  - Evidence: frontend/proxy.conf.json
- MCP servers:
  - ServiceForge MCP server (confirmed)
    - Evidence: tools/mcp-server/README.md
  - OpenProject MCP server (confirmed)
    - Evidence: tools/openproject-mcp/README.md

## Unknowns
- Exact runtime ports and environment overrides beyond what is in application.yml and proxy.conf.json
  - Status: unknown

## Provenance
- backend/pom.xml
- backend/src/main/resources/application.yml
- frontend/package.json
- frontend/angular.json
- frontend/proxy.conf.json
- tools/mcp-server/README.md
- tools/openproject-mcp/README.md
- package.json
