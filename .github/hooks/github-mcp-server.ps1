<#
  Starts the local GitHub MCP server.

  Notes:
  - Intended for local use only.
  - Requires Node.js and gh CLI authentication.
#>

$ErrorActionPreference = 'Stop'

Write-Host "[github-mcp] Installing dependencies..."
Push-Location "main/tools/github-mcp"
try {
  if (Test-Path "package-lock.json") {
    npm ci
  } else {
    npm install
  }

  Write-Host "[github-mcp] Starting MCP server (stdio)..."
  node .\index.js
} finally {
  Pop-Location
}
