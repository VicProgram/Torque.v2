# Torque

Aplicación Android para la gestión del mantenimiento de motocicletas. Desarrollada con **Kotlin**, **Jetpack Compose** y **Room**.

---

## Casos de Uso

### 🏍️ Para el motero diario
- Lleva el control del **cambio de aceite**, **filtros**, **cadena**, **neumáticos** y más
- Recibe notificaciones cuando un mantenimiento está **próximo a vencer** o **vencido**
- Consulta de un vistazo la **salud global** de tu moto (0–100%)

### 🏪 Para el taller
- Gestiona el **garaje completo** con múltiples motos
- Registra mantenimientos con **kilometraje**, **precio**, **tiempo** y **notas**
- Ajusta el **desgaste de consumibles** con sliders intuitivos
- Exporta e importa **copias de seguridad** completas (DB + fotos + preferencias)

### 👤 Para el propietario
- Perfil con foto, nombre, email, teléfono y fecha de nacimiento
- Carnets de conducir (AM, A1, A2, A)
- **Modo oscuro** optimizado para uso en taller y carretera
- **6 logos** seleccionables para personalizar la app

---

## Características

| Funcionalidad | Descripción |
|---|---|
| **Panel de control** | Carrusel de motos con swipe, salud global, kilometraje, próximo servicio y acciones rápidas — impulsado por ViewModel + StateFlow |
| **Gestión de garaje** | Añade, edita y elimina motos con foto, marca, modelo (~50 marcas con dropdown), año, matrícula, cilindrada y potencia |
| **Registro de mantenimiento** | 23 tipos predefinidos en 6 categorías (Motor, Transmisión, Frenos, Neumáticos, Eléctrico, Suspensión) |
| **Historial** | Búsqueda, filtros por categoría y detalle completo de cada registro |
| **Control de desgaste** | Sliders para neumáticos, pastillas de freno y discos |
| **Algoritmo de salud** | Cálculo ponderado (0–100%) basado en intervalos, peso y cumplimiento |
| **Notificaciones** | Alertas locales cuando un mantenimiento está al 1–20% o vencido al 0% |
| **Perfil de usuario** | Foto, nombre, email, teléfono, fecha de nacimiento y carnets |
| **Backup completo** | Exportación/importación ZIP con DB + fotos + preferencias |
| **Modo oscuro** | Tema oscuro nativo optimizado para taller |
| **Personalización** | 6 logos seleccionables desde ajustes |

---

## Stack Tecnológico

| Tecnología | Versión |
|---|---|
| **Kotlin** | 2.1.20 |
| **Jetpack Compose** | BOM 2025.05.01 |
| **Material Design 3** | — |
| **Room** | 2.6.1 |
| **DataStore Preferences** | 1.1.2 |
| **Coil** | 2.7.0 |
| **Koin** | 3.5.6 |
| **Lifecycle ViewModel Compose** | 2.8.7 |
| **Sentry** | 7.22.1 |
| **KSP** | 2.1.20-1.0.32 |
| **Gradle AGP** | 8.9.2 |
| **Gradle** | 8.12.1 |
| **Min SDK / Target** | 28 (Android 9) / 35 (Android 15) |

---

## Arquitectura

```
┌──────────────────────────────────────────┐
│               :app (UI)                   │
│  Activities · Composables · Theme         │
│  ViewModel + StateFlow en dashboard       │
│  Depende de: :core, :data                 │
└──────────────────┬───────────────────────┘
                   │
┌──────────────────▼───────────────────────┐
│              :data (Data)                 │
│  Room DB · DAOs · Entities · Repos       │
│  BackupManager · SaludMotoCalculator     │
│  Depende de: :core                        │
└──────────────────┬───────────────────────┘
                   │
┌──────────────────▼───────────────────────┐
│             :core (Domain)                │
│  TiposMantenimiento · DateUtils          │
│  Sin dependencias Android                 │
└──────────────────────────────────────────┘
```

### Estructura del proyecto

```
Torque.v2/
├── docs/                     # Documentación
│   ├── README.md             # Este archivo
│   └── GUIA_DEV.md           # Guía completa para desarrolladores
├── core/                     # Módulo de dominio
│   └── src/main/java/.../utils/
│       ├── TiposMantenimiento.kt
│       └── DateUtils.kt
├── data/                     # Módulo de datos
│   └── src/main/java/.../
│       ├── room/
│       │   ├── AppDatabase.kt
│       │   ├── AppRepository.kt
│       │   └── entity/
│       │       ├── MotoEntity.kt
│       │       ├── MantenimientoEntity.kt
│       │       ├── FotoEntity.kt
│       │       └── dao/
│       │           ├── MotoDao.kt
│       │           ├── MantenimientoDao.kt
│       │           └── FotoDao.kt
│       ├── ajustes/
│       │   └── AjustesRepository.kt
│       └── utils/
│           ├── SaludMotoCalculator.kt
│           ├── BackupManager.kt
│           ├── FileUtils.kt
│           └── ContextUtils.kt
├── app/                      # Módulo de UI
│   └── src/main/java/.../
│       ├── TorqueApp.kt
│       ├── menuprincipal/
│       ├── components/ui/dashboard/     # DashboardScreen + ViewModel + diálogos extraídos
│       ├── garage/
│       ├── maintenance/
│       ├── perfil/
│       ├── ajustes/
│       ├── ui/theme/
│       └── utils/
└── config/detekt/            # Reglas de análisis estático
```

---

## Cómo compilar

```bash
# APK de depuración
./gradlew assembleDebug
# → app/build/outputs/apk/debug/

# APK de release (requiere keystore.properties)
./gradlew assembleRelease
# → app/build/outputs/apk/release/

# Sincronizar Gradle manualmente
./gradlew --refresh-dependencies
```

> **Requisitos**: Android Studio Hedgehog+ · JDK 17 · Android SDK 35

---

## Tests

```bash
# Todos los tests unitarios
./gradlew test

# Solo tests de depuración
./gradlew testDebugUnitTest

# Tests instrumentados (requiere dispositivo/emulador)
./gradlew connectedCheck

# Tests de performance del algoritmo de salud
# (incluidos en testDebugUnitTest)
```

**Cobertura**: 61 tests unitarios + 9 tests de performance + 1 test instrumentado (71 en total).

---

## Análisis estático

```bash
./gradlew detekt
```

Reglas personalizadas en `config/detekt/detekt.yml`.

---

## Capturas de pantalla

*(pendiente)*

---

## Licencia

Uso personal.
