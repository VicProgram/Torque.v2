---
name: localization
description: Use when extracting hardcoded strings to strings.xml, adding translations, or working with string resources for i18n in this project.
---

# Localization Conventions for Torque

## String Extraction Rules

- ALL user-facing text MUST be in `res/values/strings.xml` (Spanish) and `res/values-en/strings.xml` (English).
- Hardcoded strings in composables, toasts, dialogs, and notification text are forbidden.

## Resource Files

- `app/src/main/res/values/strings.xml` — Spanish (default locale).
- `app/src/main/res/values-en/strings.xml` — English translation.

When adding a new string, add it to BOTH files.

## Known Files Requiring Extraction

| File | Strings to extract | Priority |
|------|-------------------|----------|
| `AjustesActivity.kt` | ~15 (toasts, buttons, titles, restore confirmation) | 🟡 Medium |
| `PerfilActivity.kt` | ~12 (titles, placeholders, labels) | 🟡 Medium |
| `OpcionesMantenimiento.kt` | ~7 (menu items) | 🟡 Medium |
| `MantenimientoHistorial.kt` | ~7 (filter labels: "Todos", "Aceite", etc.) | 🟡 Medium |
| `RegistroMantenimiento.kt` | ~11 (chip labels, section titles) | 🟡 Medium |
| `NotificacionSalud.kt` | ~5 (notification text) | 🟡 Medium |
| `ListaGaraje.kt` | Various (filter labels, empty state) | 🟢 Low |
| `FormularioMantenimiento.kt` | Various (hints, button text) | 🟢 Low |
| `TorqueDashboardScreen.kt` | Various (labels, welcome text) | 🟢 Low |

## String Naming Convention

- Use snake_case: `titulo_ajustes`, `boton_guardar`, `toast_error_km`
- Group by screen/feature prefix: `perfil_nombre`, `perfil_email`, `ajustes_notificaciones`
- Use `%s` and `%d` placeholders for dynamic values

## Pattern for Extraction

```kotlin
// BAD — hardcoded
Toast.makeText(contexto, "Moto guardada", Toast.LENGTH_SHORT).show()

// GOOD — resource string
Toast.makeText(contexto, getString(R.string.moto_guardada), Toast.LENGTH_SHORT).show()
```

## Translation Format

- Maintain parallel structure between `values/` and `values-en/`.
- English strings use the same `name` attribute as Spanish.
