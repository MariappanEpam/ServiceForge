# ADR-0003: Persistence via SQLite (local-only) with versioned migrations

Date: 2026-10-14
Status: Accepted

## Context
ServiceForge-Start already uses SQLite via JDBC and manages schema via Flyway migrations. The adapter-agent hard constraints require SQLite local-only for the migrated system.

The repo-understanding persistence overview documents:
- SQLite JDBC datasource configuration
- Flyway enabled and migrations present
- Tables and columns for technicians, jobs, parts, and job part reservations
- A DB-level constraint preventing negative inventory

## Decision
Persist ServiceForge-Migrated data in a local SQLite database file, managed by versioned migrations:
- Use Flyway for schema migrations.
- Keep schema aligned with the evidenced tables/columns as the baseline.
- Enforce critical invariants at the database layer where evidenced (e.g., non-negative inventory).

## Alternatives considered
1) In-memory persistence
- Pros: simplest
- Cons: loses durability; conflicts with existing evidence and target constraint

2) Managed cloud database (Postgres/Azure SQL)
- Pros: scalable
- Cons: violates local-only constraint

3) Embedded H2
- Pros: easy for Java
- Cons: diverges from existing evidence and constraint

4) ORM/JPA entities
- Pros: developer productivity
- Cons: current evidence indicates plain JDBC/POJOs; adopting ORM is a larger architectural shift (can be revisited later)

## Consequences
- Local developer experience remains self-contained (no external DB dependency).
- Schema evolution must be explicit and versioned.
- Feature specs will reference SQLite tables/columns and constraints.
- NEEDS-EVIDENCE: data retention, backup/restore, and multi-user concurrency expectations are not defined in the locked NFR baseline.

## Evidence links (repo-understanding)
- Persistence entities overview (SQLite + Flyway + schema): pipeline/repo-understanding/serviceforge-system-overview/persistence-entities.md
- Business rule for non-negative inventory constraint: pipeline/repo-understanding/serviceforge-system-overview/business-rules.md
- NFR baseline (locked; missing measurable targets): pipeline/repo-understanding/serviceforge-system-overview/nfr-baseline.md
