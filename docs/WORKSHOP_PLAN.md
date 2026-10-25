# Torque Workshop — Plan de Evolución a Sistema Profesional de Taller

> **Fecha:** Julio 2026
> **Propósito:** Transformar Torque de gestor personal de mantenimiento a sistema profesional multi-dispositivo para talleres mecánicos.

---

## Índice

1. [Stack Tecnológico](#1-stack-tecnológico)
2. [Arquitectura General](#2-arquitectura-general)
3. [Estructura del Proyecto](#3-estructura-del-proyecto)
4. [Modelo de Datos Completo](#4-modelo-de-datos-completo)
5. [Fases de Implementación](#5-fases-de-implementación)
6. [API Endpoints](#6-api-endpoints)
7. [Flujo de Trabajo Típico](#7-flujo-de-trabajo-típico)
8. [Decisiones de Arquitectura](#8-decisiones-de-arquitectura)
9. [Estado Actual del Código](#9-estado-actual-del-código)

---

## 1. Stack Tecnológico

| Capa | Tecnología | Versión | Propósito |
|------|-----------|---------|-----------|
| **Servidor** | | | |
| Framework API | Python FastAPI | 0.115+ | API REST + WebSockets |
| ORM | SQLAlchemy 2.0 | 2.0+ | Mapeo objeto-relacional |
| Migraciones | Alembic | — | Control de versiones de DB |
| Base de datos | PostgreSQL | 16+ | Base de datos central |
| Cache / WS | Redis | 7+ | Sesiones + WebSocket pub/sub |
| Auth | python-jose + passlib | — | JWT + hash de contraseñas |
| PDF | ReportLab | — | Generación de facturas/presupuestos |
| Testing | pytest | — | Tests del servidor |
| **Desktop** | | | |
| Lenguaje | Python | 3.12+ | Cliente de escritorio |
| UI | Tkinter / Flet / CustomTkinter | — | Interfaz de taller |
| HTTP Client | httpx | — | Comunicación con servidor |
| **Mobile** | | | |
| Lenguaje | Kotlin | 2.1.20 | Cliente Android |
| UI | Jetpack Compose | — | UI declarativa Android |
| DB local | Room | — | Caché offline-first |
| HTTP Client | Retrofit + OkHttp | — | Comunicación con servidor |
| Sync | Data Sync / manual | — | Sincronización offline |

---

## 2. Arquitectura General

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        SERVIDOR PYTHON (FastAPI)                         │
│                                                                          │
│  PostgreSQL ──┐                                                         │
│  Redis ───────┤── REST API (HTTP/JSON)                                   │
│  MinIO ───────┘   WebSocket (estado en tiempo real)                     │
│                   PDF generation (ReportLab)                             │
│                   Facturación / Reporting                                │
│                   Multi-tenant (cada taller aislado)                     │
└──────┬──────────────────────┬───────────────────────────────────────────┘
       │ REST + WS            │ REST + WS
       ▼                      ▼
┌──────────────────┐    ┌────────────────────────┐
│ Desktop (Taller)  │    │ Mobile (Mecánico)      │
│                   │    │                        │
│ Python            │    │ Kotlin Android         │
│ CustomTkinter/    │    │ Jetpack Compose        │
│ Flet              │    │ Room (offline-first)   │
│                   │    │ Sync offline → server  │
│ Gestión completa  │    │ Presupuestos           │
│ Clientes          │    │ Seguimiento reparación │
│ Agenda            │    │ Fichar entrada/salida  │
│ Facturación       │    │ Fotos diagnosis        │
│ Inventario        │    │ Firmas digitales       │
│ Reportes PDF      │    │ Escanear matrícula     │
└──────────────────┘    └────────────────────────┘
```

### Principios

- **Servidor central**: fuente de verdad única, todos los clientes se conectan a él
- **Desktop = HQ del taller**: gestión completa (clientes, vehículos, órdenes, facturación, inventario, informes)
- **Mobile = herramienta del mecánico**: offline-first, sincronización bidireccional
- **Comunicación en tiempo real**: WebSocket para cambios instantáneos mecánico ↔ taller
- **Multi-tenant**: un servidor sirve a múltiples talleres, aislados por `tenant_id`
- **API versionada**: `/api/v1/...` para facilitar evolución

---

## 3. Estructura del Proyecto

```
Torque.v2/
├── server/                         # Servidor Python FastAPI (√ completado)
│   ├── app/
│   │   ├── main.py                 # Entry point FastAPI
│   │   ├── config.py               # Configuración (entorno, DB, etc.)
│   │   ├── database.py             # Conexión SQLAlchemy + sesión
│   │   ├── models/                 # Modelos SQLAlchemy (12 entidades)
│   │   ├── schemas/                # Schemas Pydantic request/response
│   │   ├── api/v1/                 # Endpoints REST
│   │   ├── services/               # Lógica de negocio
│   │   └── core/                   # Security, WebSocket, exceptions
│   ├── alembic/                    # Migraciones DB
│   └── tests/                      # Tests pytest
│
├── desktop-python/                 # ← NUEVO: Cliente escritorio Python
│   ├── app/
│   │   ├── main.py                 # Entry point
│   │   ├── api_client.py           # Cliente HTTP para servidor
│   │   ├── views/                  # Pantallas (login, dashboard, clientes...)
│   │   ├── components/             # Componentes UI reutilizables
│   │   └── utils/                  # Utilidades
│   └── requirements.txt
│
├── app/                            # App Android (existente)
├── data/                           # Módulo data Android (existente)
├── core/                           # Módulo core Android (existente)
├── shared/                         # Módulo compartido JVM (existente)
│
├── docs/
│   ├── GUIA_DEV.md                 # Guía desarrollo Android
│   ├── README.md                   # README Android
│   ├── PLAN_COCHES.md              # Plan soporte coches
│   └── WORKSHOP_PLAN.md            # ← Este archivo
│
└── server/                         # Servidor Python FastAPI
    └── ...
```

---

## 4. Modelo de Datos Completo

### 4.1 Tablas Actuales (Room Android) — se mantienen

- `motos` → los datos se sincronizarán con el servidor
- `mantenimientos` → se migrarán a órdenes de trabajo
- `fotos_moto` → se migrarán a archivos adjuntos

### 4.2 Nuevas Tablas (PostgreSQL — ya implementadas en server/)

| Tabla | Propósito |
|-------|-----------|
| `talleres` | Multi-tenant: cada taller aislado |
| `usuarios` | Mecánicos, admin, recepción (con PIN para móvil) |
| `clientes` | Dueños de vehículos |
| `vehiculos` | Vehículos con todos los campos (tipo, combustible, etc.) |
| `ordenes_trabajo` | Órdenes de reparación con estados |
| `detalles_trabajo` | Líneas individuales de cada orden |
| `articulos` | Inventario de piezas |
| `piezas_utilizadas` | Piezas consumidas en cada detalle |
| `citas` | Agenda del taller |
| `facturas` | Facturación |
| `configuracion_taller` | Configuración específica del taller |
| `archivos` | Fotos, documentos, firmas |

---

## 5. Fases de Implementación

### Fase 0 — Servidor Python: Fundación (√ COMPLETADA)

| # | Tarea | Estado |
|---|-------|--------|
| 0.1 | Crear proyecto Python | ✓ |
| 0.2 | Configuración base | ✓ |
| 0.3 | Modelos SQLAlchemy | ✓ |
| 0.4 | Migraciones Alembic | ✓ |
| 0.5 | Autenticación JWT | ✓ |
| 0.6 | Schemas Pydantic | ✓ |
| 0.7 | CRUD Clientes | ✓ |
| 0.8 | CRUD Vehículos | ✓ |
| 0.9 | CRUD Órdenes de trabajo | ✓ |
| 0.10 | CRUD Artículos (inventario) | ✓ |
| 0.11 | CRUD Citas (agenda) | ✓ |
| 0.12 | CRUD Facturas | ✓ |
| 0.13 | Dashboard KPIs | ✓ |
| 0.14 | WebSocket | ✓ |
| 0.15 | Tests | ✓ |
| 0.16 | Migración de datos | ✓ |

### Fase 1 — Cliente escritorio Python (√ COMPLETADA)

| # | Tarea | Estado |
|---|-------|--------|
| 1.1 | Setup proyecto | ✓ |
| 1.2 | API Client | ✓ |
| 1.3 | Login screen (email + PIN) | ✓ |
| 1.4 | Dashboard KPIs | ✓ |
| 1.5 | Clientes screen + CRUD | ✓ |
| 1.6 | Vehículos screen + CRUD | ✓ |
| 1.7 | Órdenes screen + CRUD con estados | ✓ |
| 1.8 | Agenda screen (citas) | ✓ |

### Fase 2 — Desktop funcionalidades avanzadas (√ COMPLETADA)

| # | Tarea | Estado |
|---|-------|--------|
| 2.1 | Inventario screen (artículos + stock) | ✓ |
| 2.2 | Facturación desde orden | ✓ |
| 2.3 | PDF (generado por servidor) | ✓ |
| 2.4 | Dashboard KPIs | ✓ |
| 2.5 | UUID resolution en tablas | ✓ |

### Fase 3 — Mobile como herramienta de mecánico (pendiente)

| # | Tarea | Descripción |
|---|-------|-------------|
| 3.1 | Login PIN | Autenticación con PIN |
| 3.2 | Trabajos asignados | Lista desde servidor |
| 3.3 | Diagnosis + fotos | Cámara, firma, adjuntos |
| 3.4 | Presupuestos móvil | Crear y enviar |
| 3.5 | Fichaje | Check-in/check-out vehículos |
| 3.6 | Escáner matrícula | ML Kit |
| 3.7 | Sync offline | Room → API → PostgreSQL |
| 3.8 | Tiempo real | WebSocket |

### Fase 4 — Profesionalización (pendiente)

| # | Tarea |
|---|-------|
| 4.1 | Roles y permisos |
| 4.2 | Auditoría |
| 4.3 | Push notifications |
| 4.4 | Backup automático |
| 4.5 | Exportación Excel/CSV |
| 4.6 | Multi-idioma |

---

## 6. API Endpoints

(Ver documentación Swagger en `/docs` una vez arrancado el servidor, o consultar los archivos en `server/app/api/v1/`)

---

## 7. Flujo de Trabajo Típico

```
1. RECEPCIÓN (Desktop Python)
   ┌─ Llega cliente → buscar por matrícula/teléfono
   ├─ Si no existe → crear cliente + vehículo
   ├─ Crear orden de trabajo con problema descrito
   ├─ Asignar a mecánico
   ├─ Generar presupuesto (PDF) → cliente aprueba
   └─ Orden pasa a EN_CURSO → notificación WebSocket

2. MECÁNICO (Mobile Android)
   ┌─ Abre app, introduce PIN
   ├─ Ve órdenes asignadas (offline)
   ├─ Selecciona una → diagnóstico + fotos
   ├─ Solicita piezas desde inventario
   ├─ Marca detalles COMPLETADO
   └─ Taller ve cambio en tiempo real

3. CIERRE (Desktop Python)
   ┌─ Recepción: mecánico ha terminado
   ├─ Revisa trabajo, ajustes finales
   ├─ Genera factura (PDF)
   └─ Cierra orden → notifica al cliente
```

---

## 8. Decisiones de Arquitectura

| Decisión | Elección | Motivo |
|----------|----------|--------|
| **Lenguaje servidor** | Python (FastAPI) | Ecosistema para negocio, fácil de contratar |
| **Desktop** | Python (CustomTkinter/Flet) | Mismo lenguaje que servidor, reutiliza lógica |
| **Desktop connection** | Híbrido (caché local) | SQLite local como caché, servidor fuente de verdad |
| **Mobile sync** | Offline-first | Talleres con mala cobertura |
| **PDF generation** | Servidor (ReportLab) | Unifica formato, accesible desde ambos clientes |
| **Real-time** | WebSocket | Cambios instantáneos |
| **Multi-tenant** | Fila tenant_id | Más fácil de escalar |
| **ID format** | UUID | Previene conflictos sync offline |

---

## 9. Estado Actual del Código

### Completado (Fases 0, 1 y 2)

- `server/` — Servidor Python FastAPI completo con 14 tablas, CRUD completo, auth JWT, WebSocket, PDF, dashboard KPIs, tests (32/32)
- `desktop-python/` — Cliente de escritorio Python completo con 8 vistas (login, dashboard, clientes, vehículos, órdenes, citas, facturas, artículos), UUID resolution, CustomTkinter

### Por hacer (Fase 3+)

- `app/ android` — Conectar app Android al servidor (sync, login PIN, órdenes asignadas, diagnosis con fotos)
