---
name: clean-architecture
description: Use when refactoring module boundaries, fixing dependency violations, or moving files between :app, :data, and :core modules to maintain clean architecture.
---

# Clean Architecture Conventions for Torque

## Module Dependency Rules

```
:app  →  :data  →  :core
```

- `:core` — Zero Android dependencies. Only `androidx.core-ktx`. Contains domain types (`TiposMantenimiento`, `DateUtils`).
- `:data` — Can depend on `:core`, Room, DataStore, Lifecycle. Contains DB, repositories, calculators, backup logic.
- `:app` — Can depend on `:data` and `:core`. Contains Activities, Composables, theme, navigation.

**NEVER** allow `:data` to depend on `:app`.  
**NEVER** allow `:core` to depend on Android framework classes.

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
