---
name: kotlin-multiplatform
description: Use when setting up KMP modules, writing expect/actual declarations, sharing code between Android and Desktop, or configuring multiplatform builds.
---

# Kotlin Multiplatform Conventions for Torque

## Module Structure

```
Torque.v2/
├── shared/              # Kotlin JVM — logica de dominio compartida
│   └── build.gradle.kts
├── android/             # Android app (actual :app)
│   └── build.gradle.kts
├── desktop/             # Compose Desktop
│   └── build.gradle.kts
├── core/                # (obsoleto, se fusiona en shared)
└── data/                # Solo Android (Room, DataStore)
```

## Shared Module: `build.gradle.kts`

```kotlin
plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
}

dependencies {
    // No Android dependencies
    testImplementation("junit:junit:4.13.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
```

## What Goes in Shared

| Categoria | Contenido | Ejemplos |
|-----------|-----------|----------|
| **Modelos** | Data classes sin anotaciones de plataforma | `MotoData`, `MantenimientoData`, `FotoData`, `PlanData` |
| **Algoritmos** | Logica de negocio pura | `SaludMotoCalculator`, logica de plan, calculos de fechas |
| **Utilidades** | Funciones sin estado | `DateUtils`, validaciones, formateo |
| **Constantes** | Datos inmutables | `TiposMantenimiento.TODOS`, `WEAR_TIPOS` |

## What Stays Platform-Specific

| Plataforma | Tecnologia | Alternativa compartida |
|------------|-----------|----------------------|
| Android DB | Room | SQLDelight (desktop) |
| Desktop DB | SQLDelight | SQLDelight (desktop) |
| Android UI | Jetpack Compose (Android) | Compose Desktop |
| Desktop UI | Compose Desktop | Compose Desktop |
| Android Storage | Context.filesDir | Path en home del usuario |
| Desktop Storage | ~/.torque/ | Path en home del usuario |
| Android Encryption | Android KeyStore | java.security.KeyStore |
| Android Images | Coil | Skia/ImageIO |
| Android Notifications | NotificationManager | System tray (desktop) |

## Adapter Pattern for Platform Types

```kotlin
// shared: interfaz que ambas plataformas implementan
interface MantenimientoData {
    val tipoMantenimientoId: Long
    val nombreMantenimiento: String
    val kms: Int
    val intervaloKms: Int
    val fecha: String
    val porcentajeVida: Int
    val tareas: String
}

// shared: algoritmo usa la interfaz
fun calcular(mantenimientos: List<MantenimientoData>, kmActuales: Int): ResultadoSalud

// Android: Room entity implementa la interfaz
data class MantenimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // ...
) : MantenimientoData

// Desktop: modelo generado por SQLDelight no puede implementarla directamente,
// usar extension function:
fun com.example.torque.desktop.db.Mantenimiento.toMantenimientoData(): MantenimientoData = ...
```

## settings.gradle.kts

```kotlin
rootProject.name = "Torque.v2"
include(":app")
include(":core")
include(":data")
include(":shared")
include(":desktop")
```

## Version Catalog (gradle/libs.versions.toml)

Add to existing catalog:

```toml
[versions]
sqldelight = "2.0.2"
compose-desktop = "1.7.3"

[libraries]
sqldelight-sqlite-driver = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }

[plugins]
compose-desktop = { id = "org.jetbrains.compose", version.ref = "compose-desktop" }
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
```

## Detekt for Desktop Module

Add `desktop/` to existing detekt config. Consider adding desktop-specific rules if needed. The existing `CyclomaticComplexMethod.threshold = 20` applies to desktop too.
