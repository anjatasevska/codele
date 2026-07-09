# One-time / fresh machine setup for Codele (Windows PowerShell)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host "Created .env from .env.example — review CODELE_ADMIN_PASSWORD before production." -ForegroundColor Yellow
}

Write-Host "Starting PostgreSQL (Docker)..." -ForegroundColor Cyan
docker compose up -d

Write-Host "Waiting for database to be healthy..." -ForegroundColor Cyan
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    $status = docker inspect -f "{{.State.Health.Status}}" codele-postgres 2>$null
    if ($status -eq "healthy") { $ready = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $ready) {
    Write-Host "Postgres may still be starting. Check: docker compose logs postgres" -ForegroundColor Yellow
} else {
    Write-Host "PostgreSQL is ready on localhost:5432 (database: codele)" -ForegroundColor Green
}

Write-Host ""
Write-Host "Next: run the application" -ForegroundColor Cyan
Write-Host "  .\scripts\start.ps1"
