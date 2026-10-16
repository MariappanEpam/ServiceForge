param(
  [Parameter(Mandatory=$true)]
  [string]$ScopeSlug,

  [string]$RepoRoot
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($RepoRoot)) {
  # tools/codegraph/run-codegraph-index.ps1 -> repo root is ../..
  $RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\\.." )).Path
}

function Invoke-CodeGraphIndex {
  param(
    [Parameter(Mandatory=$true)][string]$Path,
    [Parameter(Mandatory=$true)][string]$Label,
    [Parameter(Mandatory=$true)][string]$OutDir
  )

  if (-not (Test-Path $Path)) {
    Write-Host "[codegraph] SKIP missing: $Label ($Path)" -ForegroundColor Yellow
    return
  }

  New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

  $logPath = Join-Path $OutDir ("codegraph-index-" + $Label + ".log")
  $logPath = Join-Path $OutDir ("codegraph-index-" + $Label + "-" + (Get-Date -Format "yyyyMMdd-HHmmss") + ".log")
  $statusPath = Join-Path $OutDir ("codegraph-status-" + $Label + ".txt")
  $filesPath = Join-Path $OutDir ("codegraph-files-" + $Label + ".txt")

  Write-Host ("[codegraph] Indexing " + $Label + ": " + $Path) -ForegroundColor Cyan

  Push-Location $Path
  try {
    $env:CODEGRAPH_TELEMETRY = "0"

    # Ensure initialized (creates .codegraph/). Non-interactive.
    & codegraph init -y . *>&1 | Tee-Object -FilePath $logPath -Append | Out-Null

    # Rebuild full index (idempotent). If the DB is locked (daemon running),
    # fall back to a best-effort status/files snapshot so reverse-engineering
    # still has usable navigation data.
    try {
      & codegraph index . *>&1 | Tee-Object -FilePath $logPath -Append | Out-Null
    }
    catch {
      "[codegraph] WARN: index failed for $Label. Capturing status/files anyway. Error: $($_.Exception.Message)" | Out-File -FilePath $logPath -Encoding utf8 -Append
    }

    # Capture status + file tree snapshot for reverse-engineering artifacts.
    & codegraph status . *>&1 | Out-File -FilePath $statusPath -Encoding utf8
    & codegraph files *>&1 | Out-File -FilePath $filesPath -Encoding utf8
  }
  finally {
    Pop-Location
  }
}

$artifactRoot = Join-Path $RepoRoot ("pipeline\repo-understanding\" + $ScopeSlug + "\codegraph")
New-Item -ItemType Directory -Force -Path $artifactRoot | Out-Null

Invoke-CodeGraphIndex -Path (Join-Path $RepoRoot "backend") -Label "backend" -OutDir $artifactRoot
Invoke-CodeGraphIndex -Path (Join-Path $RepoRoot "frontend") -Label "frontend" -OutDir $artifactRoot
Invoke-CodeGraphIndex -Path (Join-Path $RepoRoot "test-automation") -Label "test-automation" -OutDir $artifactRoot

Write-Host "[codegraph] Done. Artifacts: $artifactRoot" -ForegroundColor Green
