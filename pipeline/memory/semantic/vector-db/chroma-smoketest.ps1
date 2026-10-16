param(
  [string]$BaseUrl = "http://localhost:8000"
)

$ErrorActionPreference = "Stop"

Write-Host "ChromaDB smoke test -> $BaseUrl" -ForegroundColor Cyan

$heartbeat = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/heartbeat"
Write-Host "Heartbeat OK:" -NoNewline
$heartbeat | ConvertTo-Json -Compress | Write-Host

Write-Host "Done." -ForegroundColor Green
