<#
.SYNOPSIS
Convert one or more .trx files to HTML reports and store them under test-execution/<feature>/reports.

.DESCRIPTION
- Discovers .trx files under a root folder (defaults to test-automation/**/TestResults)
- For each .trx, calls tools/trx-to-html.ps1 to generate a self-contained HTML report
- Writes reports to test-execution/<feature>/reports/

EXAMPLE
pwsh -File .\tools\trx-to-html-all.ps1 -FeatureSlug "feature-2-parts-reservation" -TrxRoot ".\test-automation"
#>

[CmdletBinding()]
param(
	[string]$FeatureSlug = "feature-2-parts-reservation",
	[string]$TrxRoot = "",
	[switch]$FailIfNoTrx
)

$ErrorActionPreference = 'Stop'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")

if ([string]::IsNullOrWhiteSpace($TrxRoot)) {
	$TrxRoot = Join-Path $repoRoot "test-automation"
} else {
	$TrxRoot = Join-Path $repoRoot $TrxRoot
}

$trxRootPath = Resolve-Path $TrxRoot

$reportsDir = Join-Path $repoRoot (Join-Path "test-execution" (Join-Path $FeatureSlug "reports"))
New-Item -ItemType Directory -Force -Path $reportsDir | Out-Null

$trxFiles = Get-ChildItem -Path $trxRootPath -Recurse -Filter *.trx -File -ErrorAction SilentlyContinue

if (-not $trxFiles -or $trxFiles.Count -eq 0) {
	$msg = "No .trx files found under $trxRootPath"
	if ($FailIfNoTrx) { throw $msg }
	Write-Warning $msg
	return
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"

foreach ($trx in $trxFiles) {
	$baseName = [IO.Path]::GetFileNameWithoutExtension($trx.Name)
	$outFile = Join-Path $reportsDir ("{0}-{1}.html" -f $timestamp, $baseName)

	Write-Host ("Generating HTML for {0}" -f $trx.FullName)
	# Use Windows PowerShell for maximum compatibility on Windows hosts.
	powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot "tools\trx-to-html.ps1") -TrxPath $trx.FullName -OutFile $outFile
}

Write-Host "HTML reports written to: $reportsDir"
