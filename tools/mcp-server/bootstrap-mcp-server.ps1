param(
  [string]$BackendUrl = "http://localhost:4200",
  [switch]$SkipInstall,
  [switch]$GenerateFromOpenApi
)

$ErrorActionPreference = 'Stop'

function Write-Step([string]$msg) {
  Write-Host "[serviceforge-mcp-bootstrap] $msg"
}

function Test-CommandExists([string]$name) {
  return $null -ne (Get-Command $name -ErrorAction SilentlyContinue)
}

Write-Step "Bootstrap starting"

if (-not (Test-CommandExists "node")) {
  throw "Node.js is required (node not found in PATH)."
}

if (-not $SkipInstall) {
  Write-Step "Installing MCP server dependencies (npm install)"
  Push-Location "tools/mcp-server"
  try {
    npm install
  } finally {
    Pop-Location
  }
}

if ($GenerateFromOpenApi) {
  Write-Step "Generating MCP tools from OpenAPI (tools/mcp-server/openapi.json)"
  Push-Location "."
  try {
    node tools/mcp-server/generate-tools-from-openapi.mjs
  } finally {
    Pop-Location
  }
}

Write-Step "Checking ServiceForge backend availability at $BackendUrl"
try {
  $resp = Invoke-WebRequest -Uri "$BackendUrl/actuator/health" -Method GET -UseBasicParsing -TimeoutSec 5
  Write-Step "Backend health: HTTP $($resp.StatusCode)"
} catch {
  Write-Step "Backend health endpoint not reachable yet. MCP server can still start, but tools will fail until backend is up."
}

Write-Step "Next steps:"
Write-Step "  1) Ensure backend is running (from backend/: mvn spring-boot:run)"
Write-Step "  2) Start MCP server: npm run mcp:serviceforge"
Write-Step "  3) Configure VS Code MCP to point at tools/mcp-server (stdio)"

Write-Step "Bootstrap complete"
