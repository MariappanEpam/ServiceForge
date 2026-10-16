<#
.SYNOPSIS
Convert a .trx test result file to a simple self-contained HTML report.

.DESCRIPTION
- Reads a Visual Studio Test Results (.trx) file.
- Extracts overall outcome, counts, and per-test results.
- Writes an HTML report next to the trx (or to -OutFile).

This is intentionally dependency-free (no external report generator required).

.EXAMPLE
pwsh -File .\tools\trx-to-html.ps1 -TrxPath .\test-automation\src\ServiceForge.Tests.Api\TestResults\ui.trx -OutFile .\test-execution\feature-2-parts-reservation\ui-report.html
#>

[CmdletBinding()]
param(
  [Parameter(Mandatory=$true)]
  [string]$TrxPath,

  [Parameter(Mandatory=$false)]
  [string]$OutFile
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $TrxPath)) {
  throw "TRX not found: $TrxPath"
}

[xml]$trx = Get-Content -LiteralPath $TrxPath

# TRX uses a default namespace; use local-name() XPath to avoid namespace plumbing.
$testRunNode = $trx.SelectSingleNode("//*[local-name()='TestRun']")
$runName = $null
if ($null -ne $testRunNode -and $null -ne $testRunNode.Attributes -and $null -ne $testRunNode.Attributes['name']) {
  $runName = $testRunNode.Attributes['name'].Value
}
if ([string]::IsNullOrWhiteSpace($runName)) { $runName = 'Test Run' }

$counters = $trx.SelectSingleNode("//*[local-name()='ResultSummary']/*[local-name()='Counters']")

function Get-AttrInt($node, $attrName) {
  if ($null -eq $node) { return 0 }
  $a = $node.Attributes[$attrName]
  if ($null -eq $a) { return 0 }
  $v = 0
  [void][int]::TryParse($a.Value, [ref]$v)
  return $v
}

$total = Get-AttrInt $counters 'total'
$executed = Get-AttrInt $counters 'executed'
$passed = Get-AttrInt $counters 'passed'
$failed = Get-AttrInt $counters 'failed'
$skipped = Get-AttrInt $counters 'notExecuted'

$outcome = 'Unknown'
if ($failed -gt 0) { $outcome = 'Failed' }
elseif ($passed -gt 0 -and $failed -eq 0) { $outcome = 'Passed' }

$results = @()
$unitTestResults = $trx.SelectNodes("//*[local-name()='UnitTestResult']")
foreach ($r in $unitTestResults) {
  $testName = $null
  if ($null -ne $r.Attributes['testName']) { $testName = $r.Attributes['testName'].Value }

  $out = $null
  if ($null -ne $r.Attributes['outcome']) { $out = $r.Attributes['outcome'].Value }

  $dur = $null
  if ($null -ne $r.Attributes['duration']) { $dur = $r.Attributes['duration'].Value }

  $start = $null
  if ($null -ne $r.Attributes['startTime']) { $start = $r.Attributes['startTime'].Value }

  $end = $null
  if ($null -ne $r.Attributes['endTime']) { $end = $r.Attributes['endTime'].Value }

  $msgNode = $r.SelectSingleNode(".//*[local-name()='Message']")
  $errMsg = $null
  if ($null -ne $msgNode) { $errMsg = $msgNode.InnerText }

  $stkNode = $r.SelectSingleNode(".//*[local-name()='StackTrace']")
  $stkMsg = $null
  if ($null -ne $stkNode) { $stkMsg = $stkNode.InnerText }

  $results += [pscustomobject]@{
    TestName = $testName
    Outcome  = $out
    Duration = $dur
    Start    = $start
    End      = $end
    Error    = $errMsg
    Stack    = $stkMsg
  }
}

