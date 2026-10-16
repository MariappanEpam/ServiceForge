# ADR-0002: Authentication and authorization via Azure Entra ID

Date: 2026-10-14
Status: Accepted

## Context
The migrated target stack requires Azure Entra ID for authentication.

The current ServiceForge-Start repo-understanding artifacts do not document an existing auth mechanism (no confirmed auth flows are captured in the system overview). Therefore, auth is a forward-looking decision that must be explicitly recorded and then referenced by feature specs.

Additionally, the locked compliance map and NFR baseline currently lack evidence for regulated data flows and security targets; this increases risk and requires explicit NEEDS-EVIDENCE flags in specs.

## Decision
Use Azure Entra ID as the identity provider for ServiceForge-Migrated:
- Frontend (React): authenticate users using OIDC Authorization Code Flow with PKCE.
- Backend (Java 21 / Spring Boot): validate JWT access tokens issued by Entra (resource server).
- Authorization model (initial): role-based access control (RBAC) using Entra app roles and/or group claims.

Token handling:
- Frontend obtains access token for the backend API scope.
- Backend enforces authentication on all non-public endpoints.

## Alternatives considered
1) No authentication (open API)
- Pros: simplest
- Cons: conflicts with confirmed target stack requirement; unacceptable for most enterprise contexts

2) Local username/password
- Pros: self-contained
- Cons: conflicts with target stack requirement; adds credential storage and security burden

3) Third-party IdP (non-Entra)
- Pros: flexible
- Cons: conflicts with target stack requirement

4) API keys
- Pros: simple for service-to-service
- Cons: poor fit for interactive UI; weak user identity and auditing

## Consequences
- All feature APIs in ServiceForge-Migrated specs assume authenticated requests.
- UI flows must include sign-in/sign-out and token refresh behavior.
- Backend must include standardized auth error responses (401/403) in addition to existing 400/404/409 patterns.
- NEEDS-EVIDENCE: specific role definitions (dispatcher/admin/technician) are not evidenced in repo-understanding and must be confirmed during architecture/design.

## Evidence links (repo-understanding)
- Target stack requirement (forward-looking, confirmed by adapter-agent mode constraints): ServiceForge-Migrated/pipeline/decisions/adr-0001-target-tech-stack.md
- Compliance map baseline (locked; missing regulated flow mapping): pipeline/repo-understanding/serviceforge-system-overview/compliance-map.md
- NFR baseline (locked; missing security targets): pipeline/repo-understanding/serviceforge-system-overview/nfr-baseline.md
