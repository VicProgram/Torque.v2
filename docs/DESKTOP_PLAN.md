# Plan: Version de Escritorio — Torque Desktop

Aplicacion de escritorio (Windows/Mac/Linux) para gestion de mantenimiento de motocicletas,
que sincroniza con la version Android mediante archivos de backup compatibles.

## Stack Tecnologico

| Componente | Tecnologia |
|------------|-----------|
| Lenguaje | Kotlin 2.1.20 (JVM) |
| UI | Compose Desktop 1.7.3 (Material 3) |
| Base de datos | SQLDelight 2.0.2 (SQLite) |
| PDF | Apache PDFBox (o similar) |
| Build | Gradle 8.12.1 |

## Fases

### Fase 0 — Refactor: Modulo `:shared` (logica comun)

Extraer toda la logica de dominio a un modulo JVM puro que compile tanto para Android como para Desktop.

**Archivos a crear/mover**:

| Origen | Destino | Estado |
|--------|---------|--------|
| `core/.../TiposMantenimiento.kt` | `shared/.../model/TipoMantenimiento.kt` | Pure Kotlin, sin cambios |
| `core/.../DateUtils.kt` | `shared/.../util/DateUtils.kt` | Pure Kotlin, sin cambios |
| `data/.../SaludMotoCalculator.kt` | `shared/.../algorithm/SaludMotoCalculator.kt` | Cambiar dependencia: `MantenimientoEntity` → `MantenimientoData` |
| Nuevo | `shared/.../model/MotoData.kt` | DTO sin anotaciones |
| Nuevo | `shared/.../model/MantenimientoData.kt` | DTO sin anotaciones |
| Nuevo | `shared/.../model/FotoData.kt` | DTO sin anotaciones |
| Nuevo | `shared/.../model/PlanData.kt` | DTO sin anotaciones |
| Nuevo | `shared/.../model/ItvSeguroData.kt` | DTO sin anotaciones |
| Nuevo | `shared/.../model/SaludModels.kt` | `DetalleSalud`, `ResultadoSalud` |

### Fase 1 — Desktop MVP (solo lectura + importar backup)

**Estructura del modulo `:desktop`**:

```
desktop/
├── build.gradle.kts
└── src/main/kotlin/com/example/torque/desktop/
    ├── Main.kt                    # Entry point (fun main())
    ├── App.kt                     # Composable raiz con navegacion
    ├── navigation/
    │   └── Destinos.kt            # Sealed class de rutas
    ├── ui/
    │   ├── theme/
    │   │   ├── Color.kt           # Paleta (copia de Android)
    │   │   └── Theme.kt           # TorqueThemeDesktop
    │   ├── components/            # Componentes reutilizables
    │   │   ├── BotonTorque.kt
    │   │   ├── TarjetaTorque.kt
    │   │   ├── DialogoTorque.kt
    │   │   ├── CampoTextoTorque.kt
    │   │   └── EncabezadoSeccion.kt
    │   └── screens/               # Pantallas (placeholders iniciales)
    │       ├── DashboardScreen.kt
    │       ├── GarajeScreen.kt
    │       ├── MotoDetalleScreen.kt
    │       └── AjustesScreen.kt
    ├── data/
    │   ├── DesktopRepositorio.kt   # Implementacion del repositorio
    │   └── database/
    │       └── DatabaseFactory.kt  # Creacion de SQLDelight
    └── sync/
        └── BackupImporter.kt       # Importar ZIP de Android
```

Schema SQLDelight:
```
desktop/src/main/sqldelight/com/example/torque/desktop/db/Torque.sq
```

**SQLDelight**: esquema identico al Room de Android (`motos`, `mantenimientos`, `fotos_moto`, `plan_mantenimiento`, `moto_itv`, `moto_seguro`).

### Fase 2 — Desktop funcionalidad completa

- CRUD completo: alta/edicion/eliminacion de motos y mantenimientos
- Plan de mantenimiento
- ITV y Seguro
- Perfil y ajustes
- Exportacion a PDF (multiplataforma)
- Exportar backup (para importar en Android)

### Fase 3 — Fotos

- Almacenamiento local en `~/.torque/fotos/`
- Visualizacion con Skia/ImageIO
- Sincronizacion de fotos via backup ZIP

### Fase 4 — Sync automatico (opcional)

- Servidor Ktor opcional en red local
- Sync bidireccional con deteccion de conflictos
- O soporte para Google Drive / Dropbox

## Arquitectura General

```
┌──────────────────────────────────────────────────────┐
│                    :shared (JVM)                      │
│  MotoData, MantenimientoData, SaludMotoCalculator,    │
│  TiposMantenimiento, DateUtils                        │
│  Sin dependencias de plataforma                       │
└──────┬─────────────────────────────────┬─────────────┘
       │ depends on                      │ depends on
┌──────▼──────────────┐    ┌─────────────▼─────────────┐
│   :data (Android)    │    │   :desktop (JVM/Desktop)   │
│   Room, DataStore    │    │   SQLDelight, Compose Des. │
│   BackupManager      │    │   BackupImporter          │
└──────────────────────┘    └───────────────────────────┘
           │                          │
           └─────── ZIP backup ───────┘
              (formato compatible)
```

## Sincronizacion (v1: Backup File Sync)

Android exporta un ZIP (`BackupManager.kt`) con:
```
torque_room.db         → Base de datos SQLite
fotos/                 → Archivos de imagen
preferencias/          → DataStore (ignorado en desktop)
```

Desktop lee el mismo ZIP y puebla su SQLDelight. Formato de DB compatible (mismo schema SQL).

## Decisiones de Arquitectura

1. **Compartir logica, no UI**: El modulo `:shared` tiene modelos y algoritmos. Cada plataforma tiene su propia UI y DB.
2. **Backup como protocolo de sync**: El formato ZIP del `BackupManager` Android es el contrato entre plataformas.
3. **SQLDelight en desktop**: Reemplazo natural de Room para JVM. Mismo schema SQL, mismo dialecto SQLite.
4. **Estado con mutableStateOf**: Sin ViewModel en desktop (no existe). Se usa `mutableStateOf` + `remember` para estado reactivo.
5. **Navegacion por estado**: Sin NavHost (no disponible en Desktop). Se usa `mutableStateOf<Screen>` con `when`.

## Roadmap Temporal

```
Semana 1-2:  Fase 0 — Modulo shared, migrar calculator, tests pasando
Semana 3-4:  Fase 1 — Config desktop + SQLDelight + BackupImporter
Semana 5-8:  Fase 1 — UI MVP (Dashboard + Garaje + Mantenimientos solo lectura)
Semana 9-12: Fase 2 — CRUD completo (formularios, plan, ajustes)
Semana 13-14: Fase 2 — Export backup, PDF export
Semana 15-16: Fase 3 — Fotos
Semana 17+:   Fase 4 — Sync automatico (opcional)
```

## Archivos del Proyecto

| Modulo | Archivos (est.) | Proposito |
|--------|-----------------|-----------|
| `:shared` | ~15 | Modelos, algoritmos, utilidades |
| `:desktop` | ~45 | UI, DB, sync, config |
| `:server` (opcional) | ~5 | Sync server Ktor |
| docs/ | 3 nuevos | Plan + guia + README desktop |
