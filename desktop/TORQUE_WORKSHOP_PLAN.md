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
| Lenguaje | Kotlin | 2.1.20 | Cliente de escritorio |
| UI | Compose Desktop | 1.7.3 | UI declarativa con Material 3 |
| DB local | SQLDelight | 2.0.2 | Caché SQLite local |
| HTTP Client | Ktor Client / OkHttp | — | Comunicación con servidor |
| JSON | kotlinx.serialization | — | Serialización |
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
│ Kotlin            │    │ Kotlin Android         │
│ Compose Desktop   │    │ Jetpack Compose        │
│ SQLDelight (caché)│    │ Room (offline-first)   │
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

Después de la Fase 0, el repositorio queda así:

```
Torque.v2/
├── settings.gradle.kts              # Incluye :app, :core, :data, :shared, :desktop
├── build.gradle.kts                 # Plugins raíz
├── gradle/
├── shared/                          # Módulo compartido JVM (existe)
├── desktop/                         # Módulo desktop Kotlin (existe)
├── app/                             # App Android (existe)
├── data/                            # Módulo data Android (existe)
├── core/                            # Módulo core Android (existe)
├── docs/                            # Documentación
│   ├── DESKTOP_GUIA.md
│   └── DESKTOP_PLAN.md
│
└── server/                          # ← NUEVO: Servidor Python FastAPI
    ├── pyproject.toml               # Dependencias Python
    ├── alembic.ini                  # Configuración de migraciones
    ├── alembic/                     # Migraciones de base de datos
    │   ├── env.py
    │   └── versions/
    ├── app/                         # Código de la aplicación
    │   ├── __init__.py
    │   ├── main.py                  # Entry point FastAPI
    │   ├── config.py                # Configuración (entorno, DB, etc.)
    │   ├── database.py              # Conexión SQLAlchemy + sesión
    │   ├── models/                  # Modelos SQLAlchemy
    │   │   ├── __init__.py
    │   │   ├── base.py              # Base declarativa
    │   │   ├── taller.py            # Taller (tenant)
    │   │   ├── usuario.py           # Usuario / mecánico
    │   │   ├── cliente.py           # Cliente
    │   │   ├── vehiculo.py          # Vehículo
    │   │   ├── orden_trabajo.py    # Orden de trabajo
    │   │   ├── detalle_trabajo.py  # Detalle de orden
    │   │   ├── articulo.py         # Inventario / piezas
    │   │   ├── pieza_utilizada.py  # Piezas usadas en reparación
    │   │   ├── cita.py             # Citas / agenda
    │   │   ├── factura.py          # Facturación
    │   │   └── configuracion.py    # Configuración del taller
    │   ├── schemas/                 # Schemas Pydantic (request/response)
    │   │   ├── __init__.py
    │   │   ├── auth.py
    │   │   ├── cliente.py
    │   │   ├── vehiculo.py
    │   │   ├── orden_trabajo.py
    │   │   ├── articulo.py
    │   │   ├── cita.py
    │   │   ├── factura.py
    │   │   └── common.py            # Paginación, respuestas genéricas
    │   ├── api/                     # Router de endpoints
    │   │   ├── __init__.py
    │   │   ├── router.py            # Router principal, agrupa todos
    │   │   ├── v1/
    │   │   │   ├── __init__.py
    │   │   │   ├── auth.py
    │   │   │   ├── clientes.py
    │   │   │   ├── vehiculos.py
    │   │   │   ├── ordenes.py
    │   │   │   ├── articulos.py
    │   │   │   ├── citas.py
    │   │   │   ├── facturas.py
    │   │   │   └── dashboard.py
    │   │   └── deps.py              # Dependencias (get_db, get_current_user)
    │   ├── services/                # Lógica de negocio
    │   │   ├── __init__.py
    │   │   ├── auth_service.py
    │   │   ├── cliente_service.py
    │   │   ├── orden_service.py
    │   │   ├── pdf_service.py       # Generación de PDFs
    │   │   └── sync_service.py      # Lógica de sincronización
    │   └── core/                    # Utilidades transversales
    │       ├── __init__.py
    │       ├── security.py          # JWT, hash
    │       ├── websocket.py         # Gestor de conexiones WS
    │       └── exceptions.py        # Manejadores de error
    ├── tests/                       # Tests
    │   ├── __init__.py
    │   ├── conftest.py              # Fixtures (DB de prueba, cliente test)
    │   ├── test_auth.py
    │   ├── test_clientes.py
    │   └── test_vehiculos.py
    └── requirements.txt             # Dependencias (alternativa a pyproject.toml)
```

