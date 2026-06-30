# Torque

Aplicación Android para la gestión del mantenimiento de motocicletas. Desarrollada con **Kotlin**, **Jetpack Compose** y **Room**.

---

## Casos de Uso

### 🏍️ Para el motero diario
- Lleva el control del **cambio de aceite**, **filtros**, **cadena**, **neumáticos** y más
- Recibe notificaciones cuando un mantenimiento está **próximo a vencer** o **vencido**
- Consulta de un vistazo la **salud global** de tu moto (0–100%)
- **Plan de mantenimiento** con todas las tareas recomendadas, su estado y próximos vencimientos
- **Recordatorios de ITV y seguro** con avisos visuales en el panel

### 🏪 Para el taller
- Gestiona el **garaje completo** con múltiples motos
- Registra mantenimientos con **kilometraje**, **precio**, **tiempo**, **notas** y **fotos/facturas adjuntas**
- Ajusta el **desgaste de consumibles** con sliders intuitivos
- Exporta e importa **copias de seguridad** completas (DB + fotos + preferencias)
- **Exporta el historial a PDF** para entregar al cliente

### 👤 Para el propietario
- Perfil con foto, nombre, email, teléfono y fecha de nacimiento
- Carnets de conducir (AM, A1, A2, A)
- **Modo oscuro** optimizado para uso en taller y carretera
- **6 logos** seleccionables para personalizar la app
- **Fechas ITV y Seguro** registradas por moto

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
| **Plan de Mantenimiento** | Vista completa de todas las tareas recomendadas con estado, cumplimiento y próximo vencimiento |
| **ITV y Seguro** | Registro de fechas con indicadores visuales de vencimiento en el dashboard |
| **Exportación a PDF** | Generación de PDF del historial de mantenimiento para entregar al cliente |
| **Perfil de usuario** | Foto, nombre, email, teléfono, fecha de nacimiento y carnets |
| **Backup completo** | Exportación/importación ZIP con DB + fotos + preferencias |
| **Seguridad** | PII cifrada con AES-256-GCM via Android Keystore; backup deshabilitado; permisos mínimos |
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
| **Android Keystore** | AES-256-GCM (cifrado PII) |
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
│       │       ├── MantenimientoPlanEntity.kt
│       │       ├── MotoItvEntity.kt
│       │       ├── MotoSeguroEntity.kt
│       │       └── dao/
│       │           ├── MotoDao.kt
│       │           ├── MantenimientoDao.kt
│       │           ├── FotoDao.kt
│       │           ├── MantenimientoPlanDao.kt
│       │           └── DatosAnualesDao.kt
│       ├── ajustes/
│       │   └── AjustesRepository.kt
│       └── utils/
│           ├── SaludMotoCalculator.kt
│           ├── BackupManager.kt
│           ├── FileUtils.kt
│           ├── ContextUtils.kt
│           └── PdfExporter.kt
├── app/                      # Módulo de UI
│   └── src/main/java/.../
│       ├── TorqueApp.kt
│       ├── menuprincipal/
│       ├── pdf/                           # Exportación a PDF
│       ├── plan/                          # Plan de mantenimiento
│       ├── components/ui/dashboard/       # DashboardScreen + ViewModel + diálogos
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
>
> **Seguridad**: `android:allowBackup="false"` · solo 1 Activity exportada · `READ_EXTERNAL_STORAGE` eliminada · nombre/email/teléfono cifrados en reposo via Android Keystore (`AES/GCM/NoPadding`)

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

**Cobertura**: 78 tests de algoritmo de salud + 19 tests de plan de mantenimiento + 7 tests de exportación PDF + 9 tests de performance + 1 test de humo (114 unitarios) + 1 test instrumentado (115 en total).

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

## Seguridad

| Hallazgo | Estado | Solución |
|---|---|---|
| Backup sin restricciones | ✅ Corregido | `android:allowBackup="false"` |
| PII en texto plano | ✅ Corregido | Cifrado AES-256-GCM via Android Keystore (`CifradorPerfil`) |
| 10 Activities sin `exported` | ✅ Corregido | `android:exported="false"` (excepto launcher) |
| `READ_EXTERNAL_STORAGE` redundante | ✅ Corregido | Eliminado (sobra `READ_MEDIA_IMAGES`) |

---

## Licencia

Uso personal.
