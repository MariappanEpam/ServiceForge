# Dependency map — ServiceForge (system overview)

Scope: Whole repository (ServiceForge-Start)
Status: partial
Updated: 2026-10-14

## Maven (Java)
- Manifest: backend/pom.xml
- Status: confirmed
- Notes: Spring Boot 3.x, Java 17, includes web/validation/actuator/openapi/jdbc/flyway/sqlite-jdbc.

## npm (Node/Angular/tools)
- Root manifest: package.json
- Frontend manifest: frontend/package.json
- Tool manifests:
  - tools/mcp-server/package.json
  - tools/openproject-mcp/package.json
- Status: confirmed

## Other ecosystems
- .NET: unknown (NEEDS-EVIDENCE)
- Python: unknown (NEEDS-EVIDENCE)
- Go: unknown (NEEDS-EVIDENCE)
- Rust: unknown (NEEDS-EVIDENCE)
- Containers: partial (local tracker uses docker-compose)
  - Evidence: tools/local-tracker/docker-compose.yml

## Versions pinned
NEEDS-EVIDENCE: lockfile parsing and pinned versions list.

## Provenance
- backend/pom.xml
- package.json
- frontend/package.json
- tools/mcp-server/package.json
- tools/openproject-mcp/package.json
- tools/local-tracker/docker-compose.yml
