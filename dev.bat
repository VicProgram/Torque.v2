@echo off
REM Torque — Script de desarrollo para Windows CMD
REM Uso: dev.bat <comando>
REM   install    — Instalar dependencias
REM   server     — Iniciar servidor API
REM   desktop    — Iniciar app escritorio
REM   test       — Ejecutar tests
REM   seed       — Sembrar datos demo

if "%1"=="" goto help

if "%1"=="install" goto install
if "%1"=="server" goto server
if "%1"=="desktop" goto desktop
if "%1"=="test" goto test
if "%1"=="seed" goto seed

:help
echo ╔══════════════════════════════════════════╗
echo ║   Torque — Comandos disponibles          ║
echo ╠══════════════════════════════════════════╣
echo ║ dev.bat install     — Instalar deps     ║
echo ║ dev.bat server      — Iniciar API       ║
echo ║ dev.bat desktop     — Iniciar desktop   ║
echo ║ dev.bat test        — Ejecutar tests    ║
echo ║ dev.bat seed        — Datos demo        ║
echo ╚══════════════════════════════════════════╝
goto end

:install
echo ==^> Instalando dependencias...
cd server
if not exist venv python -m venv venv
call venv\Scripts\pip install -r requirements.txt
cd ..
cd desktop-python
if not exist .venv python -m venv .venv
call .venv\Scripts\pip install -e ".[dev]"
cd ..
echo OK
goto end

:server
echo ==^> Iniciando servidor en http://localhost:8000
cd server
if not exist .env copy .env.example .env
call venv\Scripts\python -c "from app.database import engine, Base; from app.models import *; Base.metadata.create_all(bind=engine)"
call venv\Scripts\uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
cd ..
goto end

:desktop
echo ==^> Iniciando app de escritorio...
cd desktop-python
call .venv\Scripts\python -m app.main
cd ..
goto end

:test
echo ==^> Tests del servidor...
cd server
call venv\Scripts\pytest tests/ -v
cd ..
echo ==^> Tests del desktop...
cd desktop-python
call .venv\Scripts\pytest tests/ -v
cd ..
goto end

:seed
echo ==^> Sembrando datos demo...
cd server
call venv\Scripts\python scripts\seed_demo.py
cd ..
goto end

:end