---

## 4. Modelo de Datos Completo

### 4.1 Tablas Actuales (SQLDelight / Room) — se mantienen

```sql
motos              → migrar a vehiculos (con campos extendidos)
mantenimientos     → migrar a ordenes_de_trabajo (concepto más amplio)
fotos_moto         → migrar a tabla genérica de archivos adjuntos
fotos_mantenimiento → migrar a archivos adjuntos
```

### 4.2 Nuevas Tablas (PostgreSQL)

```sql
-- ============================================================
-- TALLERES (multi-tenant)
-- ============================================================
CREATE TABLE talleres (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(200) NOT NULL,
    direccion TEXT,
    telefono VARCHAR(20),
    email VARCHAR(200),
    web VARCHAR(200),
    nif VARCHAR(20),
    logo_url TEXT,
    tipo_iva DECIMAL(5,2) DEFAULT 21.00,
    divisa VARCHAR(3) DEFAULT 'EUR',
    pie_pagina_factura TEXT,
    creado_en TIMESTAMP DEFAULT NOW(),
    activo BOOLEAN DEFAULT TRUE
);

-- ============================================================
-- USUARIOS (mecánicos, admin, recepción)
-- ============================================================
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_taller UUID NOT NULL REFERENCES talleres(id),
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    pin_code VARCHAR(6),               -- PIN para login rápido en móvil
    rol VARCHAR(20) NOT NULL DEFAULT 'MECANICO'
        CHECK (rol IN ('ADMIN', 'MECANICO', 'RECEPCION')),
    telefono VARCHAR(20),
    activo BOOLEAN DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT NOW(),
    ultimo_acceso TIMESTAMP
);

-- ============================================================
-- CLIENTES
-- ============================================================
CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_taller UUID NOT NULL REFERENCES talleres(id),
    nombre VARCHAR(200) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(200),
    direccion TEXT,
    nif VARCHAR(20),
    notas TEXT,
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- VEHÍCULOS (reemplaza a motos)
-- ============================================================
CREATE TABLE vehiculos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_taller UUID NOT NULL REFERENCES talleres(id),
    id_cliente UUID REFERENCES clientes(id),
    matricula VARCHAR(20),
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    anio INTEGER DEFAULT 0,
    bastidor VARCHAR(50),
    tipo_vehiculo VARCHAR(20) DEFAULT 'MOTO'
        CHECK (tipo_vehiculo IN ('MOTO', 'COCHE', 'FURGONETA', 'CAMION', 'OTRO')),
    tipo_combustible VARCHAR(20)
        CHECK (tipo_combustible IN ('GASOLINA', 'DIESEL', 'ELECTRICO', 'HIBRIDO', 'GLP')),
    tipo_carroceria VARCHAR(30),
    transmision VARCHAR(20) DEFAULT 'MANUAL'
        CHECK (transmision IN ('MANUAL', 'AUTOMATICA', 'CVT', 'SEMIAUTOMATICA')),
    numero_puertas INTEGER,
    cilindrada VARCHAR(10),
    caballos VARCHAR(10),
    color VARCHAR(30),
    kilometros INTEGER DEFAULT 0,
    fecha_itv DATE,
    fecha_seguro DATE,
    notas_taller TEXT,                 -- Historial de intervenciones
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW(),
    UNIQUE(id_taller, matricula)
);

-- ============================================================
-- ÓRDENES DE TRABAJO (reemplaza a mantenimientos)
-- ============================================================
CREATE TABLE ordenes_trabajo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_taller UUID NOT NULL REFERENCES talleres(id),
    id_vehiculo UUID NOT NULL REFERENCES vehiculos(id),
    id_cliente UUID REFERENCES clientes(id),
    id_mecanico_asignado UUID REFERENCES usuarios(id),
    estado VARCHAR(20) NOT NULL DEFAULT 'PRESUPUESTO'
        CHECK (estado IN ('PRESUPUESTO','APROBADO','EN_CURSO','COMPLETADO','FACTURADO','CANCELADO')),
    fecha_entrada TIMESTAMP DEFAULT NOW(),
    fecha_salida_estimada DATE,
    fecha_completado TIMESTAMP,
    kilometros_entrada INTEGER,
    descripcion_problema TEXT,
    diagnostico TEXT,
    presupuesto_total DECIMAL(10,2),
    total_real DECIMAL(10,2),
    notas_internas TEXT,
    notas_cliente TEXT,
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- DETALLES DE TRABAJO (líneas de una orden)
-- ============================================================
CREATE TABLE detalles_trabajo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_orden_trabajo UUID NOT NULL REFERENCES ordenes_trabajo(id),
    id_tipo_mantenimiento INTEGER,    -- Referencia al enum compartido (nullable)
    descripcion TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE','EN_CURSO','COMPLETADO','CANCELADO')),
    precio_estimado DECIMAL(10,2),
    precio_real DECIMAL(10,2),
    tiempo_estimado INTEGER,          -- minutos
    tiempo_real INTEGER,              -- minutos
    notas_mecanico TEXT,
    orden INTEGER DEFAULT 0
);

-- ============================================================
-- INVENTARIO / ARTÍCULOS
-- ============================================================
CREATE TABLE articulos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_taller UUID NOT NULL REFERENCES talleres(id),
    nombre VARCHAR(200) NOT NULL,
    sku VARCHAR(50),
    categoria VARCHAR(100),
    cantidad_stock INTEGER DEFAULT 0,
    stock_minimo INTEGER DEFAULT 0,
    precio_venta DECIMAL(10,2),
    precio_coste DECIMAL(10,2),
    proveedor VARCHAR(200),
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- PIEZAS UTILIZADAS (en cada detalle de trabajo)
-- ============================================================
CREATE TABLE piezas_utilizadas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_detalle_trabajo UUID NOT NULL REFERENCES detalles_trabajo(id),
    id_articulo UUID NOT NULL REFERENCES articulos(id),
    cantidad INTEGER NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10,2) NOT NULL
);

-- ============================================================
-- CITAS / AGENDA
-- ============================================================
CREATE TABLE citas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_taller UUID NOT NULL REFERENCES talleres(id),
    id_cliente UUID REFERENCES clientes(id),
    id_vehiculo UUID REFERENCES vehiculos(id),
    id_mecanico UUID REFERENCES usuarios(id),
    fecha_hora TIMESTAMP NOT NULL,
    duracion_estimada INTEGER DEFAULT 60,  -- minutos
    motivo TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE','CONFIRMADA','EN_TALLER','COMPLETADA','CANCELADA')),
    notas TEXT,
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- FACTURAS
-- ============================================================
CREATE TABLE facturas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_taller UUID NOT NULL REFERENCES talleres(id),
    id_orden_trabajo UUID REFERENCES ordenes_trabajo(id),
    numero_factura VARCHAR(50) NOT NULL,
    fecha_emision DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_vencimiento DATE,
    base_imponible DECIMAL(10,2) NOT NULL,
    iva DECIMAL(5,2) NOT NULL DEFAULT 21.00,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'EMITIDA'
        CHECK (estado IN ('EMITIDA','PAGADA','ANULADA','VENCIDA')),
    metodo_pago VARCHAR(30),
    notas TEXT,
    pdf_url TEXT,
    creado_en TIMESTAMP DEFAULT NOW(),
    UNIQUE(id_taller, numero_factura)
);

-- ============================================================
-- ARCHIVOS ADJUNTOS (fotos, documentos, firmas)
-- ============================================================
CREATE TABLE archivos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_taller UUID NOT NULL REFERENCES talleres(id),
    entidad_tipo VARCHAR(50) NOT NULL,    -- 'orden_trabajo', 'vehiculo', 'detalle_trabajo'
    entidad_id UUID NOT NULL,
    nombre_archivo VARCHAR(200),
    tipo_mime VARCHAR(100),
    ruta VARCHAR(500) NOT NULL,
    tamano_bytes INTEGER,
    creado_en TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- CONFIGURACIÓN DEL TALLER
-- ============================================================
CREATE TABLE configuracion_taller (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_taller UUID UNIQUE NOT NULL REFERENCES talleres(id),
    nombre_taller VARCHAR(200),
    direccion TEXT,
    telefono VARCHAR(20),
    email VARCHAR(200),
    web VARCHAR(200),
    tipo_iva DECIMAL(5,2) DEFAULT 21.00,
    divisa VARCHAR(3) DEFAULT 'EUR',
    pie_pagina_factura TEXT,
    logo_url TEXT,
    created_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW()
);
```

