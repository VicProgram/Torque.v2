# Torque — Script de desarrollo para Windows PowerShell
# Uso: .\dev.ps1 <comando>
#   install    — Instalar dependencias
#   server     — Iniciar servidor API
#   desktop    — Iniciar app escritorio
#   test       — Ejecutar tests
#   seed       — Sembrar datos demo

param([string]$cmd = "help")

$PY = if (Get-Command python -ErrorAction SilentlyContinue) { "python" }
       elseif (Get-Command python3 -ErrorAction SilentlyContinue) { "python3" }
       else { Write-Error "Python no encontrado"; exit 1 }

function Install-Deps {
    Write-Host "==> Instalando dependencias..." -ForegroundColor Green
    Push-Location server
    if (-not (Test-Path venv)) { & $PY -m venv venv }
    & .\venv\Scripts\pip install -r requirements.txt
    Pop-Location

    Push-Location desktop-python
    if (-not (Test-Path .venv)) { & $PY -m venv .venv }
    & .\.venv\Scripts\pip install -e ".[dev]"
    Pop-Location
    Write-Host "OK" -ForegroundColor Green
}

function Start-Server {
    Write-Host "==> Iniciando servidor en http://localhost:8000" -ForegroundColor Green
    Push-Location server
    if (-not (Test-Path .env)) { Copy-Item .env.example .env }
    & .\venv\Scripts\python -c "from app.database import engine, Base; from app.models import *; Base.metadata.create_all(bind=engine)"
    Write-Host "Servidor arrancando (Ctrl+C para parar)" -ForegroundColor Yellow
    & .\venv\Scripts\uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
    Pop-Location
}

function Start-Desktop {
    Write-Host "==> Iniciando app de escritorio..." -ForegroundColor Green
    Push-Location desktop-python
    & .\.venv\Scripts\python -m app.main
    Pop-Location
}

function Run-Tests {
    Write-Host "==> Tests del servidor..." -ForegroundColor Green
    Push-Location server
    & .\venv\Scripts\pytest tests/ -v
    Pop-Location

    Write-Host "==> Tests del desktop..." -ForegroundColor Green
    Push-Location desktop-python
    & .\.venv\Scripts\pytest tests/ -v
    Pop-Location
}

function Seed-Data {
    Write-Host "==> Sembrando datos demo..." -ForegroundColor Green
    Push-Location server
    & .\venv\Scripts\python scripts\seed_demo.py
    Pop-Location
}

switch ($cmd) {
    "install"  { Install-Deps }
    "server"   { Start-Server }
    "desktop"  { Start-Desktop }
    "test"     { Run-Tests }
    "seed"     { Seed-Data }
    default {
        Write-Host @"
╔══════════════════════════════════════════╗
║   Torque — Comandos disponibles          ║
╠══════════════════════════════════════════╣
║ .\dev.ps1 install     — Instalar deps   ║
║ .\dev.ps1 server      — Iniciar API     ║
║ .\dev.ps1 desktop     — Iniciar desktop ║
║ .\dev.ps1 test        — Ejecutar tests  ║
║ .\dev.ps1 seed        — Datos demo      ║
╚══════════════════════════════════════════╝
"@
    }
}
