# OpenProject MCP (local)

This MCP server exposes a small set of tools to manage OpenProject work packages for ServiceForge delivery tracking (implementation plan + test plan).

## Prereqs
- Local OpenProject running (see `tools/local-tracker/`)
- An OpenProject API token

## Environment variables
- `OPENPROJECT_BASE_URL` (default: `http://localhost:8088`)
- `OPENPROJECT_API_KEY` (**required**) – do not commit
- `OPENPROJECT_PROJECT_IDENTIFIER` (default: `serviceforge`)

## Run
From repo root:

- Install deps:
  - `npm install --prefix tools/openproject-mcp`
- Start server:
  - `npm start --prefix tools/openproject-mcp`

## Tools
- `openproject_get_project`
- `openproject_list_work_packages`
- `openproject_ensure_work_package`
- `openproject_update_work_package_description`
- `openproject_add_comment`
- `openproject_attach_file`

Notes:
- Auth uses OpenProject API v3 Basic auth: username `apikey`, password = token.
- Attachments are uploaded via `/api/v3/attachments` then linked to a work package.