### 4.3 Migración desde el Schema Actual

El proceso de migración tomará datos de las tablas actuales y los insertará en el nuevo schema:

| Tabla actual | Nueva tabla | Notas |
|-------------|-------------|-------|
| `motos` | `vehiculos` | Se crea un cliente genérico "Sin asignar" para motos sin cliente |
| `mantenimientos` | `ordenes_trabajo` + `detalles_trabajo` | Cada mantenimiento se convierte en una orden con un detalle |
| `fotos_moto` | `archivos` | `entidad_tipo = 'vehiculo'` |
| `fotos_mantenimiento` | `archivos` | `entidad_tipo = 'orden_trabajo'` |

---

## 5. Fases de Implementación

### Fase 0 — Servidor Python: Fundación (4-6 semanas)

| # | Tarea | Descripción |
|---|-------|-------------|
| 0.1 | Crear proyecto Python | Estructura de carpetas, `pyproject.toml`, dependencias |
| 0.2 | Configuración base | Variables de entorno, conexión PostgreSQL, logging |
| 0.3 | Modelos SQLAlchemy | Todas las tablas del schema completo |
| 0.4 | Migraciones Alembic | Script de migración inicial con todas las tablas |
| 0.5 | Autenticación JWT | Registro de taller, login email/password, login PIN |
| 0.6 | Schemas Pydantic | Request/response para todas las entidades |
| 0.7 | CRUD Clientes | Endpoints REST completos |
| 0.8 | CRUD Vehículos | Endpoints REST completos |
| 0.9 | CRUD Órdenes de trabajo | Endpoints REST + cambios de estado |
| 0.10 | CRUD Artículos (inventario) | Endpoints REST |
| 0.11 | CRUD Citas (agenda) | Endpoints REST |
| 0.12 | CRUD Facturas | Endpoints REST + generación PDF |
| 0.13 | Dashboard | Endpoints para KPIs (órdenes activas, ingresos, etc.) |
| 0.14 | WebSocket | Notificaciones en tiempo real (estado de órdenes) |
| 0.15 | Tests | Tests de autenticación, CRUD principal |
| 0.16 | Migración de datos | Script para importar desde SQLite actual a PostgreSQL |

