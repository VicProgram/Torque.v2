---
name: clean-architecture
description: Use when refactoring module boundaries, fixing dependency violations, or moving files between :app, :data, and :core modules to maintain clean architecture.
---

# Clean Architecture Conventions for Torque

## Module Dependency Rules

```
:app  →  :data  →  :core
:desktop  →  :shared  ←  :data
```

- `:shared` — Pure Kotlin JVM. Zero Android dependencies. Domain models (`MotoData`, `MantenimientoData`), algorithms (`SaludMotoCalculator`), utilities (`DateUtils`).
- `:core` — Zero Android dependencies. Contains domain types (`TiposMantenimiento`, `DateUtils`). Will eventually merge into `:shared`.
- `:data` — Can depend on `:core`, `:shared`, Room, DataStore, Lifecycle. Contains DB, repositories, backup logic.
- `:app` — Android UI. Depends on `:data` and `:core`.
- `:desktop` — Compose Desktop app. Depends on `:shared`. Never depends on Android modules.

**NEVER** allow `:data` to depend on `:app` or `:desktop`.  
**NEVER** allow `:shared` or `:core` to depend on Android framework classes.  
**NEVER** allow `:desktop` to depend on Android modules.

## Current Violations to Fix

### Files in wrong module

| File | Currently in | Should be in | Reason |
|------|-------------|-------------|--------|
| `ContextUtils.kt` | `:data` | `:app` | Uses `Activity.finish()` — Android coupling |
| `FileUtils.kt` | `:data` | `:app` | Uses `ContentResolver`, `OpenableColumns` — Android coupling |
| `PerfilRepository.kt` (copy in app) | `:app` | `:data` | Data layer concern (DataStore + crypto) |
| `Context.dataStore` extension | `:data` | `:app` | Android Context extension belongs in UI layer |

### Package naming

- Current: `components.ui.dashboard` → should be `ui.dashboard` (standard convention).
- Mixed Spanish/English names — keep Spanish as project convention.

## Repository Pattern

- `RepositorioApp` in `:data` is the single source of truth.
- UI never accesses DAOs directly.
- ViewModels use `RepositorioApp` (obtained via `remember { RepositorioApp.obtenerInstancia(contexto) }`).

## Koin DI

- Module defined in `TorqueApp.kt` (`:app`).
- Currently registers: `RepositorioApp` and `AjustesRepositorio`.
- New repos should be added to Koin rather than instantiated manually in composables.
