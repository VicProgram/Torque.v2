.PHONY: help install server desktop test test-server test-desktop lint lint-python clean seed seed-marcas

help:
	@echo "╔══════════════════════════════════════════╗"
	@echo "║   Torque — Comandos disponibles          ║"
	@echo "╠══════════════════════════════════════════╣"
	@echo "║ make install      ─ Instalar dependencias ║"
	@echo "║ make server       ─ Iniciar servidor API  ║"
	@echo "║ make desktop      ─ Iniciar app escritorio║"
	@echo "║ make test         ─ Ejecutar tests        ║"
	@echo "║ make test-server  ─ Tests del servidor    ║"
	@echo "║ make test-desktop ─ Tests del desktop     ║"
	@echo "║ make lint         ─ detekt + Ruff         ║"
	@echo "║ make lint-python  ─ Ruff (server/desktop) ║"
	@echo "║ make clean        ─ Limpiar archivos temp ║"
	@echo "║ make seed         ─ Sembrar datos demo    ║"
	@echo "║ make seed-marcas  ─ Catálogo marcas/modelos║"
	@echo "╚══════════════════════════════════════════╝"

install:
	@echo "==> Instalando dependencias..."
	cd server && (test -d venv || python3 -m venv venv) && . venv/bin/activate && pip install -r requirements.txt
	cd desktop-python && (test -d .venv || python3 -m venv .venv) && . .venv/bin/activate && pip install -e ".[dev]"
	@echo "OK"

server:
	@echo "==> Iniciar servidor en http://localhost:8000"
	cd server && . venv/bin/activate && test -f .env || cp .env.example .env && \
	python3 -c "from app.database import engine, Base; from app.models import *; Base.metadata.create_all(bind=engine)" && \
	uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload

desktop:
	@echo "==> Iniciando app de escritorio"
	cd desktop-python && . .venv/bin/activate && python3 -m app.main

test: test-server test-desktop

test-server:
	cd server && . venv/bin/activate && python3 -m pytest tests/ -v

test-desktop:
	cd desktop-python && . .venv/bin/activate && python3 -m pytest tests/ -v

seed:
	@echo "==> Sembrando datos demo..."
	cd server && . venv/bin/activate && python3 scripts/seed_demo.py

seed-marcas:
	@echo "==> Sembrando catálogo de marcas y modelos..."
	cd server && . venv/bin/activate && python3 scripts/seed_marcas.py
	@echo "OK"

lint: lint-python
	@echo "==> Ejecutando detekt..."
	./gradlew detekt 2>/dev/null || echo "(detekt solo disponible en proyecto Android con Gradle)"

lint-python:
	@echo "==> Ejecutando Ruff..."
	cd server && . venv/bin/activate && ruff check .
	cd desktop-python && . .venv/bin/activate && ruff check .

clean:
	rm -rf server/venv desktop-python/.venv
	rm -rf server/__pycache__ desktop-python/__pycache__
	rm -rf server/**/__pycache__ desktop-python/**/__pycache__
	rm -rf .pytest_cache server/.pytest_cache desktop-python/.pytest_cache
	rm -rf build/
	@echo "Limpieza completada"