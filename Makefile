.PHONY: help install server desktop test test-server test-desktop lint clean seed

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
	@echo "║ make lint         ─ Analizar código (detek)║"
	@echo "║ make clean        ─ Limpiar archivos temp ║"
	@echo "║ make seed         ─ Sembrar datos demo    ║"
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
	cd server && . venv/bin/activate && python3 -c "import requests, json; \
	BASE = 'http://localhost:8000/api/v1'; \
	r = requests.post(f'{BASE}/auth/login', json={'email': 'admin@demo.com', 'password': 'demo1234'}); \
	r = requests.post(f'{BASE}/auth/registro-taller', json={'nombre_taller': 'Taller Demo', 'nombre_admin': 'Admin', 'email_admin': 'admin@demo.com', 'password': 'demo1234'}) if r.status_code != 200 else r; \
	token = r.json()['access_token']; \
	h = {'Authorization': f'Bearer {token}', 'Content-Type': 'application/json'}; \
	[requests.post(f'{BASE}/clientes', headers=h, json=c) for c in [{'nombre':'Juan Pérez','telefono':'612345678'},{'nombre':'María García','telefono':'611111111'}]]; \
	[requests.post(f'{BASE}/articulos', headers=h, json=a) for a in [{'nombre':'Aceite 10W40','sku':'ACE-001','cantidad_stock':20,'precio_venta':12.5},{'nombre':'Filtro Aceite','sku':'FIL-001','cantidad_stock':15,'precio_venta':8.0}]]; \
	print('Datos demo creados')"

lint:
	@echo "==> Ejecutando detekt..."
	./gradlew detekt 2>/dev/null || echo "(detekt solo disponible en proyecto Android con Gradle)"

clean:
	rm -rf server/venv desktop-python/.venv
	rm -rf server/__pycache__ desktop-python/__pycache__
	rm -rf server/**/__pycache__ desktop-python/**/__pycache__
	rm -rf .pytest_cache server/.pytest_cache desktop-python/.pytest_cache
	rm -rf build/
	@echo "Limpieza completada"