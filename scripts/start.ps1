# Start Codele with local profile (PostgreSQL via Docker)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

if (-not (docker ps -q -f name=codele-postgres 2>$null)) {
    Write-Host "PostgreSQL is not running. Run: .\scripts\setup.ps1" -ForegroundColor Red
    exit 1
}

# Load .env into process for Spring (optional; application.yml has defaults)
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
        }
    }
}

$env:SPRING_PROFILES_ACTIVE = "local"
$pgHost = if ($env:POSTGRES_HOST) { $env:POSTGRES_HOST } else { "localhost" }
$pgPort = if ($env:POSTGRES_PORT) { $env:POSTGRES_PORT } else { "5433" }
$pgDb = if ($env:POSTGRES_DB) { $env:POSTGRES_DB } else { "codele" }
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://${pgHost}:${pgPort}/${pgDb}"
$env:SPRING_DATASOURCE_USERNAME = if ($env:POSTGRES_USER) { $env:POSTGRES_USER } else { "codele" }
$env:SPRING_DATASOURCE_PASSWORD = if ($env:POSTGRES_PASSWORD) { $env:POSTGRES_PASSWORD } else { "codele" }
Write-Host "Database: ${pgHost}:${pgPort}/${pgDb}" -ForegroundColor Cyan
Write-Host "Starting Codele (profile: local) at http://localhost:$($env:SERVER_PORT ?? '8080')" -ForegroundColor Green
& .\mvnw.cmd spring-boot:run