$css = @'
:root{color-scheme:light dark}
body{font-family:Segoe UI,Arial,sans-serif;margin:24px;max-width:1100px}
.header{display:flex;justify-content:space-between;align-items:flex-end;gap:16px;flex-wrap:wrap}
.badge{padding:4px 10px;border-radius:999px;font-weight:600;font-size:12px;display:inline-block}
.badge.pass{background:#e6ffed;color:#0a5c2b;border:1px solid #b7f5c8}
.badge.fail{background:#ffecec;color:#7a0b0b;border:1px solid #ffb3b3}
.kv{display:grid;grid-template-columns:180px 1fr;gap:6px 12px;margin-top:12px}
.kv div{padding:2px 0}
.table{width:100%;border-collapse:collapse;margin-top:18px}
.table th,.table td{border:1px solid #ddd;padding:8px;vertical-align:top}
.table th{background:#f6f6f6;text-align:left}
.row-pass{background:#f3fff6}
.row-fail{background:#fff5f5}
pre{white-space:pre-wrap;word-break:break-word;margin:8px 0}
small{color:#666}
@media (prefers-color-scheme:dark){
  .table th{background:#1f1f1f}
  .table th,.table td{border-color:#333}
  small{color:#aaa}
}
'@

function HtmlEncode([string]$s) {
  if ($null -eq $s) { return '' }
  return ($s.Replace('&','&amp;').Replace('<','&lt;').Replace('>','&gt;').Replace('"','&quot;').Replace("'",'&#39;'))
}

$badgeClass = if ($outcome -eq 'Passed') { 'pass' } else { 'fail' }

$rowsHtml = ($results | ForEach-Object {
  $rowClass = if ($_.Outcome -eq 'Passed') { 'row-pass' } else { 'row-fail' }
  $err = if ([string]::IsNullOrWhiteSpace($_.Error)) { '' } else { "<details><summary>Error</summary><pre>$(HtmlEncode $_.Error)</pre></details>" }
  $stk = if ([string]::IsNullOrWhiteSpace($_.Stack)) { '' } else { "<details><summary>StackTrace</summary><pre>$(HtmlEncode $_.Stack)</pre></details>" }
  "<tr class='$rowClass'><td>$(HtmlEncode $_.TestName)</td><td>$(HtmlEncode $_.Outcome)</td><td>$(HtmlEncode $_.Duration)</td><td>$err$stk</td></tr>"
}) -join "`n"

$generatedAt = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')

$html = @"
<!doctype html>
<html>
<head>
  <meta charset='utf-8' />
  <meta name='viewport' content='width=device-width, initial-scale=1' />
  <title>$(HtmlEncode $runName) — TRX Report</title>
  <style>$css</style>
</head>
<body>
  <div class='header'>
    <div>
      <h1 style='margin:0'>$(HtmlEncode $runName)</h1>
      <small>Generated: $generatedAt</small>
    </div>
    <div class='badge $badgeClass'>$outcome</div>
  </div>

  <div class='kv'>
    <div><b>TRX</b></div><div>$(HtmlEncode (Resolve-Path -LiteralPath $TrxPath).Path)</div>
    <div><b>Total</b></div><div>$total</div>
    <div><b>Executed</b></div><div>$executed</div>
    <div><b>Passed</b></div><div>$passed</div>
    <div><b>Failed</b></div><div>$failed</div>
    <div><b>Not executed</b></div><div>$skipped</div>
  </div>

  <h2>Test results</h2>
  <table class='table'>
    <thead>
      <tr><th>Test</th><th>Outcome</th><th>Duration</th><th>Details</th></tr>
    </thead>
    <tbody>
      $rowsHtml
    </tbody>
  </table>
</body>
</html>
"@

if ([string]::IsNullOrWhiteSpace($OutFile)) {
  $OutFile = [System.IO.Path]::ChangeExtension((Resolve-Path -LiteralPath $TrxPath).Path, '.html')
}

$OutDir = Split-Path -Parent $OutFile
if (-not [string]::IsNullOrWhiteSpace($OutDir)) {
  New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
}

Set-Content -LiteralPath $OutFile -Value $html -Encoding UTF8
Write-Host "Wrote HTML report: $OutFile"
