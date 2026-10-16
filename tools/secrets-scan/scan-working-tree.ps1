<#
  scan-working-tree.ps1

  Purpose:
  - Best-effort secrets scan for Claude Code workflow hooks.
  - Scans the current git working tree (tracked files) for likely secrets.

  Notes:
  - This is NOT a PreToolUse hook. It cannot intercept Write/Edit tool calls.
  - It is intended to fail the reviewer stage early if secrets are present.

  Exit codes:
    0 = clean
    2 = secrets detected
    3 = not a git repo / usage error
#>

$ErrorActionPreference = 'Stop'

function Write-Info([string]$msg) {
  [Console]::Error.WriteLine($msg)
}

try {
  $git = Get-Command git -ErrorAction Stop
} catch {
  Write-Info "[secrets-scan] git not found; cannot scan working tree."
  exit 3
}

try {
  $isRepo = git rev-parse --is-inside-work-tree 2>$null
  if ($LASTEXITCODE -ne 0 -or $isRepo -ne 'true') {
    Write-Info "[secrets-scan] Not a git working tree; cannot scan."
    exit 3
  }
} catch {
  Write-Info "[secrets-scan] Not a git working tree; cannot scan."
  exit 3
}

# Get tracked files only (avoid node_modules/target/etc)
$files = git ls-files
if ($LASTEXITCODE -ne 0) {
  Write-Info "[secrets-scan] Failed to list tracked files."
  exit 3
}

$repoRoot = git rev-parse --show-toplevel
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($repoRoot)) {
  Write-Info "[secrets-scan] Failed to resolve repo root."
  exit 3
}

$scanner = Join-Path $repoRoot 'tools\secrets-scan\scan-content.js'
if (-not (Test-Path $scanner)) {
  Write-Info "[secrets-scan] Missing scanner: $scanner"
  exit 3
}

$blocked = $false
$blockedFiles = New-Object System.Collections.Generic.List[string]

foreach ($f in $files) {
  $full = Join-Path $repoRoot $f

  if (-not (Test-Path $full)) { continue }

  # Skip large/binary-ish files by extension
  $ext = [IO.Path]::GetExtension($full).ToLowerInvariant()
  if ($ext -in @('.png','.jpg','.jpeg','.gif','.webp','.ico','.pdf','.zip','.jar','.exe','.dll','.pdb','.db','.sqlite','.bin','.trx')) {
    continue
  }

  # Read as text; if it fails, skip
  $content = $null
  try {
    $content = Get-Content -LiteralPath $full -Raw -ErrorAction Stop
  } catch {
    continue
  }

  # Write to temp file for scanner
  $tmp = [IO.Path]::GetTempFileName()
  try {
    Set-Content -LiteralPath $tmp -Value $content -Encoding UTF8

    node $scanner --path $f --content-file $tmp | Out-Null
    $code = $LASTEXITCODE

    if ($code -eq 2) {
      $blocked = $true
      $blockedFiles.Add($f) | Out-Null
    } elseif ($code -ne 0) {
      Write-Info "[secrets-scan] Scanner error ($code) on $f"
    }
  } finally {
    Remove-Item -LiteralPath $tmp -Force -ErrorAction SilentlyContinue
  }
}

if ($blocked) {
  Write-Info "[secrets-scan] BLOCKED: potential secrets detected in tracked files:"
  foreach ($bf in $blockedFiles) {
    Write-Info "  - $bf"
  }
  exit 2
}

Write-Info "[secrets-scan] OK: no secrets detected in tracked files."
exit 0
