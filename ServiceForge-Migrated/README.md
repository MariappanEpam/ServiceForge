# ServiceForge-Migrated

This folder contains the **migrated target-stack** implementation (Java 21 backend + React frontend + Azure Entra auth + SQLite local-only).

## Prerequisites
- Java 21 (required for the migrated backend)
- Maven
- Node.js (for the migrated frontend)

## Backend (Java 21 + Spring Boot)
From ServiceForge-Migrated/backend:
- Configure Java 21 as your active `JAVA_HOME`.
- Run tests: `mvn test`
- Run app: `mvn spring-boot:run`

Environment variables:
- `SERVICEFORGE_DB_PATH` (default: `./data/serviceforge.db`)
- `ENTRA_ISSUER_URI` (default placeholder)
- `ENTRA_AUDIENCE` (default placeholder)

## Frontend (React)
TBD (scaffold pending).
