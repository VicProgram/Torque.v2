# Torque v2

Sistema profesional de gestión de taller mecánico multi-dispositivo.

> 🇬🇧 [English README](../README.md)

---

## Componentes

| Componente | Tecnología | Descripción |
|---|---|---|
| **Servidor API** | Python FastAPI + SQLite | Backend REST con auth JWT, WebSocket, PDF |
| **Desktop (Taller)** | Python CustomTkinter | Gestión de clientes, vehículos, órdenes, citas, facturas, artículos |
| **Android (Mecánico)** | Kotlin + Jetpack Compose | App de gestión de mantenimiento de vehículos con algoritmo de salud |

---

## Cómo empezar

### Requisitos

- **Python 3.12+**
- **JDK 17+** (para Android)
- **Android SDK 35** (para Android)

### Servidor

```bash
# Linux / WSL
make install      # crear venv + instalar deps
make server       # arrancar en http://localhost:8000
make seed         # sembrar datos demo (con server corriendo)
make test-server  # ejecutar tests

# Windows (PowerShell)
.\dev.ps1 install
.\dev.ps1 server
.\dev.ps1 seed

# Windows (CMD)
dev.bat install
dev.bat server
```

### Desktop Python

```bash
# Linux / WSL
make desktop      # lanza la app CustomTkinter
make test-desktop # tests

# Windows
.\dev.ps1 desktop
```

> **Importante**: CustomTkinter necesita Tkinter. En Windows, marca "tcl/tk and IDLE" al instalar Python.

### Android

```bash
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/
```

---

## Arquitectura

```
┌─────────────────────────────────────────────────────┐
│                   SERVIDOR (FastAPI)                  │
│  SQLite ── REST API ── Auth JWT ── WebSocket ── PDF  │
│  Multi-taller (registros aislados por id_taller)     │
└──────┬────────────────────────────────────┬──────────┘
       │ REST + JWT                         │ REST + JWT
       ▼                                    ▼
┌──────────────────┐              ┌────────────────────┐
│ Desktop (Taller)  │              │ Android (Mecánico) │
│ CustomTkinter     │              │ Jetpack Compose    │
│ Gestión completa  │              │ Room offline       │
│ Clientes,órdenes, │              │ Algoritmo salud    │
│ facturas,stock    │              │ Historial local    │
└──────────────────┘              └────────────────────┘
```

---

## Estructura del proyecto

```
Torque.v2/
├── server/                # Servidor Python FastAPI
│   ├── app/
│   │   ├── main.py        # Entry point
│   │   ├── models/        # SQLAlchemy (14 tablas)
│   │   ├── schemas/       # Pydantic request/response
│   │   ├── api/v1/        # Endpoints REST
│   │   ├── services/      # Lógica de negocio
│   │   └── core/          # Security, WebSocket, exceptions
│   ├── alembic/           # Migraciones DB
│   └── tests/             # pytest (16 tests)
│
├── desktop-python/        # Cliente escritorio
│   ├── app/
│   │   ├── main.py        # Entry point
│   │   ├── api_client.py  # Cliente HTTP
│   │   ├── views/         # 8 pantallas (login, dashboard, clientes...)
│   │   ├── components/    # Sidebar, table_widget, dialogs, form_widgets
│   │   └── utils/         # Helpers
│   └── tests/             # pytest (4 tests)
│
├── app/                   # Android UI (Jetpack Compose)
├── data/                  # Android data layer (Room)
├── core/                  # Android domain layer
├── shared/                # Android shared module
│
├── Makefile               # Comandos Linux/WSL
├── dev.ps1                # Comandos Windows PowerShell
├── dev.bat                # Comandos Windows CMD
│
└── docs/                  # Documentación
```

---

## Comandos rápidos

| Acción | Linux/WSL | Windows |
|--------|-----------|---------|
| Instalar deps | `make install` | `.\dev.ps1 install` |
| Arrancar server | `make server` | `.\dev.ps1 server` |
| Arrancar desktop | `make desktop` | `.\dev.ps1 desktop` |
| Tests server | `make test-server` | `.\dev.ps1 test` |
| Tests desktop | `make test-desktop` | (incluido en test) |
| Datos demo | `make seed` | `.\dev.ps1 seed` |
| Sembrar marcas/modelos | `make seed-marcas` | `python server/scripts/seed_marcas.py` |
| Compilar Android | `./gradlew assembleDebug` | `./gradlew assembleDebug` |
| Análisis estático | `make lint` | `./gradlew detekt` |

---

## API Endpoints

Con el servidor corriendo, abrir http://localhost:8000/docs para Swagger UI.

### Auth
- `POST /api/v1/auth/registro-taller` — Crear taller + admin
- `POST /api/v1/auth/login` — Login (email/password)
- `POST /api/v1/auth/login-pin` — Login por PIN
- `GET /api/v1/auth/me` — Perfil actual
- `PATCH /api/v1/auth/me/pin` — Asignar PIN

### Gestión
- `GET/POST /api/v1/clientes` — Listar / crear clientes
- `GET/PUT/DELETE /api/v1/clientes/{id}` — CRUD cliente
- `GET/POST /api/v1/vehiculos` — Listar / crear vehículos
- `GET/PUT/DELETE /api/v1/vehiculos/{id}` — CRUD vehículo
- `GET /api/v1/vehiculos/buscar-matricula/{matricula}` — Búsqueda
- `GET/POST /api/v1/ordenes` — Listar / crear órdenes
- `GET/PUT/DELETE /api/v1/ordenes/{id}` — CRUD orden
- `GET/POST /api/v1/citas` — Listar / crear citas
- `GET/PUT/DELETE /api/v1/citas/{id}` — CRUD cita
- `GET/POST /api/v1/articulos` — Listar / crear artículos
- `GET/PUT/DELETE /api/v1/articulos/{id}` — CRUD artículo
- `GET/POST /api/v1/facturas` — Listar / crear facturas
- `GET/DELETE /api/v1/facturas/{id}` — CRUD factura
- `GET /api/v1/dashboard` — KPIs del taller

### Catálogo de Vehículos
- `GET /api/v1/marcas?tipo=MOTO|COCHE` — Lista marcas disponibles (47 moto, 31 coche)
- `GET /api/v1/marcas/{id}/modelos` — Lista modelos de una marca (1383 modelos total)

---

## Tests

```bash
# Servidor (16 tests)
cd server && . venv/bin/activate && pytest tests/ -v

# Desktop (4 tests)
cd desktop-python && . .venv/bin/activate && pytest tests/ -v

# Android
./gradlew testDebugUnitTest :shared:test   # tests unitarios (app + shared)
./gradlew connectedCheck                    # tests instrumentados (requiere emulador)
./gradlew detekt              # análisis estático
```

---

## Demo

Usuario: `admin@demo.com`
Contraseña: `demo1234`
PIN: `1234`

O ejecutar `make seed` (o `.\dev.ps1 seed`) con el servidor corriendo para crear datos de ejemplo.

Para sembrar el catálogo de marcas y modelos de vehículos, ejecutar `make seed-marcas` (idempotente — no duplica datos).

---

## Licencia

[MIT](../LICENSE) © 2026 Victor Abad Romero