### Fase 1 — Desktop conectado al servidor (6-8 semanas)

| # | Tarea | Descripción |
|---|-------|-------------|
| 1.1 | HTTP Client | Añadir Ktor Client (o OkHttp) al módulo `:desktop` |
| 1.2 | API Client Kotlin | Capa de cliente API para todos los endpoints |
| 1.3 | Auth en desktop | Login screen + almacenamiento de token JWT |
| 1.4 | Clientes screen | Lista + detalle + formulario de clientes |
| 1.5 | Vehículos screen | Lista + detalle + formulario de vehículos |
| 1.6 | Órdenes screen | Lista + detalle + seguimiento de estado |
| 1.7 | Agenda screen | Calendario de citas |
| 1.8 | Caché local SQLDelight | Almacenamiento offline para datos frecuentes |
| 1.9 | Dashboard real | KPIs desde el servidor |

### Fase 2 — Desktop funcionalidades avanzadas (4-6 semanas)

| # | Tarea | Descripción |
|---|-------|-------------|
| 2.1 | Inventario screen | CRUD de artículos + control de stock |
| 2.2 | Facturación | Generar factura desde orden de trabajo |
| 2.3 | PDF en servidor | Presupuestos y facturas desde API |
| 2.4 | Dashboard KPIs | Órdenes activas, ingresos del mes, carga de mecánicos |
| 2.5 | Informes | Histórico cliente, rentabilidad, etc. |

