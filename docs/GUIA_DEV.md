# GUÍA PARA DESARROLLADORES — Torque v2

## Índice

1. [Descripción General](#1-descripción-general)
2. [Arquitectura](#2-arquitectura)
3. [Estructura del Proyecto](#3-estructura-del-proyecto)
4. [Módulo `:core` — Capa Compartida/Dominio](#4-módulo-core--capa-compartidadominio)
5. [Módulo `:data` — Capa de Datos](#5-módulo-data--capa-de-datos)
6. [Módulo `:app` — Capa de UI](#6-módulo-app--capa-de-ui)
7. [Base de Datos (Room)](#7-base-de-datos-room)
8. [Sistema de Diseño y Tema](#8-sistema-de-diseño-y-tema)
9. [Algoritmo de Salud](#9-algoritmo-de-salud)
10. [Sistema de Notificaciones](#10-sistema-de-notificaciones)
11. [Backup y Restauración](#11-backup-y-restauración)
12. [Navegación y Flujo de Pantallas](#12-navegación-y-flujo-de-pantallas)
13. [Inyección de Dependencias (Koin)](#13-inyección-de-dependencias-koin)
14. [Testing](#14-testing)
15. [Configuración del Build](#15-configuración-del-build)
16. [CI/CD](#16-cicd)
17. [Plan de Mantenimiento](#17-plan-de-mantenimiento)
18. [ITV y Seguro](#18-itv-y-seguro)
19. [Exportación a PDF](#19-exportación-a-pdf)
20. [Cómo Añadir un Nuevo Tipo de Mantenimiento](#20-cómo-añadir-un-nuevo-tipo-de-mantenimiento)
21. [Cómo Añadir un Nuevo Elemento al Plan](#21-cómo-añadir-un-nuevo-elemento-al-plan)
22. [Cómo Añadir una Nueva Pantalla](#22-cómo-añadir-una-nueva-pantalla)
23. [Seguridad](#23-seguridad)
24. [Convenciones de Código](#24-convenciones-de-código)
25. [Módulo Servidor (`server/`)](#25-módulo-servidor-server)
26. [Módulo Desktop Python (`desktop-python/`)](#26-módulo-desktop-python-desktop-python)
27. [Scripts de desarrollo multiplataforma](#27-scripts-de-desarrollo-multiplataforma)

---

## 1. Descripción General

**Torque** es una aplicación Android nativa para la gestión del mantenimiento de motocicletas. Permite:

- Gestionar un garaje virtual con múltiples motos (foto, marca, modelo, año, cilindrada, potencia, matrícula, estilo)
- Registrar mantenimientos de 23 tipos predefinidos en 6 categorías
- Monitorear la salud de cada moto mediante un algoritmo de cumplimiento ponderado (0–100%)
- Ajustar el desgaste de consumibles (neumáticos, pastillas de freno, discos) con sliders
- Recibir notificaciones locales cuando un mantenimiento está próximo a vencer o vencido
- Gestionar perfil de usuario (foto, nombre, email, teléfono, fecha nacimiento, carnets)
- Exportar/importar copia de seguridad completa (DB + fotos + preferencias)

**Stack**: Kotlin 2.1.20 + Jetpack Compose (BOM 2025.05.01) + Material 3 + Room 2.6.1 + Koin 3.5.6 + Coil 2.7.0 + Sentry 7.22.1
**Build**: Gradle 8.12.1 + AGP 8.9.2 + KSP 2.1.20-1.0.32
**Min SDK**: 28 (Android 9) | **Target SDK**: 35 (Android 15)

---

## 2. Arquitectura

```
┌──────────────────────────────────────────────────┐
│                  :app (UI Layer)                   │
│    Activities, Composables, Theme, Navegación      │
│    Depende de: :core, :data                        │
└──────────────────┬───────────────────────────────┘
                   │ depends on
┌──────────────────▼───────────────────────────────┐
│                :data (Data Layer)                  │
│    Room DB, DAOs, Entities, Repositories,          │
│    BackupManager, SaludMotoCalculator              │
│    Depende de: :core                               │
└──────────────────┬───────────────────────────────┘
                   │ depends on
┌──────────────────▼───────────────────────────────┐
│          :core (Domain/Shared Layer)               │
│    TiposMantenimiento, DateUtils                  │
│    Sin dependencias Android (solo core-ktx)        │
└──────────────────────────────────────────────────┘
```

### Principios aplicados:
- **Multi-módulo**: Separación clara de responsabilidades con dependencias unidireccionales
- **Repository Pattern**: `RepositorioApp` encapsula todos los accesos a datos
- **Activity-based navigation** combinado con **Compose Navigation** (NavHost)
- **DataStore Preferences** para settings de usuario y perfil
- **DI manual con Koin**: módulo simple de 2 singletons

---

## 3. Estructura del Proyecto

```
Torque.v2/
├── settings.gradle.kts           # Define 3 módulos: :app, :core, :data
├── build.gradle.kts              # Plugins globales (AGP, Kotlin, Compose, KSP, Detekt)
├── gradle.properties             # JVM args, AndroidX, Kotlin code style
├── gradle/
│   ├── libs.versions.toml        # Version catalog centralizado
│   └── wrapper/                  # Gradle 8.12.1 wrapper
│
├── core/                         # ← MÓDULO CORE
│   └── src/main/java/com/example/torque/utils/
│       ├── TiposMantenimiento.kt # 23 tipos, data class, lookup functions
│       └── DateUtils.kt          # Constantes de formato de fecha
│
├── data/                         # ← MÓDULO DATA
│   └── src/main/java/com/example/torque/
│       ├── room/
│   │           ├── AppDatabase.kt    # Room DB v4, migrations, singleton
│       │   ├── AppRepository.kt  # RepositorioApp: CRUD + flows
│       │   └── entity/
│       │       ├── MotoEntity.kt               # tabla: motos
│       │       ├── MantenimientoEntity.kt       # tabla: mantenimientos
│       │       ├── FotoEntity.kt               # tabla: fotos_moto
│       │       ├── MantenimientoPlanEntity.kt   # tabla: plan_mantenimiento
│       │       ├── MotoItvEntity.kt            # tabla: moto_itv
│       │       ├── MotoSeguroEntity.kt         # tabla: moto_seguro
│       │       └── dao/
│       │           ├── MotoDao.kt
│       │           ├── MantenimientoDao.kt
│       │           ├── FotoDao.kt
│       │           ├── MantenimientoPlanDao.kt
│       │           └── DatosAnualesDao.kt      # ITV + Seguro
│       ├── ajustes/
│       │   └── AjustesRepository.kt # DataStore: notificaciones, dark mode, logo
│       ├── perfil/
│       │   ├── PerfilRepository.kt  # DataStore del perfil (nombres, email, teléfono)
│       │   └── CifradorPerfil.kt    # Cifrado AES-256-GCM via Android Keystore
│       └── utils/
│           ├── SaludMotoCalculator.kt # Algoritmo de salud
│           ├── BackupManager.kt       # Export/import ZIP
│           ├── PdfExporter.kt         # Exportación a PDF (iText 5)
│           ├── FileUtils.kt           # Copia/compresión de imágenes
│           └── ContextUtils.kt        # Extension: Activity.finish()
│
├── app/                          # ← MÓDULO APP
│   ├── src/main/AndroidManifest.xml
│   ├── proguard-rules.pro
│   └── src/main/java/com/example/torque/
│       ├── TorqueApp.kt          # Application class (Sentry + Koin init)
│       ├── menuprincipal/
│       │   └── MenuPrincipal.kt  # Entry point, NavHost, splash screen
│       ├── pdf/                           # Exportación a PDF
│       │   └── PdfExportActivity.kt
│       ├── plan/                          # Plan de mantenimiento
│       │   ├── PlanMantenimientoScreen.kt
│       │   └── PlanMantenimientoViewModel.kt
│       ├── components/ui/dashboard/
│       │   ├── ColoresTorque.kt              # Objeto de colores + componentes reutilizables
│       │   ├── TorqueDashboardScreen.kt      # Dashboard (~550 líneas, usa ViewModel)
│       │   ├── TorqueDashboardViewModel.kt   # ViewModel con StateFlow<DashboardUiState>
│       │   ├── DialogoKm.kt                  # Diálogo edición de km
│       │   ├── DialogoNotificaciones.kt      # Diálogo de notificaciones pendientes
│       │   └── DialogoAjusteDesgaste.kt      # Sliders de desgaste
│       ├── garage/
│       │   ├── ListaGaraje.kt          # Lista de motos con búsqueda/filtro
│       │   ├── MotoDetalle.kt          # Detalle de moto + galería + acciones
│       │   ├── AnadirMotoActivity.kt   # Alta/edición de moto
│       │   ├── MarcasModelos.kt        # ~50 marcas + modelos, selectores dropdown
│       │   ├── DialogoActualizarKm.kt  # Diálogo edición de km (extraído de MotoDetalle)
│       │   ├── DialogoEliminarMoto.kt  # Diálogo confirmación eliminar moto
│       │   └── DialogoEliminarFoto.kt  # Diálogo confirmación eliminar foto
│       ├── maintenance/
│       │   ├── RegistroMantenimiento.kt     # Selección de tareas
│       │   ├── FormularioMantenimiento.kt   # Formulario (km, precio, notas)
│       │   ├── OpcionesMantenimiento.kt     # Menú: nuevo / historial
│       │   └── MantenimientoHistorial.kt    # Historial con ViewModel
│       ├── perfil/
│       │   ├── PerfilActivity.kt   # Perfil de usuario
│       │   └── PerfilRepository.kt # DataStore del perfil
│       ├── ajustes/
│       │   └── AjustesActivity.kt  # Settings: notifs, dark mode, logo, backup
│       ├── ui/theme/
│       │   ├── Color.kt            # Paleta de colores completa
│       │   └── Theme.kt            # Material3 dark/light theme
│       └── utils/
│           └── NotificacionSalud.kt # Sistema de notificaciones locales
│
├── app/src/test/.../
│   ├── ExampleUnitTest.kt
│   ├── SaludMotoCalculatorTest.kt        # 78 unit tests
│   └── SaludMotoCalculatorPerformanceTest.kt # 9 performance tests
│
└── app/src/androidTest/.../
    └── DashboardUITest.kt
```

---

## 4. Módulo `:core` — Capa Compartida/Dominio

**`build.gradle.kts`**: Solo depende de `androidx.core-ktx`. Sin acceso a Room, DataStore, ni UI.

### `TiposMantenimiento.kt` — `core/src/main/java/.../utils/TiposMantenimiento.kt`

Define los 23 tipos de mantenimiento con sus propiedades:

```kotlin
data class TipoMantenimiento(
    val id: Long,
    val nombre: String,
    val categoria: String,       // "Motor", "Transmisión", "Frenos", "Neumáticos", "Eléctrico", "Suspensión"
    val intervaloKms: Int,       // 0 si no aplica por km
    val intervaloDias: Int = 0,  // 0 si no aplica por días
    val peso: Int,               // Importancia relativa (1-10) para el cálculo de salud
    val esDesgaste: Boolean = false // true si es consumible (neumáticos, pastillas, discos)
)
```

**Categorías y tipos**:

| Categoría | Tipos | IDs |
|-----------|-------|-----|
| Motor | Aceite, Filtro Aceite, Bujías, Filtro Aire, Refrigerante, Válvulas | 1-6 |
| Transmisión | Cadena (limpieza, tensado, cambio) | 7-9 |
| Frenos | Pastillas D/T, Líquido, Discos D/T | 10-13, 23 |
| Neumáticos | Presión, Cambio, Neumático D/T | 14-15, 21-22 |
| Eléctrico | Batería, Luces, ECU | 16-18 |
| Suspensión | Delantera, Trasera | 19-20 |

**Wear types** (`WEAR_TIPOS`): 6 tipos marcados como `esDesgaste = true` (IDs: 10, 11, 13, 21, 22, 23)

**Funciones lookup**:
- `desdeNombre(nombre: String): TipoMantenimiento?` — lookup por nombre (case-insensitive, trim)
- `desdeId(id: Long): TipoMantenimiento?` — lookup por ID

### `DateUtils.kt` — `core/src/main/java/.../utils/DateUtils.kt`

Constantes de formato:
- `FORMATO_FECHA_VISUAL = "dd/MM/yyyy"` — para mostrar al usuario
- `FORMATO_FECHA_COMPLETA = "yyyy-MM-dd HH:mm:ss"` — para almacenar (creadoEn)

---

## 5. Módulo `:data` — Capa de Datos

**`build.gradle.kts`**: Depende de `:core`, Room, DataStore, Lifecycle.

### Room Database — `data/src/main/java/.../room/AppDatabase.kt`

```kotlin
@Database(
    entities = [MotoEntity::class, MantenimientoEntity::class, FotoEntity::class,
                MantenimientoPlanEntity::class, MotoItvEntity::class, MotoSeguroEntity::class],
    version = 4,
    exportSchema = false
)
abstract class BaseDeDatosApp : RoomDatabase() { ... }
```

**Migraciones**:
- `MIGRACION_1_2`: Añade `tipoMantenimientoId` a `mantenimientos`
- `MIGRACION_2_3`: Añade `porcentajeVida` a `mantenimientos`
- `MIGRACION_3_4`: Crea `plan_mantenimiento`, `moto_itv`, `moto_seguro`

**Singleton**: `obtenerInstancia(context)` con doble-check locking.
**Reset**: `cerrarYReiniciar()` cierra la instancia y la setea a null (usado en restore).

### Entidades

| Archivo | Tabla | Campos clave |
|---------|-------|-------------|
| `MotoEntity.kt` | `motos` | idMoto (PK), marca, modelo, anio, matricula, color, cilindrada, caballos, estilo, kilometros, fechaCompra, esPrincipal (0/1), uriFoto, creadoEn, actualizadoEn |
| `MantenimientoEntity.kt` | `mantenimientos` | id (PK), idMoto, nombreMantenimiento, fecha, kms, intervaloKms, precio, tiempo, notas, tareas, creadoEn, tipoMantenimientoId, porcentajeVida (100 = nuevo) |
| `FotoEntity.kt` | `fotos_moto` | id (PK), idMoto, ruta, creadoEn |
| `MantenimientoPlanEntity.kt` | `plan_mantenimiento` | id (PK), idMoto, nombreTarea, categoria, intervaloKms, intervaloDias, ultimoCumplimientoKm, realizado (0/1) |
| `MotoItvEntity.kt` | `moto_itv` | idMoto (PK, FK → motos), fechaItv, fechaRecordatorio |
| `MotoSeguroEntity.kt` | `moto_seguro` | idMoto (PK, FK → motos), fechaSeguro, fechaRecordatorio |

### DAOs

**`MotoDao`** — `data/src/main/java/.../room/entity/dao/MotoDao.kt`
- `insertar`, `actualizar`, `eliminar` — CRUD estándar
- `obtenerTodasFlow()` → `Flow<List<MotoEntity>>` ordenado por `creadoEn DESC`
- `obtenerPorId(id)` → suspenso
- `contarMotos()` → Int
- `obtenerPrincipalFlow()` → `Flow<MotoEntity?>` (esPrincipal = 1)
- `resetearPrincipales()` + `marcarComoPrincipal(id)` — transaccional en repositorio
- `actualizarKilometros(idMoto, kilometros)`

**`MantenimientoDao`** — `data/src/main/java/.../room/entity/dao/MantenimientoDao.kt`
- `insertar`, `eliminar`
- `obtenerPorMotoFlow(idMoto)` → Flow por fecha DESC
- `obtenerPorMoto(idMoto)` → List suspenso
- `obtenerUltimoPorTipo(idMoto, tipoId)` → último por kms DESC
- `actualizarPorcentajeVida(id, porcentaje)`
- `obtenerUltimoPorMoto(idMoto)` → último por fecha DESC (único)
- *Nota: `obtenerUltimosPorTipos` fue eliminado — los datos de desgaste ahora vienen del ViewModel*

**`FotoDao`** — `data/src/main/java/.../room/entity/dao/FotoDao.kt`
- `insertar`, `eliminar`
- `obtenerPorMotoFlow(idMoto)` → Flow por creadoEn DESC
- `obtenerPorIdMoto(idMoto)` → List suspenso

**`MantenimientoPlanDao`** — `data/src/main/java/.../room/entity/dao/MantenimientoPlanDao.kt`
- `insertar`, `actualizar`, `eliminar`
- `obtenerPorMotoFlow(idMoto)` → Flow<List<MantenimientoPlanEntity>>
- `obtenerPorId(id)` → suspenso

**`DatosAnualesDao`** — `data/src/main/java/.../room/entity/dao/DatosAnualesDao.kt`
- `insertarOActualizarItv(idMoto, fechaItv, fechaRecordatorio)` — UPSERT
- `obtenerItv(idMoto)` → MotoItvEntity?
- `obtenerItvFlow(idMoto)` → Flow<MotoItvEntity?>
- `insertarOActualizarSeguro(idMoto, fechaSeguro, fechaRecordatorio)` — UPSERT
- `obtenerSeguro(idMoto)` → MotoSeguroEntity?
- `obtenerSeguroFlow(idMoto)` → Flow<MotoSeguroEntity?>

### RepositorioApp — `data/src/main/java/.../room/AppRepository.kt`

Singleton que encapsula todos los DAOs. Es la **única puerta de entrada** a datos desde la capa UI.

Métodos principales:
- `obtenerMotosFlow()`, `insertarMoto()`, `actualizarMoto()`, `eliminarMoto()`, `obtenerMotoPorId()`, `contarMotos()`
- `obtenerMotoPrincipalFlow()`, `establecerMotoPrincipal(idMoto)` (transaccional)
- `obtenerMantenimientosPorMoto()` (Flow), `obtenerTodosMantenimientosPorMoto()` (List)
- `insertarMantenimiento()`, `eliminarMantenimiento()`
- `actualizarKilometros()`, `obtenerUltimoMantenimiento()`, `obtenerUltimoMantenimientoPorTipo()`
- `actualizarPorcentajeVida()`
- `obtenerFotosPorMoto()` (Flow), `insertarFoto()`, `eliminarFoto()`
- `obtenerPlanPorMotoFlow()` (Flow), `insertarTareaPlan()`, `actualizarTareaPlan()`, `eliminarTareaPlan()`
- `obtenerItv()`, `obtenerItvFlow()`, `guardarItv()`
- `obtenerSeguro()`, `obtenerSeguroFlow()`, `guardarSeguro()`
- `cerrarBaseDeDatos()` — usado en backup restore

**Manejo de errores**: Métodos de solo lectura tragan excepciones con `Log.e()`. Métodos críticos de escritura (`actualizarMoto`, `eliminarMoto`, `insertarMantenimiento`, `insertarFoto`, `eliminarFoto`) relanzan la excepción tras loguearla para que el llamante pueda reaccionar.

### AjustesRepository — `data/src/main/java/.../ajustes/AjustesRepository.kt`

DataStore Preferences con 3 claves:
- `notificaciones_activadas` (Boolean, default: true)
- `modo_oscuro_activado` (Boolean, default: true)
- `logo_seleccionado` (Int, default: 4)

El DataStore se define como extension property en Context:
```kotlin
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
```

### SaludMotoCalculator — `data/src/main/java/.../utils/SaludMotoCalculator.kt`

(Los detalles del algoritmo están en la [Sección 9](#9-algoritmo-de-salud))

### BackupManager — `data/src/main/java/.../utils/BackupManager.kt`

(Los detalles están en la [Sección 11](#11-backup-y-restauración))

### ArchivoUtils — `data/src/main/java/.../utils/FileUtils.kt`

Utilidad para copiar imágenes del content provider al almacenamiento interno:
- `copiarImagenAInterno(contexto, uri, comprimir=true)` → `String?` (ruta absoluta)
- Compresión JPEG al 75% si `comprimir = true`
- Obtiene nombre original del archivo via `OpenableColumns.DISPLAY_NAME`

### ContextUtils — `data/src/main/java/.../utils/ContextUtils.kt`

```kotlin
fun Activity.terminar() {
    if (!isFinishing) finish()
}
```

---

## 6. Módulo `:app` — Capa de UI

### TorqueApp.kt — `app/src/main/java/.../TorqueApp.kt`

Application class. Inicializa:
1. **Sentry**: solo si el DSN no es placeholder
2. **Koin**: módulo con `RepositorioApp` (singleton) y `AjustesRepositorio` (singleton)

```kotlin
val appModule = module {
    single { RepositorioApp.obtenerInstancia(get()) }
    single { AjustesRepositorio(get<android.content.Context>().dataStore) }
}
```

### AndroidManifest.xml

- 11 Activities declaradas (10 con `android:exported="false"`, solo launcher con `exported="true"`)
- Permisos: `READ_MEDIA_IMAGES`, `POST_NOTIFICATIONS` (se eliminó `READ_EXTERNAL_STORAGE` — sobra desde API 33)
- `android:allowBackup="false"` — deshabilitado para evitar extracción de DB y DataStore via adb
- Launcher Activity: `MenuPrincipal` con splash theme
- Sentry auto-init deshabilitado (se inicializa manualmente)

### MenuPrincipal.kt — `app/src/main/java/.../menuprincipal/MenuPrincipal.kt`

Activity principal con:
1. `installSplashScreen()` — splash screen API de Android 12+
2. `enableEdgeToEdge()` — modo edge-to-edge
3. `TorqueTheme(darkMode)` — tema dinámico desde DataStore
4. `TorqueNavHost()` — NavHost con una sola ruta `dashboard`

El NavHost renderiza `TorqueDashboardScreen` y maneja la navegación hacia otras Activities via Intents.

### TorqueDashboardScreen.kt — `app/src/main/java/.../components/ui/dashboard/TorqueDashboardScreen.kt`

(~550 líneas, refactorizado) Es el **corazón de la UI**. Usa `TorqueDashboardViewModel` con `StateFlow<DashboardUiState>` para todos los datos reactivos.

Incluye un **carrusel de motos** con `HorizontalPager` + indicador de puntos: permite deslizar entre todas las motos del garaje (la principal primero). Solo la moto visible tiene datos de salud en vivo. La cabecera usa `ColoresTorque.ALTURA_CABECERA = 56.dp`.

**DashboardUiState**:
- `motos: List<MotoEntity>` — todas las motos ordenadas (principal primero)
- `selectedIndex: Int` — índice en el carrusel
- `motoActual: MotoEntity?` — moto visible actualmente
- `nombreUsuario: String` — nombre del perfil
- `kmActuales: Int` — kilómetros actuales
- `resultadoSalud: ResultadoSalud?` — salud calculada
- `motivosProximosVencidos: List<DetalleSalud>` — notificaciones pendientes
- `porcentajesDesgaste: Map<Long, Int>` — desgaste de consumibles (key = tipoMantenimientoId)
- `itv: MotoItvEntity?` — datos ITV de la moto actual
- `seguro: MotoSeguroEntity?` — datos seguro de la moto actual
- `cargando: Boolean` — estado inicial

**ViewModel** (`TorqueDashboardViewModel.kt`):
- Factory propia para `AndroidViewModel` (sin Koin)
- Observa `obtenerTodasFlow()` y `perfilRepositorio.nombreFlow`
- Expone `actualizarKilometros(km)`, `seleccionarMoto(indice)`, y `guardarAjustesDesgaste(idMoto, porcentajes, onComplete)`

**Composables en pantalla**:
- `CabeceraDashboard` — top bar + menús
- `TarjetaProximoServicio` — tarjeta principal con foto, km, salud
- `TarjetaAccionRapida` — botones de acción rápida

**Diálogos extraídos a archivos separados**:
- `DialogoKm.kt` — edición de km (usa `DialogoTorque`)
- `DialogoNotificaciones.kt` — notificaciones pendientes (usa `DialogoTorque` con `showDismiss = false`)
- `DialogoAjusteDesgaste.kt` — sliders de desgaste (6 wear types, recibe `initialPorcentajes` del ViewModel, sin queries DB, usa `DialogoTorque`)

### ColoresTorque.kt — `app/src/main/java/.../components/ui/dashboard/ColoresTorque.kt`

Objeto que re-exporta los colores del tema + componentes reutilizables:

| Componente | Descripción |
|------------|-------------|
| `ColoresTorque` | Objeto con todas las referencias de color |
| `ImagenDeFondo` | Fondo con overlay de opacidad |
| `TarjetaTorque` | Card con estilo consistente |
| `CampoTextoTorque` | OutlinedTextField con colores unificados |
| `EncabezadoDeSeccion` | Título de sección en uppercase |
| `BotonTorque` | Botón primary full-width con esquinas redondeadas |
| `FiltroChip` | Chip seleccionable (usado en listas) |
| `ChipDeSeleccionRapida` | Chip con icono |
| `ElementoCheckMotoKeep` | Fila checkeable con icono, título y descripción |
| `DialogoTorque` | Diálogo reutilizable sobre `AlertDialog` con `confirmColor` (rojo para eliminar), `showDismiss`, `title`, `icon`, `onConfirm`, `confirmText`, `dismissText`, `content` |
| `ALTURA_CABECERA` | Constante `56.dp` para altura unificada de cabeceras en 10 pantallas |
| `navegarA`, `lanzarActividad`, `mostrarToast` | Helpers de navegación |

### Pantallas de Garage

**`ListaGaraje.kt`** — Listado de motos con:
- Búsqueda por marca/modelo
- Filtro: "Todas", "En Uso", "Históricas"
- Tarjetas con foto, modelo, marca/año, estado salud
- FAB para añadir moto
- `TarjetaMoto`: card clickeable que navega a `MotoDetalle`

**`MotoDetalle.kt`** — Detalle de moto:
- Foto grande (click para añadir desde galería)
- Especificaciones: marca, año, matrícula, cilindrada, potencia, km, estilo
- Galería de fotos (LazyRow)
- Dropdown menu: marcar principal, registrar mantenimiento, ver historial, editar, eliminar
- Diálogos extraídos a archivos separados (usando `DialogoTorque`):
  - `DialogoActualizarKm.kt` — edición de km
  - `DialogoEliminarMoto.kt` — confirmación de eliminación (botón rojo via `confirmColor`)
  - `DialogoEliminarFoto.kt` — confirmación de eliminar foto (botón rojo)

**`AnadirMotoActivity.kt`** — Formulario de alta/edición:
- Campos: marca (req, dropdown), modelo (req, dropdown filtrado por marca), año (dropdown), matrícula, cilindrada, caballos, estilo, km, foto
- Los dropdowns de marca y modelo usan `ExposedDropdownMenuBox` con datos de `MarcasModelos` (~50 marcas, cientos de modelos)
- Se puede escribir directamente si la marca/modelo no está en la lista
- Switch "moto principal"
- Edición: recibe `idMoto` y `editando=true` via Intent extras
- Validación: marca y modelo obligatorios
- Diálogo de descarte si hay cambios sin guardar

**`MarcasModelos.kt`** — Datos de marcas y modelos de motos (~50 marcas):
- `MarcasModelos.TODAS`: `Map<String, List<String>>` con todas las marcas y sus modelos
- `MarcasModelos.MARCAS`: lista ordenada de nombres de marca
- `Anios.RANGO`: `List<Int>` de 1990 a 2027
- `SelectorMarca`, `SelectorModelo`, `SelectorAnio`: composables `ExposedDropdownMenuBox` reutilizables con filtrado por escritura y tema oscuro

### Pantallas de Mantenimiento

**`RegistroMantenimiento.kt`** — Selección de tareas:
- 22 tareas visuales en 6 categorías (cada una con icono y color)
- Búsqueda por nombre
- Filtro por categoría (chips)
- Bottom sheet con contador de seleccionadas + botón "Continuar"
- Pasa selección a `FormularioMantenimiento` via `putStringArrayListExtra`

**`FormularioMantenimiento.kt`** — Formulario de registro:
- Campos: km actuales (req), intervalo (cada X km), precio, tiempo, notas
- Si es tipo desgaste: slider de porcentaje de vida
- Al guardar: si km > km actuales de la moto, pregunta si actualizar
- Guarda `MantenimientoEntity` con `tipoMantenimientoId` si es 1 sola tarea

**`OpcionesMantenimiento.kt`** — Menú intermedio:
- Muestra la moto seleccionada
- Opciones: "Nuevo Registro" → `RegistroMantenimiento`, "Ver Historial" → `MantenimientoHistorial`, "Plan de Mantenimiento" → `PlanMantenimiento`

**`MantenimientoHistorial.kt`** — Historial con ViewModel:
- `MantenimientoHistorialViewModel`: AndroidViewModel con StateFlow
- Filtros rápidos: Todos, Aceite, Filtro, Neumático, Cadena, Frenos
- Lista de registros con icono dinámico según tipo
- Click → Dialog de detalle (nombre, fecha, km, notas, tareas, precio)
- Swipe/long press → eliminar
- FAB para nuevo registro

### Perfil

**`CifradorPerfil.kt`** — Cifrado de datos personales en reposo:
- Usa AES-256-GCM con clave generada en Android Keystore (`KeyGenParameterSpec`)
- `cifrar(texto)` → Base64(IV + ciphertext); `descifrar(cifrado)` → texto original
- `descifrar()` tiene fallback silencioso para datos legacy no cifrados (migración automática)
- Sin dependencias externas — usa `KeyStore` + `Cipher` del JDK

**`PerfilRepository.kt`** — DataStore con 5 claves:
- `perfil_nombre`, `perfil_email`, `perfil_telefono` → cifrados con `CifradorPerfil`
- `perfil_foto`, `perfil_fecha_nacimiento_iso` → sin cifrar (rutas de archivo, no PII sensible)

**`PerfilActivity.kt`** — Pantalla de perfil:
- Foto circular (click para cambiar desde galería, usando `ArchivoUtils.copiarImagenAInterno`)
- Modo edición: campos de texto para nombre, email, teléfono, fecha nacimiento
- Modo vista: filas con iconos
- Sección "MIS CARNETS": AM, A1, A2, A (hardcoded, decorativo)

### Ajustes

**`AjustesActivity.kt`** — Pantalla de configuración:
- Switch: Notificaciones activadas
- Switch: Modo oscuro
- Selector de logo (6 logos en fila, con borde primario si seleccionado)
- Exportar copia de seguridad (`BackupManager.exportar` via `CreateDocument`)
- Restaurar copia de seguridad (`BackupManager.restaurar` via `OpenDocument` + confirmación)
- Post-restore: `finishAffinity()` para reiniciar la app
- Exportar historial a PDF (lanzador hacia `PdfExportActivity`; ver [§19](#19-exportación-a-pdf))

**Plan de Mantenimiento**: pantalla [`PlanMantenimiento`] (`app/src/main/java/.../plan/`) con ViewModel que itera `TiposMantenimiento.TODOS` y muestra estado de cada tarea (ver [§17](#17-plan-de-mantenimiento)).

**ITV y Seguro**: se editan desde `MotoDetalle.kt` con DatePicker y se persisten en `moto_itv`/`moto_seguro` mediante `DatosAnualesDao`. El dashboard muestra indicadores de vencimiento visual (ver [§18](#18-itv-y-seguro)).

**PDF Export**: `PdfExporter.kt` con `android.graphics.pdf.PdfDocument` (API nativa), lanzado desde `PdfExportActivity` via `CreateDocument` (ver [§19](#19-exportación-a-pdf)).

---

## 7. Base de Datos (Room)

### Esquema

```sql
-- Tabla: motos
CREATE TABLE motos (
    idMoto INTEGER PRIMARY KEY AUTOINCREMENT,
    marca TEXT NOT NULL,
    modelo TEXT NOT NULL,
    anio INTEGER NOT NULL DEFAULT 0,
    matricula TEXT,
    color TEXT,
    cilindrada TEXT,
    caballos TEXT,
    estilo TEXT,
    kilometros INTEGER NOT NULL DEFAULT 0,
    fechaCompra TEXT,
    esPrincipal INTEGER NOT NULL DEFAULT 0,
    uriFoto TEXT,
    creadoEn TEXT,
    actualizadoEn TEXT
);

-- Tabla: mantenimientos
CREATE TABLE mantenimientos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    idMoto INTEGER NOT NULL,
    nombreMantenimiento TEXT NOT NULL,
    fecha TEXT NOT NULL,
    kms INTEGER NOT NULL DEFAULT 0,
    intervaloKms INTEGER NOT NULL DEFAULT 0,
    precio TEXT,
    tiempo TEXT,
    notas TEXT,
    tareas TEXT,
    creadoEn TEXT,
    tipoMantenimientoId INTEGER NOT NULL DEFAULT 0,
    porcentajeVida INTEGER NOT NULL DEFAULT 100
);

-- Tabla: fotos_moto
CREATE TABLE fotos_moto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    idMoto INTEGER NOT NULL,
    ruta TEXT NOT NULL,
    creadoEn TEXT
);

-- Tabla: plan_mantenimiento
CREATE TABLE plan_mantenimiento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    idMoto INTEGER NOT NULL,
    nombreTarea TEXT NOT NULL,
    categoria TEXT NOT NULL,
    intervaloKms INTEGER NOT NULL DEFAULT 0,
    intervaloDias INTEGER NOT NULL DEFAULT 0,
    ultimoCumplimientoKm INTEGER NOT NULL DEFAULT 0,
    realizado INTEGER NOT NULL DEFAULT 0
);

-- Tabla: moto_itv
CREATE TABLE moto_itv (
    idMoto INTEGER PRIMARY KEY,
    fechaItv TEXT,
    fechaRecordatorio TEXT,
    FOREIGN KEY (idMoto) REFERENCES motos(idMoto) ON DELETE CASCADE
);

-- Tabla: moto_seguro
CREATE TABLE moto_seguro (
    idMoto INTEGER PRIMARY KEY,
    fechaSeguro TEXT,
    fechaRecordatorio TEXT,
    FOREIGN KEY (idMoto) REFERENCES motos(idMoto) ON DELETE CASCADE
);
```

### Singleton Pattern

Tanto `BaseDeDatosApp` como `RepositorioApp` usan double-check locking con `@Volatile`:
```kotlin
companion object {
    @Volatile private var INSTANCE: BaseDeDatosApp? = null
    fun obtenerInstancia(context: Context): BaseDeDatosApp =
        INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(...).build().also { INSTANCE = it }
        }
}
```

### Migraciones

- **v1 → v2**: `ALTER TABLE mantenimientos ADD COLUMN tipoMantenimientoId INTEGER NOT NULL DEFAULT 0`
- **v2 → v3**: `ALTER TABLE mantenimientos ADD COLUMN porcentajeVida INTEGER NOT NULL DEFAULT 100`
- **v3 → v4**: Creación de `plan_mantenimiento`, `moto_itv`, `moto_seguro`

```kotlin
val MIGRACION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE plan_mantenimiento (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idMoto INTEGER NOT NULL,
                nombreTarea TEXT NOT NULL,
                categoria TEXT NOT NULL,
                intervaloKms INTEGER NOT NULL DEFAULT 0,
                intervaloDias INTEGER NOT NULL DEFAULT 0,
                ultimoCumplimientoKm INTEGER NOT NULL DEFAULT 0,
                realizado INTEGER NOT NULL DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE moto_itv (
                idMoto INTEGER PRIMARY KEY,
                fechaItv TEXT,
                fechaRecordatorio TEXT,
                FOREIGN KEY (idMoto) REFERENCES motos(idMoto) ON DELETE CASCADE
            )
        """)
        db.execSQL("""
            CREATE TABLE moto_seguro (
                idMoto INTEGER PRIMARY KEY,
                fechaSeguro TEXT,
                fechaRecordatorio TEXT,
                FOREIGN KEY (idMoto) REFERENCES motos(idMoto) ON DELETE CASCADE
            )
        """)
    }
}
```

---

## 8. Sistema de Diseño y Tema

### Colores — `app/src/main/java/.../ui/theme/Color.kt`

Paleta oscura (el modo claro es secundario, el app usa oscuro por defecto):

| Variable | Color | Uso |
|----------|-------|-----|
| `TorquePrimary` | `#135BEC` | Azul primario de marca |
| `TorqueAccent` | `#FF6B00` | Naranja acento |
| `TorqueBackground` | `#101622` | Fondo principal oscuro |
| `TorqueSurfaceDark` | `#1C2331` | Superficie de tarjetas |
| `TorqueBorderDark` | `#2D3748` | Bordes |
| `TorquePrimaryContainer` | `#4F378B` | Contenedor primario |
| `TorqueSecondaryContainer` | `#4A4458` | Contenedor secundario |
| `TorqueSurfaceContainer` | `#211F26` | Contenedor de superficie |
| `RojoError` | `#EF4444` | Semántico error |
| `VerdeExito` | `#22C55E` | Semántico éxito |
| `AmarilloAdvertencia` | `#EAB308` | Semántico advertencia |

### Theme — `app/src/main/java/.../ui/theme/Theme.kt`

- `TorqueTheme(darkMode: Boolean = true, content)`: wrapper de `MaterialTheme`
- Usa `darkColorScheme` / `lightColorScheme`
- Tipografía personalizada (bodyLarge, titleLarge, labelSmall)
- Shapes: small (8dp), medium (16dp), large (24dp)

### Componentes Reutilizables — `ColoresTorque.kt`

Ver tabla en [Sección 6 - ColoresTorque](#colorestorquekt).

---

## 9. Algoritmo de Salud

**Archivo**: `data/src/main/java/.../utils/SaludMotoCalculator.kt`

### Modelos de datos

```kotlin
data class DetalleSalud(
    val tipo: TipoMantenimiento,
    val ultimoKms: Int?,        // km del último registro (si existe)
    val cumplimiento: Int,      // 0-100%
    val relevancia: Int         // (peso * (100 - cumplimiento)) / 100
)

data class ResultadoSalud(
    val saludGlobal: Int,           // 0-100%
    val desglose: List<DetalleSalud>,  // todos los tipos
    val proximosVencidos: List<DetalleSalud>  // top 5 más relevantes
)
```

### Algoritmo (`calcular(mantenimientos, kmActuales)`)

> **Optimización**: Pre-agrupa los mantenimientos por `tipoMantenimientoId` en un `Map<Long, List<>>` (O(N)), luego para cada tipo hace lookup O(1). Solo los registros sin ID (multi-tarea o legacy) caen en fallback por nombre/tareas. Reduce O(23×N) a prácticamente O(N+23).

1. **Para cada `TipoMantenimiento`** en `TiposMantenimiento.TODOS`:
   - Busca el último registro que coincida (por `tipoMantenimientoId`, nombre, o campo `tareas`)
   - Usa el de mayor `kms` (compara varios registros del mismo tipo)
   - **Si no hay registro**: `cumplimiento = 100` (asume que está OK)
   - **Si es tipo desgaste**: `cumplimiento = porcentajeVida` del último registro
   - **Si es tipo normal**:
     - `cumplimientoKm` = `1 - (kmActuales - ultimo.kms) / intervaloKms`
     - `cumplimientoDias` = `1 - (días desde último / intervaloDias)`
     - `cumplimientoFinal` = `min(cumplimientoKm, cumplimientoDias)` (el peor de los dos)
     - Se acota a [0, 1] y se multiplica por 100

2. **Cálculo de salud global**:
   - `pesoTotal = suma de pesos de todos los tipos`
   - `saludGlobal = suma(tipo.peso * tipo.cumplimiento) / pesoTotal`

3. **Próximos vencidos**: filtra `cumplimiento < 100`, ordena por `relevancia` descendente, top 5.

### Coincidencia de registros (`coincide`)

```kotlin
private fun coincide(mantenimiento: MantenimientoEntity, tipo: TipoMantenimiento): Boolean {
    if (mantenimiento.tipoMantenimientoId == tipo.id) return true           // match exacto por ID
    if (nombre.equals(tipo.nombre, ignoreCase = true)) return true          // match por nombre
    if (tareas.split(",").any { it.trim().equals(tipo.nombre, ...) }) return true  // match por campo tareas
}
```

### Parseo de fechas

Acepta dos formatos: `dd/MM/yyyy` y `yyyy-MM-dd`.

---

## 10. Sistema de Notificaciones

**Archivo**: `app/src/main/java/.../utils/NotificacionSalud.kt`

### Funcionamiento

1. Se llama a `verificarYNotificar(contexto, resultadoSalud, kmActuales, motoNombre)` desde el `LaunchedEffect` del dashboard
2. Crea el canal de notificación `mantenimiento_vencimiento`
3. Compara contra un Set de ya-notificados almacenado en DataStore bajo la clave `notificaciones_enviadas`
4. Para cada `DetalleSalud`:
   - Ignora si es desgaste o si no tiene intervalo
   - Si `cumplimiento` está entre 1 y 20: notificación **"próximo a vencer"** (solo si no se había notificado antes)
   - Si `cumplimiento` es 0: notificación **"vencido"** (solo si no se había notificado antes)
5. Actualiza el set de notificados en DataStore

### IDs de notificación

- Warning: `tipoId * 10 + 1`
- Expired: `tipoId * 10 + 2`

Esto asegura que no se solapen notificaciones. Cada tipo puede tener una warning y una expired.

---

## 11. Backup y Restauración

**Archivo**: `data/src/main/java/.../utils/BackupManager.kt`

### Exportación (`exportar`)

1. Hace WAL checkpoint en la DB (`PRAGMA wal_checkpoint(FULL)`)
2. Crea un ZIP temporal en cacheDir con:
   - `torque_room.db` → archivo de la base de datos
   - `fotos/` → todos los archivos en `context.filesDir`
   - `preferencias/` → todos los archivos en `datastore/` (ruta relativa al parent de filesDir)
3. Copia el ZIP al URI proporcionado por `contentResolver.openOutputStream`

### Restauración (`restaurar`)

1. Copia el ZIP desde el URI a cacheDir
2. Extrae en un directorio temporal
3. Cierra la base de datos (`RepositorioApp.cerrarBaseDeDatos()`)
4. Reemplaza:
   - Archivo de DB (eliminando archivos WAL/SHM previos)
   - Archivos de fotos
   - Archivos de DataStore
5. Elimina el directorio temporal

### Uso en UI

- Export: `ActivityResultContracts.CreateDocument("application/zip")` → llama a `BackupManager.exportar`
- Import: `ActivityResultContracts.OpenDocument()` → confirmación → `BackupManager.restaurar` → `finishAffinity()`

---

## 12. Navegación y Flujo de Pantallas

```
MenuPrincipal (Activity + NavHost)
├── Dashboard (TorqueDashboardScreen) ← ruta "dashboard"
│   ├── → Garaje (ListaGaraje)
│   │   ├── → MotoDetalle (idMoto)
│   │   │   ├── → AnadirMotoActivity (idMoto, editando=true)
│   │   │   ├── → RegistroMantenimiento (idMoto)
│   │   │   ├── → MantenimientoHistorial (idMoto)
│   │   │   ├── → PlanMantenimientoScreen (idMoto)
│   │   │   └── → [Diálogo ITV/Seguro] (inline)
│   │   └── → AnadirMotoActivity (nueva)
│   ├── → OpcionesMantenimiento (idMoto opcional)
│   │   ├── → RegistroMantenimiento (idMoto)
│   │   └── → MantenimientoHistorial (idMoto)
│   ├── → PerfilActivity
│   └── → AjustesActivity
│       └── Backup (CreateDocument / OpenDocument)
└── [Ajustes de desgaste] (diálogo inline)
    └── → FormularioMantenimiento (tareas[], idMoto)
```

**Patrón**: Las pantallas "principales" son Activities independientes. La navegación se hace con `Intent` + extras. El `NavHost` solo contiene la ruta del dashboard. Cada Activity tiene su propio `setContent` con `TorqueTheme`.

---

## 13. Inyección de Dependencias (Koin)

**Archivo**: `app/src/main/java/.../TorqueApp.kt`

```kotlin
val appModule = module {
    single { RepositorioApp.obtenerInstancia(get()) }
    single { AjustesRepositorio(get<android.content.Context>().dataStore) }
}
```

- Solo **2 singletons** registrados
- `RepositorioApp` se obtiene en los composables con `remember { RepositorioApp.obtenerInstancia(contexto) }` (no vía Koin injection directamente en UI, aunque el singleton está en Koin)
- `AjustesRepositorio` igualmente se instancia en composables con `remember { AjustesRepositorio(contexto.dataStore) }`
- `PerfilRepositorio` no está en Koin — se instancia directamente donde se necesita
- `TorqueDashboardViewModel` usa su propio `Factory` (patrón `viewModel { }`) en lugar de `koinViewModel()` para evitar añadir `koin-androidx-viewmodel` como dependencia

**Nota**: Aunque Koin está configurado, muchos repositorios se instancian manualmente en los composables. Esto es un área potencial de refactor para centralizar en Koin.

---

## 14. Testing

### Tests Unitarios (JUnit 4)

**`ExampleUnitTest.kt`** — Placeholder.

**`SaludMotoCalculatorTest.kt`** — 78 tests del algoritmo de salud:

| # | Test | Verifica |
|---|------|----------|
| 1 | Sin mantenimientos → salud 100 | Caso base |
| 2 | Todos recién hechos → salud 100 | Cumplimiento al 100% |
| 3 | 50% del intervalo → cumplimiento 50% | Cálculo lineal |
| 4 | Vencido (150% intervalo) → cumplimiento 0 | Saturación a 0 |
| 5 | Desgaste con 30% → cumplimiento 30% | Modo desgaste |
| 6 | Desgaste sin registro → 100% | Default |
| 7 | Múltiples registros → usa max kms | Agregación |
| 8 | Coincidencia por campo `tareas` | Fallback matching |
| 9 | Escenario mixto → salud en rango | Integración |
| 10 | Próximos vencidos ordenados | Relevancia descendente |
| 11-12 | `desdeNombre` lookup | Case insensitive, null |
| 13 | `desdeId` lookup | Correcto, null |
| 14 | `WEAR_TIPOS` contenido | 6 tipos esperados |
| 15 | Sin intervalo → cumplimiento 100% | Borde |
| 16-33 | Varios bordes y combinaciones | Robustez |
| 34-38 | Data integrity (IDs 1-25 sin saltos, pesos > 0, wear consistency) | Consistencia datos |
| 39 | Salud ponderada con diferentes pesos | Weighted average |
| 40-45 | Wear types — todos al 0%, todos al 100%, wear + intervalo | Casos desgaste |
| 46-49 | `proximosVencidos` — max 5, orden, incluye wear, vacío | Notificaciones |
| 50-55 | Matching — multi-tarea commas, espacios, null, nombre fallback, ID exacto | Coincidencia |
| 56-61 | Boundary — km < último, legacy matching, relevancia 0, IDs duplicados | Robustez |

Para ejecutar:
```bash
./gradlew testDebugUnitTest
```

**`SaludMotoCalculatorPerformanceTest.kt`** — 9 tests de performance:

### Tests de Plan de Mantenimiento

**`PlanMantenimientoLogicTest.kt`** — 20 tests de lógica del plan de mantenimiento:

| Test | Verifica |
|------|----------|
| Tarea sin registro | Estado SIN_DATOS, cumplimiento 100 |
| Tarea recién hecha | Estado OK, cumplimiento 100 |
| Tarea al 50% | Estado OK, cumplimiento 50 |
| Tarea al 15% (próximo) | Estado PROXIMO |
| Tarea al 5% (próximo) | Estado PROXIMO |
| Tarea al 1% (próximo) | Estado PROXIMO |
| Tarea vencida al 0% | Estado VENCIDO |
| Tarea muy vencida | Estado VENCIDO |
| Desgaste con 30% | Cumplimiento 30, OK |
| Desgaste al 15% | Estado PROXIMO |
| Desgaste al 0% | Estado VENCIDO |
| Múltiples registros | Usa el de mayor kms |
| Próximo km calculado | 5000 + 5000 = 10000 |
| Sin intervalo km | No tiene próximoKm |
| Desgaste sin próximoKm | Null |
| Km actual menor que último | 100% cumplimiento |
| Todos los tipos producen resultado válido | Cumplimiento 0-100 |
| Tipo sin intervalo ni desgaste sin registro | SIN_DATOS |
| Tipo sin intervalo con registro | OK, 100% |

### Tests de Exportación PDF

**`PdfExporterDataTest.kt`** — 7 tests de datos de exportación PDF:

| Test | Verifica |
|------|----------|
| Próximo servicio con intervalo | Muestra km futuro (10000) |
| Próximo servicio sin intervalo | Muestra "—" |
| Fila con datos completos | Fecha, nombre, km, precio, próximo, notas |
| Fila con nulos | Precio "—", notas vacío |
| Fecha larga truncada | Solo 10 caracteres |
| Resumen registros y gasto | 3 registros, 60.0€ |
| Precios en diferentes formatos | "45€", "120,50€" → 165.5€ |

### Tests Instrumentados

| Test | Condición | Límite |
|------|-----------|--------|
| Cálculo normal | 23 tipos, 100 iteraciones | < 10ms media |
| 1000 registros | 10 iteraciones | < 100ms |
| 10000 registros | 5 iteraciones | < 500ms |
| Sin degradación | 100 llamadas | Máx < 10x mediana |
| 50 llamadas vacío | Sin datos | < 50ms total |
| Memoria | 500 llamadas | < 2MB leak |
| Wear types mixtos | 6 wear + 17 normales, 100 iteraciones | < 10ms media |
| Datos mixtos grandes | 500 wear + 2000 normales, 10 iteraciones | < 100ms |
| 2500 registros | 5 iteraciones | < 200ms |

### Tests Instrumentados

**`DashboardUITest.kt`** — 2 tests:
- Verifica que el paquete es correcto
- Verifica que el dashboard muestra texto de bienvenida

Para ejecutar:
```bash
./gradlew connectedCheck  # Requiere emulador/dispositivo
```

---

## 15. Configuración del Build

### Version Catalog — `gradle/libs.versions.toml`

Todas las versiones centralizadas. Versiones clave:
- Kotlin 2.1.20, AGP 8.9.2, Gradle 8.12.1
- Compose BOM 2025.05.01, Room 2.6.1, Koin 3.5.6
- Sentry 7.22.1, Coil 2.7.0, DataStore 1.1.2

### `app/build.gradle.kts`

- `applicationId = "com.example.torque"`
- `minSdk = 28`, `targetSdk = 35`, `compileSdk = 35`
- `versionCode = 1`, `versionName = "1.0"`
- Release: minification + shrinking + ProGuard + signing (desde `keystore.properties`)
- Detekt config desde `${rootDir}/config/detekt/detekt.yml`
- BuildConfig field: `SENTRY_DSN` (placeholder por defecto)

### ProGuard — `app/proguard-rules.pro`

Reglas para: Room entities, Compose, Coroutines, DataStore, Sentry, Koin.

### Detekt — `config/detekt/detekt.yml`

Reglas deshabilitadas: `LongMethod`, `LongParameterList`, `LargeClass`, `MagicNumber`, etc.
`CyclomaticComplexMethod` configurado con `threshold = 20` para tolerar la complejidad natural de composables Compose.

---

## 16. CI/CD

**Archivo**: `.github/workflows/ci.yml`

Pipeline:
1. JDK 17 setup
2. Detekt analysis
3. Unit tests (`./gradlew test`)
4. Build debug APK (`./gradlew assembleDebug`)
5. Upload APK como artifact

---

## 17. Plan de Mantenimiento

**Archivo UI**: `app/src/main/java/.../plan/PlanMantenimientoScreen.kt`
**ViewModel**: `app/src/main/java/.../plan/PlanMantenimientoViewModel.kt`
**Entidad**: `data/src/main/java/.../room/entity/MantenimientoPlanEntity.kt`
**DAO**: `data/src/main/java/.../room/entity/dao/MantenimientoPlanDao.kt`

### Entidad

```kotlin
@Entity(tableName = "plan_mantenimiento")
data class MantenimientoPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idMoto: Long,
    val nombreTarea: String,
    val categoria: String,
    val intervaloKms: Int = 0,
    val intervaloDias: Int = 0,
    val ultimoCumplimientoKm: Int = 0,
    val realizado: Boolean = false
)
```

### Funcionamiento

- Cada moto tiene su propio plan con tareas predefinidas (insertadas al crear la moto)
- Categorías: "Motor", "Transmisión", "Frenos", "Neumáticos", "Eléctrico", "Suspensión"
- El usuario marca tareas como realizadas; al hacerlo se actualiza `ultimoCumplimientoKm` con los km actuales de la moto
- El ViewModel filtra por categoría usando chips y expone `StateFlow<PlanUiState>`
- El cómputo del próximo vencimiento se hace en UI: `proximoKm = ultimoCumplimientoKm + intervaloKms`

### PlanUiState

```kotlin
data class PlanUiState(
    val tareas: List<MantenimientoPlanEntity> = emptyList(),
    val filtro: String = "Todo",
    val kmActuales: Int = 0
)
```

---

## 18. ITV y Seguro

**Entidades**: `data/src/main/java/.../room/entity/MotoItvEntity.kt`, `MotoSeguroEntity.kt`
**DAO**: `data/src/main/java/.../room/entity/dao/DatosAnualesDao.kt`

### Entidades

```kotlin
@Entity(tableName = "moto_itv")
data class MotoItvEntity(
    @PrimaryKey val idMoto: Long,
    val fechaItv: String?,
    val fechaRecordatorio: String?
)

@Entity(tableName = "moto_seguro")
data class MotoSeguroEntity(
    @PrimaryKey val idMoto: Long,
    val fechaSeguro: String?,
    val fechaRecordatorio: String?
)
```

### Funcionamiento

- Cada moto tiene **una fila** en cada tabla (PK = idMoto)
- Las fechas se editan desde `MotoDetalle.kt` mediante un diálogo con `DatePicker`
- `DatosAnualesDao` usa `INSERT OR REPLACE` (UPSERT) para guardar
- El dashboard obtiene `MotoItvEntity` y `MotoSeguroEntity` vía Flow y muestra indicadores:
  - Verde: al día (faltan +30 días)
  - Amarillo: próximo a vencer (1-30 días)
  - Rojo: vencido
- `fechaRecordatorio` es un campo reservado para futuras notificaciones automáticas

---

## 19. Exportación a PDF

**Archivo**: `data/src/main/java/.../utils/PdfExporter.kt`
**Activity**: `app/src/main/java/.../pdf/PdfExportActivity.kt`

### PdfExporter

Usa la API nativa de Android `android.graphics.pdf.PdfDocument` (sin dependencias externas).

```kotlin
object PdfExporter {
    fun exportarHistorial(
        context: Context,
        moto: MotoEntity,
        mantenimientos: List<MantenimientoEntity>,
        uri: Uri
    )
}
```

Genera un documento PDF con:
1. **Cabecera**: texto "Torque - Historial de Mantenimiento" con la fecha de generación
2. **Datos de la moto**: marca, modelo, año, matrícula, km actuales
3. **Tabla de mantenimientos**: columnas Fecha, Tarea, Km, Intervalo, Precio, Tiempo, Notas
4. **Resumen**: total de registros y total gastado (suma de precios)

### PdfExportActivity

- Recibe `idMoto` por Intent
- Recupera la moto y sus mantenimientos del repositorio
- Lanza `CreateDocument("application/pdf")` para que el usuario elija dónde guardar
- Exporta usando `PdfExporter.exportarHistorial`
- Muestra toast de éxito o error y finaliza

---

## 20. Cómo Añadir un Nuevo Tipo de Mantenimiento

1. **Añadir a `TiposMantenimiento.kt`** — `core/src/main/java/.../utils/TiposMantenimiento.kt`
   - Agregar un nuevo `TipoMantenimiento(id, nombre, categoria, intervaloKms, intervaloDias, peso, esDesgaste)` a la lista `TODOS`
   - El `id` debe ser único (test #21 verifica esto) y en orden secuencial
   - Si es desgaste, añadirlo también a `WEAR_TIPOS`
2. **Añadir a `RegistroMantenimiento.kt`** — `app/src/main/java/.../maintenance/RegistroMantenimiento.kt`
   - Agregar una `Tarea(...)` en la lista de la categoría correspondiente
   - Si es una categoría nueva, crear la lista y añadirla a `secciones`
3. **Añadir filtro en `MantenimientoHistorial.kt`** (opcional) — si se quiere filtrar por el nuevo tipo

---

## 21. Cómo Añadir un Nuevo Elemento al Plan

1. **Añadir tarea por defecto** — en `PlanMantenimientoScreen.kt`, localiza la lista de tareas por defecto (función `tareasPorDefecto`) y agrega una nueva entrada:
   ```kotlin
   Triple("Nombre Tarea", "Categoría", intervaloKms)
   ```
2. **La categoría debe coincidir** con una existente ("Motor", "Transmisión", "Frenos", "Neumáticos", "Eléctrico", "Suspensión") para que el filtro funcione
3. **No requiere migración DB** — las nuevas tareas se insertan al crear la moto
4. Si se necesita añadir la tarea a motos existentes, agregar lógica en la migración o en un `OnCreate` callback

---

## 22. Cómo Añadir una Nueva Pantalla

1. Crear el archivo en el paquete correspondiente dentro de `app/src/main/java/.../`
2. Crear la clase `ComponentActivity` con `setContent { TorqueTheme { MiPantalla() } }`
3. Declarar la Activity en `AndroidManifest.xml`
4. Agregar el Intent de navegación desde donde corresponda:
   ```kotlin
   contexto.startActivity(Intent(contexto, MiPantalla::class.java).apply {
       putExtra("idMoto", idMoto)
   })
   ```
5. Si necesita acceso a datos, obtener `RepositorioApp` con `remember { RepositorioApp.obtenerInstancia(contexto) }`

---

## 23. Seguridad

### AndroidManifest

| Medida | Detalle |
|---|---|
| `android:allowBackup="false"` | Previene extracción de DB + DataStore via `adb backup` |
| `android:exported="false"` en 10/11 Activities | Solo `MenuPrincipal` (launcher) es exportada |
| `READ_EXTERNAL_STORAGE` eliminado | `READ_MEDIA_IMAGES` (API 33+) cubre la selección de fotos |

### Cifrado de PII en reposo

**Archivo**: `data/src/main/java/.../perfil/CifradorPerfil.kt`

Las claves `perfil_nombre`, `perfil_email` y `perfil_telefono` del DataStore de perfil se almacenan cifradas:

```
cifrar:  texto → Cipher(AES/GCM/NoPadding, ENCRYPT_MODE) → IV(12) + ciphertext → Base64
descifrar: Base64 → IV(12) + ciphertext → Cipher(AES/GCM/NoPadding, DECRYPT_MODE, KeystoreKey) → texto
```

- Algoritmo: **AES-256-GCM** con IV de 12 bytes y tag de 128 bits
- Clave generada en **Android Keystore** (`KeyGenParameterSpec`) — no exportable, hardware-backed si el dispositivo lo soporta
- Sin dependencias externas: usa `java.security.KeyStore` + `javax.crypto.Cipher`
- Migración automática: `descifrar()` captura excepciones y retorna el texto intacto si el valor legacy no estaba cifrado

### Actividades exportadas

Todas las Activities internas (10 de 11) tienen `android:exported="false"`. Solo `MenuPrincipal` (launcher con `MAIN`/`LAUNCHER`) es exportada. Esto elimina la ambigüedad en API 28–30 y evita intents externos no deseados.

---

## 24. Convenciones de Código

### Estilo
- Kotlin con nombres en español (el proyecto fue desarrollado para un público hispanohablante)
- Nombres de archivos en PascalCase (ej. `MotoDetalle.kt`)
- Nombres de funciones y variables en camelCase
- Layouts con Jetpack Compose (sin XML de layouts)
- Sin ViewBinding ni findViewById — todo Compose
- Sin fragments — solo Activities + Composables

### Patrones
- **Singleton**: para DB, Repository, Calculator, BackupManager
- **Flow**: para datos reactivos desde Room y DataStore
- **StateFlow**: en ViewModel (MantenimientoHistorial, TorqueDashboardViewModel)
- **LaunchedEffect/remember**: para lifecycle y estado en composables
- **rememberCoroutineScope**: para lanzar corrutinas desde callbacks de UI

### Manejo de errores
- Los métodos del repositorio atrapan excepciones y retornan valores por defecto (-1, null, emptyList)
- Los errores se muestran con `Toast`
- Sentry para crash reporting en producción

### Internacionalización
- Strings en `res/values/strings.xml` (referenciados con `stringResource(R.id.xxx)`)
- Nombres de strings en inglés snake_case (ej. `my_garage_title`, `btn_save_maintenance`)
- El contenido de los strings está en español

### Temas oscuro/claro
- Por defecto: dark mode activado
- Cambia dinámicamente desde `AjustesActivity` escribiendo en DataStore
- El dashboard lee `modoOscuroActivadoFlow` en el `setContent` de `MenuPrincipal`

### Manejo de imágenes
- Coil para carga asíncrona (`rememberAsyncImagePainter`)
- `ArchivoUtils.copiarImagenAInterno` para copiar y comprimir imágenes al storage interno
- Fotos almacenadas como archivos en `context.filesDir` con ruta absoluta en DB
- Sin ContentResolver persistente (se copia al interno)

### Manejo de permisos
- `POST_NOTIFICATIONS`: solicitado en runtime (Android 13+) vía `rememberLauncherForActivityResult` en el dashboard
- `READ_MEDIA_IMAGES`: declarado en manifest (para selección de fotos de galería, API 33+)
- `READ_EXTERNAL_STORAGE`: eliminado — redundante desde API 33

---

## 25. Módulo Servidor (`server/`)

### Stack

| Tecnología | Propósito |
|---|---|
| **Python 3.12+** | Lenguaje |
| **FastAPI** | Framework REST |
| **SQLAlchemy 2.0** | ORM |
| **Alembic** | Migraciones DB |
| **SQLite** | Base de datos (desarrollo) |
| **python-jose** | JWT |
| **passlib + bcrypt** | Hash de contraseñas |
| **pytest** | Tests |
| **ReportLab** | Generación de PDF |

### Estructura

```
server/
├── app/
│   ├── main.py              # Entry point FastAPI
│   ├── config.py            # Settings desde variables de entorno
│   ├── database.py          # SQLAlchemy engine + SessionLocal
│   ├── models/
│   │   ├── base.py          # DeclarativeBase + mixins (TimestampMixin)
│   │   ├── taller.py        # Taller (multi-tenant)
│   │   ├── usuario.py       # Usuarios con PIN
│   │   ├── cliente.py       # Clientes
│   │   ├── vehiculo.py      # Vehículos
│   │   ├── orden_trabajo.py # Órdenes de trabajo (con estados)
│   │   ├── detalle_trabajo.py
│   │   ├── articulo.py      # Inventario
│   │   ├── pieza_utilizada.py
│   │   ├── cita.py          # Agenda
│   │   ├── factura.py       # Facturación
│   │   ├── configuracion.py # Configuración del taller
│   │   └── archivo.py       # Archivos adjuntos
│   ├── schemas/             # Pydantic (request/response)
│   ├── api/v1/              # 8 routers REST
│   ├── services/            # Lógica de negocio
│   └── core/                # Security, WebSocket, exceptions
├── alembic/                 # Migraciones
├── tests/                   # pytest (16 tests)
└── requirements.txt
```

### Base de datos: 12 tablas

| Tabla | PK | Claves foráneas |
|---|---|---|
| `talleres` | id UUID | — |
| `usuarios` | id UUID | taller_id → talleres |
| `clientes` | id UUID | taller_id → talleres |
| `vehiculos` | id UUID | taller_id, cliente_id |
| `ordenes_trabajo` | id UUID | taller_id, cliente_id, vehiculo_id, mecanico_id |
| `detalles_trabajo` | id UUID | orden_id → ordenes_trabajo |
| `articulos` | id UUID | taller_id |
| `piezas_utilizadas` | id UUID | detalle_id, articulo_id |
| `citas` | id UUID | taller_id, cliente_id, vehiculo_id |
| `facturas` | id UUID | taller_id, cliente_id, orden_id |
| `configuracion_taller` | taller_id (PK) | taller_id |
| `archivos` | id UUID | taller_id, orden_id |

### Auth

- `POST /auth/registro-taller` — crea taller + admin con hash bcrypt
- `POST /auth/login` — email + password → JWT (sub=usuario.id, taller_id)
- `POST /auth/login-pin` — PIN de 4 dígitos → JWT
- `GET /auth/me` — perfil actual desde token
- `PATCH /auth/me/pin` — asignar/cambiar PIN

JWT configurable: access token (30 min default).

### Tests

```bash
cd server && . venv/bin/activate && pytest tests/ -v
```

16 tests: auth (4), clientes (6), vehículos (5 + 1 búsqueda).

### Ejecutar

```bash
cd server
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
# → http://localhost:8000
# → http://localhost:8000/docs (Swagger)
```

---

## 26. Módulo Desktop Python (`desktop-python/`)

### Stack

| Tecnología | Propósito |
|---|---|
| **Python 3.12+** | Lenguaje |
| **CustomTkinter** | UI moderna con tema oscuro |
| **httpx** | Cliente HTTP asíncrono |
| **Pillow** | Procesamiento de imágenes |
| **pytest** | Tests |

### Estructura

```
desktop-python/
├── app/
│   ├── main.py              # Entry point (ventana + navegación)
│   ├── config.py            # Config (token file, API URL)
│   ├── api_client.py        # Cliente HTTP con JWT
│   ├── state.py             # Estado global (AppState)
│   ├── views/
│   │   ├── login_view.py    # Login email + PIN
│   │   ├── dashboard_view.py# KPIs del taller
│   │   ├── clientes_view.py # CRUD con tabla
│   │   ├── vehiculos_view.py# CRUD con tabla
│   │   ├── ordenes_view.py  # CRUD con tabla y UUID resolution
│   │   ├── citas_view.py    # CRUD con tabla
│   │   ├── facturas_view.py # CRUD con selector de órdenes
│   │   └── articulos_view.py# CRUD con stock
│   ├── components/
│   │   ├── sidebar.py       # Navegación lateral
│   │   ├── table_widget.py  # Treeview con estilo centralizado
│   │   ├── form_widgets.py  # Campos de formulario reutilizables
│   │   └── dialogs.py       # Diálogos modales
│   └── utils/
│       └── helpers.py       # format_fecha, format_decimal
│
└── tests/
    └── test_helpers.py      # 4 tests de utilidades
```

### Pantallas

| Vista | Funcionalidad |
|---|---|
| **Login** | Email/password + PIN con selector de servidor |
| **Dashboard** | KPIs: órdenes activas, clientes, vehículos, citas hoy, ingresos mes |
| **Clientes** | Lista + crear/editar/eliminar con nombre, teléfono, email, dirección |
| **Vehículos** | Lista + crear/editar/eliminar con marca, modelo, matrícula, cliente |
| **Órdenes** | Lista + crear/editar/eliminar con estados, UUIDs resueltos a nombre |
| **Citas** | Agenda con selector de cliente, vehículo, mecánico, fecha |
| **Facturas** | Crear desde orden con selector de vehículo + cliente |
| **Artículos** | Inventario con stock, precio, SKU |

### Patrones

- **AppState** global: almacena token, taller_id, usuario actual
- **api_client.py**: métodos `get`, `post`, `put`, `delete` con JWT automático
- **UUID resolution**: los UUIDs de cliente/vehículo se resuelven a nombres visibles en las tablas mediante caché local
- **Treeview centralizado**: estilo común en `table_widget.py`

### Tests

```bash
cd desktop-python && . .venv/bin/activate && pytest tests/ -v
```

4 tests de helpers (`format_fecha`, `format_decimal`).

### Ejecutar

```bash
cd desktop-python
python3 -m venv .venv && source .venv/bin/activate
pip install -e ".[dev]"
python -m app.main
```

> **Nota**: CustomTkinter requiere Tkinter. En Ubuntu: `sudo apt install python3-tk`. En Windows: marca "tcl/tk and IDLE" en el instalador de Python.

---

## 27. Scripts de desarrollo multiplataforma

### Makefile (Linux / WSL)

| Comando | Acción |
|---|---|
| `make install` | Crea venvs + pip install (server + desktop) |
| `make server` | Arranca FastAPI con --reload |
| `make desktop` | Arranca app CustomTkinter |
| `make test` | Tests server + desktop |
| `make seed` | Datos demo vía API |
| `make lint` | Detekt (Android) |
| `make clean` | Borra venvs, caches, builds |

### dev.ps1 (Windows PowerShell)

```
.\dev.ps1 install | server | desktop | test | seed
```

### dev.bat (Windows CMD)

```
dev.bat install | server | desktop | test | seed
```
