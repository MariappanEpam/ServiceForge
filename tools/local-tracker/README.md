# Local delivery tracker (UI)

This repo includes an optional local issue tracker UI using **OpenProject** (Docker).

It can be used to manage delivery artifacts as:
- Epic (OpenProject: *Project* or *Work package* depending on your preference)
- User stories / tasks (OpenProject: *Work packages*)
- Tests (OpenProject: *Work packages* with type "Test" or "Task")

## Prereqs
- Docker Desktop running

## Start
From repo root:

1) Start the tracker
- `docker compose -f tools/local-tracker/docker-compose.yml up -d`

Or use npm:
- `npm run tracker:up`

2) Open UI
- http://localhost:8088

## First-time setup
OpenProject will guide you through initial setup in the browser.

This repo also provides an idempotent bootstrap script that:
- starts the containers (unless `-SkipDockerUp`)
- ensures local admin credentials (default `admin/admin`)
- ensures the `serviceforge` project exists

Run:
- `npm run tracker:bootstrap`

API token is intentionally NOT automated. Create it in the UI:
- http://localhost:8088/my/access_token
Then set it in your terminal session:
- `$env:OPENPROJECT_API_KEY = "<token>"`
And sync baseline work packages:
- `npm run openproject:sync`

## Stop
- `docker compose -f tools/local-tracker/docker-compose.yml down`

## Reset (delete all tracker data)
- `docker compose -f tools/local-tracker/docker-compose.yml down -v`

## Notes
- Data is stored in Docker volumes (`openproject_pgdata`, `openproject_assets`).
- This is intentionally local-only and does not integrate with jiraeu.epam.com.
