<#
  after-developer-complete.ps1
  - Kills processes listening on ports 8080 and 4200 (if any)
  - Starts backend (mvn spring-boot:run) in main/backend
  - Starts frontend (npm start) in main/frontend using the configured proxy

  Notes:
  - This script is intended for local development only. It will not run in CI.
  - It uses Stop-Process for any PID bound to the ports; ensure this is acceptable.
  - The script launches both servers in separate PowerShell jobs.
#>

function Kill-Port($port) {
    try {
        $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
        if ($conn) {
            $pids = $conn | Select-Object -ExpandProperty OwningProcess -Unique
            foreach ($pid in $pids) {
                try {
                    Write-Output "Killing PID $pid listening on port $port"
                    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                } catch {
                    Write-Warning ("Failed to kill PID {0}: {1}" -f $pid, $_)
                }
            }
        } else {
            Write-Output "No process found on port $port"
        }
    } catch {
        Write-Warning ("Error while checking port {0}: {1}" -f $port, $_)
    }
}

Push-Location -Path (Split-Path -Path $MyInvocation.MyCommand.Definition -Parent)
Set-Location -Path (Resolve-Path "..\..\")

# Kill ports
Kill-Port 8080
Kill-Port 4200

# Start backend
if (Test-Path "main\backend\pom.xml") {
    Write-Output "Starting backend (mvn spring-boot:run) in main/backend"
    Start-Job -Name "ServiceForgeBackend" -ScriptBlock {
        Set-Location -Path "$PWD\main\backend"
        # Use user's mvn if available, otherwise try mvnw
        if (Get-Command mvn -ErrorAction SilentlyContinue) {
            & mvn spring-boot:run
        } elseif (Test-Path "./mvnw.cmd") {
            & .\mvnw.cmd spring-boot:run
        } else {
            Write-Error "Maven not found. Please install Maven or provide mvnw.cmd in main/backend."
        }
    } | Out-Null
} else {
    Write-Warning "main/backend not found; skipping backend start"
}

# Start frontend
if (Test-Path "main\frontend\package.json") {
    Write-Output "Starting frontend (npm start) in main/frontend"
    Start-Job -Name "ServiceForgeFrontend" -ScriptBlock {
        Set-Location -Path "$PWD\main\frontend"
        if (Get-Command npm -ErrorAction SilentlyContinue) {
            & npm start --silent
        } else {
            Write-Error "npm not found. Please install Node.js/npm."
        }
    } | Out-Null
} else {
    Write-Warning "main/frontend not found; skipping frontend start"
}

Write-Output "after-developer-complete hook launched backend and frontend jobs."

Pop-Location
