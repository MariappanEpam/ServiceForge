# ADR-0001: Target technology stack (Java 21 + React + Azure Entra ID + SQLite local-only)

Date: 2026-10-14
Status: Accepted

## Context
ServiceForge-Start is a training boilerplate with a working reference implementation using:
- Backend: Java/Spring Boot (current repo indicates Java 17 + Spring Boot 3)
- Frontend: Angular
- Persistence: SQLite via JDBC + Flyway migrations

The forward-engineering target stack is confirmed (per adapter-agent mode constraints):
- Backend: Java 21
- Frontend: React
- Auth: Azure Entra ID
- Persistence: SQLite local-only

We need a single, consistent target stack to:
- Produce forward-engineered feature specs (API contracts, UI flows, data model)
- Produce follow-on architecture and implementation plans
- Preserve the local-only persistence constraint

## Decision
Adopt the following target stack for ServiceForge-Migrated:
- Backend: Java 21 (Spring Boot 3.x)
- Frontend: React (TypeScript)
- Authentication/Authorization: Azure Entra ID (OIDC/OAuth2)
- Persistence: SQLite (local file) with versioned migrations (Flyway)

## Alternatives considered
1) Keep current stack (Java 17 + Angular)
- Pros: maximum reuse of existing code patterns
- Cons: conflicts with confirmed target stack; does not meet migration goal

2) Backend Java 21 + keep Angular
- Pros: smaller frontend change
- Cons: conflicts with confirmed target stack (React)

3) Backend Java 21 + React + use mock/in-memory persistence
- Pros: simplest runtime
- Cons: conflicts with existing evidence of SQLite + Flyway and the local-only persistence requirement for the migrated system

4) Backend Java 21 + React + managed cloud database
- Pros: production-like
- Cons: violates hard constraint: SQLite local-only

## Consequences
- All new forward-engineering artifacts (specs/ADRs) will describe React UI flows and Java 21 backend APIs.
- Existing SQLite schema evidence (tables/columns and constraints) becomes the baseline for the migrated data model.
- Authentication becomes a first-class cross-cutting concern; endpoints will require Entra-backed identity (details in ADR-0002).
- Implementation planning must account for a frontend rewrite (Angular → React) and backend upgrade (Java 17 → 21).

## Evidence links (repo-understanding)
- Feature catalog (confirmed features and current Angular/Java evidence): pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md
- Persistence entities (SQLite + Flyway evidence): pipeline/repo-understanding/serviceforge-system-overview/persistence-entities.md
- NFR baseline (locked; currently missing measurable targets): pipeline/repo-understanding/serviceforge-system-overview/nfr-baseline.md
- Compliance map (locked; currently missing regulated flow mapping): pipeline/repo-understanding/serviceforge-system-overview/compliance-map.md
