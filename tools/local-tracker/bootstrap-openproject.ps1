param(
  [string]$BaseUrl = "http://localhost:8088",
  [string]$ProjectIdentifier = "serviceforge",
  [string]$ProjectName = "ServiceForge",
  [string]$AdminUser = "admin",
  [string]$AdminPassword = "admin",
  [switch]$SkipDockerUp
)

$ErrorActionPreference = 'Stop'

function Write-Step([string]$msg) {
  Write-Host "[openproject-bootstrap] $msg"
}

function Invoke-DockerCompose([string]$args) {
  $compose = "tools/local-tracker/docker-compose.yml"
  Write-Step "docker compose -f \"$compose\" $args"
  $parts = @($args -split ' ' | Where-Object { $_ -and $_.Trim().Length -gt 0 })
  & docker compose -f $compose @parts | Out-String | Write-Host
}

function Wait-HttpOk([string]$url, [int]$timeoutSeconds = 300) {
  $deadline = (Get-Date).AddSeconds($timeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    try {
      $resp = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing -TimeoutSec 10
      if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500) {
        return
      }
    } catch {
      Start-Sleep -Seconds 2
    }
  }
  throw "Timed out waiting for $url"
}

function Invoke-RailsRunner([string]$ruby) {
  $compose = "tools/local-tracker/docker-compose.yml"
  Write-Step "rails runner (inside container)"
  # Avoid quoting issues by writing the Ruby to a temp file and running it.
  $tmp = Join-Path $env:TEMP ("openproject-runner-{0}.rb" -f ([guid]::NewGuid().ToString('N')))
  Set-Content -Path $tmp -Value $ruby -Encoding UTF8
  try {
    & docker compose -f $compose cp $tmp openproject:/tmp/runner.rb | Out-String | Write-Host
    & docker compose -f $compose exec -T openproject ./bin/rails runner /tmp/runner.rb | Out-String | Write-Host
  } finally {
    Remove-Item -Force -ErrorAction SilentlyContinue $tmp
  }
}

Write-Step "Bootstrap starting"

if (-not $SkipDockerUp) {
  Invoke-DockerCompose "up -d"
}

Write-Step "Waiting for OpenProject to respond..."
Wait-HttpOk "$BaseUrl/" 300

# Ensure admin credentials are set (idempotent)
# NOTE: This is local-only convenience for the training repo.
$setAdminRuby = @"
user = User.find_by(login: '$AdminUser') || User.admin.first
raise 'No admin user found' unless user

user.login = '$AdminUser'
user.mail = user.mail.presence || 'admin@example.net'
user.firstname = user.firstname.presence || 'OpenProject'
user.lastname = user.lastname.presence || 'Admin'
user.admin = true

min_len = (Setting.respond_to?(:password_min_length) ? Setting.password_min_length.to_i : 10)
pwd = '$AdminPassword'
pwd = (pwd + ('0' * min_len))[0, min_len] if pwd.length < min_len

user.password = pwd
user.password_confirmation = pwd
user.status = Principal.statuses[:active] if user.respond_to?(:status)
user.save!
puts "Admin ensured: #{user.login} (password length=#{pwd.length})"
"@
Invoke-RailsRunner $setAdminRuby

# Ensure project exists (idempotent)
$ensureProjectRuby = @"
identifier = '$ProjectIdentifier'
name = '$ProjectName'
project = Project.find_by(identifier: identifier)
if project
  puts "Project exists: #{project.identifier}"
else
  project = Project.new(identifier: identifier, name: name)
  project.public = false
  project.active = true
  project.save!
  puts "Project created: #{project.identifier}"
end
"@
Invoke-RailsRunner $ensureProjectRuby

Write-Step "Ensuring baseline work packages (Epic/Stories/Plans) via REST API"
Write-Step "NOTE: You must create an API token manually and set OPENPROJECT_API_KEY to run the sync."

if (-not $env:OPENPROJECT_API_KEY) {
  Write-Step "OPENPROJECT_API_KEY is not set. Skipping work package creation."
  Write-Step "Next: Open $BaseUrl/my/access_token ; create API token ; then run:"
  Write-Step "  `$env:OPENPROJECT_API_KEY=\"YOUR_TOKEN_HERE\" ; `$env:OPENPROJECT_BASE_URL=\"$BaseUrl\" ; `$env:OPENPROJECT_PROJECT_IDENTIFIER=\"$ProjectIdentifier\" ; npm run openproject:sync"
  exit 0
}

$env:OPENPROJECT_BASE_URL = $BaseUrl
$env:OPENPROJECT_PROJECT_IDENTIFIER = $ProjectIdentifier

npm run openproject:sync

Write-Step "Bootstrap complete"