### Fase 3 — Mobile como herramienta de mecánico (6-8 semanas)

| # | Tarea | Descripción |
|---|-------|-------------|
| 3.1 | Login PIN | Autenticación con PIN para mecánicos |
| 3.2 | Trabajos asignados | Lista desde servidor |
| 3.3 | Diagnosis + fotos | Cámara, firma digital, adjuntar archivos |
| 3.4 | Presupuestos móvil | Crear y enviar presupuesto desde móvil |
| 3.5 | Fichaje | Check-in/check-out de vehículos |
| 3.6 | Escáner matrícula | ML Kit para buscar vehículo |
| 3.7 | Sync offline | Room → API → PostgreSQL |
| 3.8 | Tiempo real | WebSocket: cambios instantáneos |

### Fase 4 — Profesionalización (4-6 semanas)

| # | Tarea | Descripción |
|---|-------|-------------|
| 4.1 | Roles y permisos | Admin, mecánico, recepción |
| 4.2 | Auditoría | Quién hizo qué y cuándo |
| 4.3 | Push notifications | Mecánico recibe nueva asignación |
| 4.4 | Backup automático | A la nube |
| 4.5 | Exportación | Excel, CSV |
| 4.6 | Multi-idioma | Español, inglés, catalán/euskera/gallego |

---

## 6. API Endpoints

### Autenticación

```
POST /api/v1/auth/registro-taller       # Registrar nuevo taller (admin)
POST /api/v1/auth/login                  # email + password → JWT
POST /api/v1/auth/login-pin              # PIN para mecánicos → JWT
POST /api/v1/auth/refresh                # Refrescar token
GET  /api/v1/auth/me                     # Datos del usuario actual
```

### Clientes

```
GET    /api/v1/clientes                  # Lista (paginada, búsqueda)
POST   /api/v1/clientes                  # Crear
GET    /api/v1/clientes/{id}             # Detalle
PUT    /api/v1/clientes/{id}             # Actualizar
DELETE /api/v1/clientes/{id}             # Eliminar (soft)
GET    /api/v1/clientes/{id}/vehiculos   # Vehículos del cliente
GET    /api/v1/clientes/{id}/historial   # Órdenes de trabajo históricas
```

### Vehículos

```
GET    /api/v1/vehiculos                 # Lista (filtros: matricula, idCliente)
POST   /api/v1/vehiculos                 # Crear
GET    /api/v1/vehiculos/{id}            # Detalle
PUT    /api/v1/vehiculos/{id}            # Actualizar
DELETE /api/v1/vehiculos/{id}            # Eliminar
GET    /api/v1/vehiculos/buscar-matricula?q=   # Búsqueda por matrícula
```

### Órdenes de Trabajo

```
GET    /api/v1/ordenes                   # Lista (filtros: estado, idMecanico, fecha)
POST   /api/v1/ordenes                   # Crear
GET    /api/v1/ordenes/{id}              # Detalle
PUT    /api/v1/ordenes/{id}              # Actualizar
PATCH  /api/v1/ordenes/{id}/estado       # Cambiar estado
POST   /api/v1/ordenes/{id}/detalles     # Añadir detalle
PATCH  /api/v1/ordenes/detalles/{id}     # Actualizar detalle (estado, precio_real, etc.)
DELETE /api/v1/ordenes/detalles/{id}     # Eliminar detalle
POST   /api/v1/ordenes/{id}/pdf-presupuesto  # Generar PDF presupuesto
```

### Artículos (Inventario)

