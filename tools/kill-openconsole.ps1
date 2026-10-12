<#
.SYNOPSIS
Kills all running OpenConsole.exe processes.

.DESCRIPTION
Some local runs (Playwright, Angular, Java, etc.) can leave OpenConsole.exe processes behind.
This script force-terminates all OpenConsole.exe instances.

Use as a pre-run hook before starting the application or test runs.

.EXAMPLE
pwsh -File .\tools\kill-openconsole.ps1
#>

[CmdletBinding()]
param(
	[switch]$WhatIf
)

$ErrorActionPreference = 'Stop'

$procs = Get-Process -Name 'OpenConsole' -ErrorAction SilentlyContinue

if (-not $procs) {
	Write-Host 'No OpenConsole.exe processes found.'
	return
}

Write-Host ("Found {0} OpenConsole.exe process(es)." -f $procs.Count)

if ($WhatIf) {
	$procs | ForEach-Object {
		Write-Host ("[WhatIf] Would kill PID={0} Name={1}" -f $_.Id, $_.ProcessName)
	}
	return
}

# Per request: kill all OpenConsole.exe processes.
# Note: this may terminate the terminal hosting this script if it is backed by OpenConsole.
Stop-Process -Name OpenConsole -Force -ErrorAction SilentlyContinue

$remaining = Get-Process -Name 'OpenConsole' -ErrorAction SilentlyContinue
if ($remaining) {
	Write-Warning ("Some OpenConsole.exe processes are still running: {0}" -f (($remaining | Select-Object -ExpandProperty Id) -join ', '))
} else {
	Write-Host 'All OpenConsole.exe processes terminated.'
}
