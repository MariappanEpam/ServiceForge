ServiceForge MCP Server
======================

Purpose
-------
Lightweight MCP wrapper that accepts prompt-based booking instructions and forwards them to the local ServiceForge backend (/api/jobs).

Quick start
-----------
1. Copy .env.example to .env and adjust SF_BACKEND_URL and API_KEY.
2. Install dependencies: npm install
3. Start: npm start

Endpoints
---------
- POST /prompt/book-job (requires x-api-key header or ?apiKey=)
  - body: { prompt: string }
  - returns: job created by backend
- GET /proxy/* (proxy to backend /api/* endpoints)

Security
--------
This is a demo scaffold. Use Azure Managed Identity and secure token exchange for production.
