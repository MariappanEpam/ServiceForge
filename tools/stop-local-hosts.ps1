param(
  [switch]$StopBackend,
  [switch]$StopFrontend,
  [switch]$StopMcp,
  [switch]$All
)

$ErrorActionPreference = 'Stop'

function Stop-PortProcess {
  param([int]$Port)

  $conns = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
  if (-not $conns) {
    Write-Host "No listener found on port $Port"
    return
  }

  $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
  foreach ($procId in $pids) {
    try {
      $p = Get-Process -Id $procId -ErrorAction Stop
      Write-Host "Stopping PID $procId ($($p.ProcessName)) listening on port $Port"
      Stop-Process -Id $procId -Force
    } catch {
      Write-Warning ("Failed to stop PID {0} on port {1}: {2}" -f $procId, $Port, $_.Exception.Message)
    }
  }
}

function Clear-ActiveHostsEpisode {
  $episodePath = Join-Path $PSScriptRoot '..\pipeline\memory\episodes\active-hosts.md'
  $episodePath = (Resolve-Path $episodePath).Path

  if (-not (Test-Path $episodePath)) {
    Write-Host "Episode file not found: $episodePath"
    return
  }

  $content = @(
    '# Active local hosts',
    '',
    'Status: STOPPED',
    ('Updated: ' + (Get-Date -Format 'yyyy-MM-dd')),
    '',
    '## URLs',
    '- Backend (Spring Boot): (stopped)',
    '- Frontend (Angular dev server): (stopped)',
    '- MCP server: (stopped)',
    '',
    '## Notes',
    '- Hosts were stopped via tools/stop-local-hosts.ps1'
  ) -join "`n"

  Set-Content -Path $episodePath -Value $content -Encoding UTF8
  Write-Host "Updated episodic memory: $episodePath"
}

if ($All) {
  $StopBackend = $true
  $StopFrontend = $true
  $StopMcp = $true
}

if ($StopBackend) { Stop-PortProcess -Port 8080 }
if ($StopFrontend) {
  # Frontend port is dynamic; try common ports first.
  Stop-PortProcess -Port 4200
  Stop-PortProcess -Port 50759
}

if ($StopMcp) {
  # MCP server is stdio; cannot reliably stop by port.
  Write-Host 'MCP server runs over stdio; stop it by terminating the node process in its terminal session.'
}

Clear-ActiveHostsEpisode