```
GET    /api/v1/articulos                 # Lista (filtros: categoria, stock bajo)
POST   /api/v1/articulos                 # Crear
GET    /api/v1/articulos/{id}            # Detalle
PUT    /api/v1/articulos/{id}            # Actualizar
DELETE /api/v1/articulos/{id}            # Eliminar
PATCH  /api/v1/articulos/{id}/stock      # Ajustar stock
```

### Citas (Agenda)

```
GET    /api/v1/citas                     # Lista (filtros: fecha, idMecanico)
POST   /api/v1/citas                     # Crear
PUT    /api/v1/citas/{id}                # Actualizar
PATCH  /api/v1/citas/{id}/estado         # Cambiar estado
DELETE /api/v1/citas/{id}                # Cancelar
```

### Facturas

```
GET    /api/v1/facturas                  # Lista
POST   /api/v1/facturas                  # Crear (desde orden)
GET    /api/v1/facturas/{id}             # Detalle
GET    /api/v1/facturas/{id}/pdf         # Descargar PDF
PATCH  /api/v1/facturas/{id}/estado      # Pagar, anular
```

### Dashboard

```
GET    /api/v1/dashboard/resumen         # KPIs principales
GET    /api/v1/dashboard/ordenes-activas  # Órdenes en curso hoy
GET    /api/v1/dashboard/proximas-citas   # Próximas citas
GET    /api/v1/dashboard/ingresos-mes     # Ingresos del mes actual
```

### Tiempo Real (WebSocket)

```
WS     /ws/taller/{id_taller}            # Conexión WebSocket del taller
                                          # Eventos:
                                          # - orden.creada
                                          # - orden.estado.cambiado
                                          # - detalle.completado
                                          # - cita.creada
                                          # - cita.cancelada
```

---

## 7. Flujo de Trabajo Típico

```
1. RECEPCIÓN (Desktop)
   ┌─ Llega cliente → buscar por matrícula/teléfono
   ├─ Si no existe → crear cliente + vehículo
   ├─ Crear orden de trabajo con problema descrito
   ├─ Asignar a mecánico
   ├─ Generar presupuesto (PDF) → cliente aprueba → orden pasa a APROBADO
   └─ Orden pasa a EN_CURSO → notificación al mecánico vía WebSocket

2. MECÁNICO (Mobile)
   ┌─ Abre app, introduce PIN
   ├─ Ve órdenes asignadas (sincronizadas offline)
   ├─ Selecciona una → ve diagnóstico pendiente
   ├─ Inspecciona vehículo, añade fotos, notas
   ├─ Si necesita piezas → solicita desde móvil (inventario)
   ├─ Marca detalles como COMPLETADO cuando termina
   └─ Taller ve el cambio en tiempo real (WebSocket)

3. CIERRE (Desktop)
   ┌─ Recepción: mecánico ha terminado
   ├─ Revisa trabajo, añade ajustes si necesario
   ├─ Genera factura (PDF)
   ├─ Cierra orden → COMPLETADO
   └─ Notifica al cliente (email/SMS)
```

---

## 8. Decisiones de Arquitectura

| Decisión | Opción A | Opción B | Elección | Motivo |
|----------|----------|----------|----------|--------|
| **Lenguaje servidor** | Kotlin (Ktor) | Python (FastAPI) | **Python** | Más ecosistema para negocio (facturación, PDF, contabilidad), más fácil de contratar |
| **Desktop connection** | Solo online (thin client) | Híbrido (caché local) | **Híbrido** | SQLDelight como caché, servidor como fuente de verdad |
| **Mobile sync** | Siempre online | Offline-first | **Offline-first** | Talleres tienen mala cobertura |
| **PDF generation** | En servidor (Python) | En desktop (PDFBox) | **Servidor** | Unifica formato, accesible desde ambos clientes |
| **Real-time** | Polling cada N seg | WebSocket | **WebSocket** | Cambios instantáneos mecánico↔taller |
| **Multi-tenant** | DB separada por taller | Fila tenant_id | **Fila tenant_id** | Más fácil de escalar, un solo PostgreSQL |
| **ID format** | Auto-increment INT | UUID | **UUID** | Previene conflictos en sync offline, seguro en API |
| **ORM** | SQLAlchemy (core) | SQLAlchemy (declarative) | **Declarative** | Más productivo, tipado con mypy |

