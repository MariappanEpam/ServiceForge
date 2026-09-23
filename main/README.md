# main — ServiceForge code root

This folder consolidates all source code, frontend, backend, and tests for the ServiceForge project.

Layout
- backend/: Java Spring Boot backend
- frontend/: Angular frontend
- tests/: shared test utilities and integration tests
- tools/: helper scripts

Guidelines
- New code and tests must be added under this folder.
- Existing top-level `backend/` and `frontend/` were moved into `main/` during the recent migration. If you roll back that move, update these notes accordingly.

CI and scripts
- This repository contains CI and helper scripts that may reference the old top-level paths. Common locations:
	- .github/ (automation, prompts, and Copilot hints)
	- tools/ (helper scripts and PowerShell helpers)

- After moving source folders into `main/`, update any CI workflows, build scripts, or CI runners that reference `backend/` or `frontend/` so they point to `main/backend/` and `main/frontend/` instead.
- A short notice describing the file move was created at main/moved_files_notice.txt — review it for details about moved build artifacts and caches.