---

## 9. Estado Actual del Código

### Módulo `:desktop` (Kotlin Compose Desktop) — COMPLETO (Fase 1 MVP)

| Archivo | Propósito | Líneas |
|---------|-----------|--------|
| `Main.kt` | Entry point, ventana 1200x800 | 26 |
| `App.kt` | Composable raíz, NavigationRail, navegación por estado | 68 |
| `navigation/Destinos.kt` | Sealed class `Screen` (DASHBOARD, GARAJE, MOTO_DETALLE, AJUSTES) | 8 |
| `data/DesktopRepositorio.kt` | Fachada CRUD sobre SQLDelight + mappers | 99 |
| `data/database/DatabaseFactory.kt` | Singleton driver SQLite en `~/.torque/` | ~60 |
| `sync/BackupImporter.kt` | Importar backup ZIP desde Android | ~120 |
| `ui/theme/Color.kt` | Paleta Material 3 oscura | ~50 |
| `ui/theme/Theme.kt` | TorqueThemeDesktop (dark/light) | ~80 |
| `ui/components/BotonTorque.kt` | Botón primario reutilizable | ~30 |
| `ui/components/TarjetaTorque.kt` | Card redondeada | ~15 |
| `ui/components/DialogoTorque.kt` | AlertDialog reutilizable | ~40 |
| `ui/components/CampoTextoTorque.kt` | OutlinedTextField temático | ~25 |
| `ui/components/EncabezadoSeccion.kt` | Texto uppercase azul | ~10 |
| `ui/screens/DashboardScreen.kt` | Placeholder | ~20 |
| `ui/screens/GarajeScreen.kt` | Placeholder | ~20 |
| `ui/screens/MotoDetalleScreen.kt` | Placeholder con idMoto | ~25 |
| `ui/screens/AjustesScreen.kt` | Placeholder | ~20 |
| `src/main/sqldelight/.../Torque.sq` | Schema SQL (4 tablas: motos, mantenimientos, fotos_moto, fotos_mantenimiento) | ~60 |
| Tests (2 archivos) | DesktopRepositorioTest (9 tests) + DatabaseFactoryTest (8 tests) | ~200 |

### Módulo `:shared` (JVM puro) — COMPLETO

| Archivo | Propósito |
|---------|-----------|
| `model/MotoData.kt` | DTO vehículo (23 campos) |
| `model/MantenimientoData.kt` | Interface |
| `model/MantenimientoDto.kt` | DTO mantenimiento (16 props) |
| `model/TipoMantenimiento.kt` | Tipos predefinidos (25), enum TipoVehiculo |
| `model/FotoData.kt` | Foto vehículo |
| `model/FotoMantenimientoData.kt` | Foto mantenimiento |
| `model/PlanData.kt` | Item plan mantenimiento |
| `model/ItvSeguroData.kt` | ITV y seguro |
| `model/SaludModels.kt` | DetalleSalud, ResultadoSalud |
| `algorithm/SaludMotoCalculator.kt` | Algoritmo de salud |
| `util/DateUtils.kt` | Constantes formato fecha |
| Tests (2 archivos) | SaludMotoCalculatorTest (30 tests) + DateUtilsTest |

### Lo que NO existe aún

- ❌ Servidor Python (FastAPI)
- ❌ Conexión de red en desktop (HTTP client)
- ❌ Login / autenticación en desktop
- ❌ Pantallas reales (solo placeholders)
- ❌ Clientes, órdenes de trabajo, facturación, inventario, agenda
- ❌ App móvil con sincronización
- ❌ Multi-tenant
- ❌ PDF generation
- ❌ WebSocket / tiempo real

---

## Siguiente: Fase 0

El servidor Python FastAPI se crea en `/home/vic/Torque.v2/server/` con la estructura descrita en la sección 3.

Comandos de inicio:
```bash
cd /home/vic/Torque.v2
mkdir -p server/app/{models,schemas,api/v1,services,core}
mkdir -p server/alembic/versions
mkdir -p server/tests
```
