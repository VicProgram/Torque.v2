# Torque Desktop — Guia de Desarrollo

## Indice

1. [Stack Tecnologico](#1-stack-tecnologico)
2. [Arquitectura General](#2-arquitectura-general)
3. [Setup del Entorno](#3-setup-del-entorno)
4. [Estructura del Proyecto](#4-estructura-del-proyecto)
5. [Modulo :shared (Logica Compartida)](#5-modulo-shared-logica-compartida)
6. [Modulo :desktop](#6-modulo-desktop)
7. [Base de Datos (SQLDelight)](#7-base-de-datos-sqldelight)
8. [Data Layer](#8-data-layer)
9. [UI Layer](#9-ui-layer)
10. [Sync con Android (Backup ZIP)](#10-sync-con-android-backup-zip)
11. [Testing](#11-testing)
12. [Patrones y Convenciones](#12-patrones-y-convenciones)
13. [Como Anadir una Pantalla](#13-como-anadir-una-pantalla)
14. [Deploy y Distribucion](#14-deploy-y-distribucion)
15. [Troubleshooting](#15-troubleshooting)

---

## 1. Stack Tecnologico

| Componente | Version | Proposito |
|------------|---------|-----------|
| Kotlin | 2.1.20 | Lenguaje principal (JVM) |
| Compose Desktop | 1.7.3 | UI declarativa multiplataforma |
| Material 3 | (incluido) | Diseno de componentes UI |
| SQLDelight | 2.0.2 | Base de datos SQLite tipada |
| SQLite JDBC | (transitiva) | Driver JDBC para SQLite |
| Gradle | 8.12.1 | Build system |
| Java | 17+ | JVM target |
| JUnit | 4.13.2 | Tests unitarios |

---

## 2. Arquitectura General

```
┌────────────────────────────────────────────────────────────┐
│                    :shared (JVM puro)                       │
│  MotoData, MantenimientoData, SaludMotoCalculator,          │
│  TiposMantenimiento, DateUtils, FotoData, ItvSeguroData     │
│  Sin dependencias de plataforma                             │
└──────┬──────────────────────────────────┬──────────────────┘
       │ depends on                       │ depends on
┌──────▼──────────────┐    ┌──────────────▼──────────────────┐
│   :data (Android)    │    │   :desktop (JVM Desktop)        │
│   Room, DataStore    │    │   SQLDelight, Compose Desktop   │
│   BackupManager      │    │   BackupImporter                │
│   (exporta ZIP)      │    │   (importa ZIP)                 │
└──────────────────────┘    └────────────────────────────────┘
           │                            │
           └────── ZIP backup ──────────┘
              (formato compatible)
```

**Principios:**
- Logica de dominio compartida en `:shared` (modelos + algoritmos)
- Cada plataforma tiene su propia UI y base de datos
- El formato ZIP del BackupManager Android es el contrato entre plataformas
- No hay ViewModel en desktop — estado gestionado con `mutableStateOf` + `remember`

---

## 3. Setup del Entorno

### Requisitos

- Java 17+ (JDK)
- Git
- Android Studio (opcional, para ver el proyecto completo)

### Verificar Java

```bash
java -version  # debe ser 17 o superior
```

Si no tienes Java 17, descargalo portable:

```bash
curl -sL "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.14+7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.14_7.tar.gz" -o /tmp/jdk17.tar.gz
tar xzf /tmp/jdk17.tar.gz -C /tmp/
export JAVA_HOME=/tmp/jdk-17.0.14+7
export PATH=$JAVA_HOME/bin:$PATH
```

### Compilar

```bash
./gradlew :desktop:compileKotlin
```

### Ejecutar

```bash
./gradlew :desktop:run
```

### Tests

```bash
./gradlew :shared:test :desktop:test
```

### Generar instalable nativo

```bash
./gradlew :desktop:packageDistributableForCurrentOS
```

Los binarios se generan en `desktop/build/compose/binaries/`.

---

## 4. Estructura del Proyecto

```
Torque.v2/
├── settings.gradle.kts              # Incluye :app, :core, :data, :shared, :desktop
├── build.gradle.kts                 # Plugins raiz (todos con apply false)
├── gradle/libs.versions.toml        # Version catalog
├── shared/                          # ← Modulo compartido JVM
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/.../shared/
│       │   ├── model/               # DTOs e interfaces
│       │   ├── algorithm/           # SaludMotoCalculator
│       │   └── util/                # DateUtils
│       └── test/kotlin/.../shared/  # Tests
└── desktop/                         # ← Modulo desktop
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── kotlin/.../desktop/
        │   │   ├── Main.kt          # Entry point
        │   │   ├── App.kt           # Composable raiz
        │   │   ├── navigation/      # Destinos.kt (sealed class Screen)
        │   │   ├── data/
        │   │   │   ├── DesktopRepositorio.kt
        │   │   │   └── database/
        │   │   │       └── DatabaseFactory.kt
        │   │   ├── sync/
        │   │   │   └── BackupImporter.kt
        │   │   └── ui/
        │   │       ├── theme/       # Color.kt, Theme.kt
        │   │       ├── components/  # BotonTorque, TarjetaTorque, etc.
        │   │       └── screens/     # Dashboard, Garaje, MotoDetalle, Ajustes
        │   └── sqldelight/.../db/
        │       └── Torque.sq       # Schema SQL + queries
        └── test/kotlin/.../desktop/ # Tests
```

---

## 5. Modulo :shared (Logica Compartida)

Modulo JVM puro sin dependencias de plataforma. Contiene todo lo que se comparte entre Android y Desktop.

### 5.1 Modelos (`shared/.../model/`)

| Archivo | Descripcion |
|---------|-------------|
| `MotoData.kt` | DTO de vehiculo — 23 campos (marca, modelo, tipoVehiculo, fechaItv, etc.) |
| `MantenimientoData.kt` | Interface que deben implementar los DTOs de mantenimiento |
| `MantenimientoDto.kt` | Implementacion de `MantenimientoData` con 16 propiedades |
| `TipoMantenimiento.kt` | `enum class TipoVehiculo { MOTO, COCHE }`, `data class TipoMantenimiento`, `object TiposMantenimiento` con 25 tipos predefinidos |
| `FotoData.kt` | Foto de vehiculo (id, idMoto, ruta, creadoEn) |
| `FotoMantenimientoData.kt` | Foto de mantenimiento (id, idMantenimiento, ruta, tipo, creadoEn) |
| `PlanData.kt` | Item del plan de mantenimiento |
| `ItvSeguroData.kt` | `ItvData` y `SeguroData` con fechas de ITV y seguro |
| `SaludModels.kt` | `DetalleSalud` (tipo, ultimoKms, cumplimiento, relevancia) y `ResultadoSalud` (saludGlobal, desglose, proximosVencidos) |

### 5.2 `SaludMotoCalculator` (`shared/.../algorithm/`)

Algoritmo de salud que recibe `List<MantenimientoData>` + `kmActuales` y devuelve `ResultadoSalud`.

**Logica:**
- Agrupa mantenimientos por `tipoMantenimientoId`
- Para cada tipo, calcula cumplimiento (0-100%):
  - **Desgaste**: usa `porcentajeVida` directamente
  - **Intervalo km**: `1 - (kmActuales - ultimoKms) / intervaloKms`
  - **Intervalo dias**: `1 - (diasDesdeUltimo) / intervaloDias`
  - Toma el menor de ambos (km y dias)
- Salud global: media ponderada por `peso` de cada tipo
- `proximosVencidos`: top 5 con cumplimiento < 100, ordenados por relevancia descendente

### 5.3 `DateUtils` (`shared/.../util/`)

Constantes de formato:
- `FORMATO_FECHA_VISUAL = "dd/MM/yyyy"`
- `FORMATO_FECHA_COMPLETA = "yyyy-MM-dd HH:mm:ss"`

### 5.4 Build (`shared/build.gradle.kts`)

```kotlin
plugins { alias(libs.plugins.kotlin.jvm) }
dependencies { testImplementation("junit:junit:4.13.2") }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }
tasks.test { useJUnit() }
```

---

## 6. Modulo :desktop

### 6.1 Build (`desktop/build.gradle.kts`)

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight") version "2.0.2"
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
    testImplementation("junit:junit:4.13.2")
}

sqldelight {
    databases {
        create("TorqueDatabase") {
            packageName.set("com.example.torque.desktop.db")
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.torque.desktop.MainKt"
        nativeDistributions {
            targetFormats(Dmg, Deb, Msi)
            packageName = "Torque"
            packageVersion = "1.0.0"
            vendor = "Torque"
        }
    }
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }
```

**Puntos clave:**
- SQLDelight genera codigo en `build/generated/sqldelight/`
- Compose Desktop necesita el plugin `kotlin.plugin.compose`
- La dependencia `sqlite-driver` incluye el JDBC de SQLite

---

## 7. Base de Datos (SQLDelight)

### 7.1 Schema (`desktop/src/main/sqldelight/.../db/Torque.sq`)

Define 4 tablas identicas al Room de Android:

```sql
-- Tabla motos (vehiculos)
CREATE TABLE IF NOT EXISTS motos (
    idMoto INTEGER PRIMARY KEY AUTOINCREMENT,
    marca TEXT NOT NULL DEFAULT '',
    modelo TEXT NOT NULL DEFAULT '',
    anio INTEGER NOT NULL DEFAULT 0,
    matricula TEXT,
    color TEXT,
    cilindrada TEXT, caballos TEXT, estilo TEXT,
    kilometros INTEGER NOT NULL DEFAULT 0,
    fechaCompra TEXT,
    esPrincipal INTEGER NOT NULL DEFAULT 0,
    uriFoto TEXT,
    creadoEn TEXT, actualizadoEn TEXT,
    fechaItv TEXT, fechaSeguro TEXT,
    tipoVehiculo TEXT NOT NULL DEFAULT 'MOTO',
    tipoCarroceria TEXT, tipoCombustible TEXT,
    tipoTransmision TEXT, numeroPuertas INTEGER
);

CREATE TABLE IF NOT EXISTS mantenimientos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    idMoto INTEGER NOT NULL,
    nombreMantenimiento TEXT NOT NULL, fecha TEXT NOT NULL,
    kms INTEGER NOT NULL DEFAULT 0, intervaloKms INTEGER NOT NULL DEFAULT 0,
    precio TEXT, tiempo TEXT, notas TEXT, tareas TEXT,
    creadoEn TEXT,
    tipoMantenimientoId INTEGER NOT NULL DEFAULT 0,
    porcentajeVida INTEGER NOT NULL DEFAULT 100
);

CREATE TABLE IF NOT EXISTS fotos_moto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    idMoto INTEGER NOT NULL, ruta TEXT NOT NULL, creadoEn TEXT
);

CREATE TABLE IF NOT EXISTS fotos_mantenimiento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    idMantenimiento INTEGER NOT NULL,
    ruta TEXT NOT NULL, tipo TEXT NOT NULL DEFAULT 'foto', creadoEn TEXT
);
```

### 7.2 Queries Nombradas

SQLDelight convierte cada query con nombre en funciones Kotlin tipadas:

```sql
obtenerTodasMotos:  SELECT * FROM motos ORDER BY creadoEn DESC;
obtenerMotoPorId:   SELECT * FROM motos WHERE idMoto = ?;
contarMotos:        SELECT COUNT(*) FROM motos;
insertarMoto:       INSERT INTO motos(...) VALUES (?, ?, ...);
eliminarTodoMotos:  DELETE FROM motos;
```

### 7.3 Codigo Generado

Para cada tabla, SQLDelight genera un data class con el nombre de la tabla en plural:

| Tabla SQL | Clase generada | Package |
|-----------|---------------|---------|
| `motos` | `Motos` | `com.example.torque.desktop.db` |
| `mantenimientos` | `Mantenimientos` | ... |
| `fotos_moto` | `Fotos_moto` | ... |
| `fotos_mantenimiento` | `Fotos_mantenimiento` | ... |

**Tipos generados importantes:**
- `anio: Long`, `kilometros: Long`, `esPrincipal: Long`
- `kms: Long`, `intervaloKms: Long`, `porcentajeVida: Long`, `tipoMantenimientoId: Long`
- `tareas: String?` (nullable)
- `matricula: String?`, `color: String?` (nullable)

### 7.4 Acceso a la DB

```kotlin
val driver = JdbcSqliteDriver("jdbc:sqlite:/path/to/torque.db")
TorqueDatabase.Schema.create(driver)  // Crea las tablas si no existen
val db = TorqueDatabase(driver)       // Instancia tipada
val motos = db.torqueQueries.obtenerTodasMotos().executeAsList()
```

---

## 8. Data Layer

### 8.1 `DatabaseFactory` (`data/database/DatabaseFactory.kt`)

Singleton `object` que gestiona el driver JDBC de SQLite:

```kotlin
object DatabaseFactory {
    private val RUTA_BASE = File(System.getProperty("user.home"), ".torque")
    
    fun obtenerDriver(): SqlDriver  // Crea o reusa driver, ejecuta Schema.create
    fun obtenerBaseDeDatos(): TorqueDatabase
    fun cerrar()
    fun rutaBase(): File            // ~/.torque/
    fun rutaFotos(): File           // ~/.torque/fotos/
}
```

La base de datos se almacena en `~/.torque/torque.db`.

### 8.2 `DesktopRepositorio` (`data/DesktopRepositorio.kt`)

Fachada de acceso a datos. Constructor acepta `testDb: TorqueDatabase? = null` para testing.

```kotlin
class DesktopRepositorio(private val testDb: TorqueDatabase? = null) {
    // Lectura
    fun obtenerMotos(): List<MotoData>
    fun obtenerMotoPorId(id: Long): MotoData?
    fun contarMotos(): Long
    fun obtenerMantenimientosPorMoto(idMoto: Long): List<MantenimientoDto>
    fun obtenerFotosPorMoto(idMoto: Long): List<FotoData>
    
    // Escritura bulk (para backup)
    fun cargarBackup(motos, mantenimientos, fotosMoto, fotosMantenimiento)
    fun limpiarTodo()  // DELETE en orden: mantenimientos → fotos → fotos_mantenimiento → motos
    
    // Mappers privados
    private fun Motos.toMotoData(): MotoData
    private fun Mantenimientos.toMantenimientoDto(): MantenimientoDto
}
```

**Nota sobre inserciones:** La query `insertarMoto` no incluye `fechaItv`, `fechaSeguro`, `tipoVehiculo`, etc. porque esas columnas tienen valores DEFAULT en el schema. Cuando se necesiten insertar explicitamente, habra que anadirlas a la query en Torque.sq.

---

## 9. UI Layer

### 9.1 Tema (`ui/theme/`)

**`Color.kt`** — `object ColoresTorque` con 14 constantes que replican el tema Android:

```kotlin
object ColoresTorque {
    val Primary = Color(0xFF135BEC)
    val Accent = Color(0xFFFF6B00)
    val Background = Color(0xFF101622)
    val SurfaceDark = Color(0xFF1C2331)
    val BorderDark = Color(0xFF2D3748)
    val RojoError = Color(0xFFEF4444)
    val VerdeExito = Color(0xFF22C55E)
    val AmarilloAdvertencia = Color(0xFFEAB308)
    // ...
}
```

**`Theme.kt`** — `@Composable fun TorqueThemeDesktop(darkMode: Boolean, content)`

Aplica `darkColorScheme` (completo) y `lightColorScheme` (basico) de Material 3, con `Typography` y `Shapes` personalizados.

**Modo oscuro por defecto:** La app arranca en dark mode.

### 9.2 Componentes (`ui/components/`)

| Componente | Firma | Descripcion |
|-----------|-------|-------------|
| `EncabezadoSeccion` | `(titulo: String, modifier)` | Texto uppercase azul primary, 14sp bold |
| `BotonTorque` | `(text, icon?, onClick, modifier, enabled)` | Boton primary full-width, 48dp height, rounded 12dp |
| `CampoTextoTorque` | `(value, onValueChange, label, modifier, enabled)` | OutlinedTextField con colores del tema |
| `TarjetaTorque` | `(modifier, content)` | Card con 16dp rounded, surface oscuro |
| `DialogoTorque` | `(titulo, mensaje, textoConfirmar, textoCancelar, colorConfirmar, onConfirmar, onCancelar)` | AlertDialog reutilizable |

### 9.3 Navegacion

No hay NavHost (no disponible en Compose Desktop). Se usa estado mutable:

```kotlin
var currentScreen by remember { mutableStateOf<Screen>(Screen.DASHBOARD) }

when (currentScreen) {
    Screen.DASHBOARD -> DashboardScreen()
    Screen.GARAJE -> GarajeScreen()
    Screen.MOTO_DETALLE -> MotoDetalleScreen(idMoto = ...)
    Screen.AJUSTES -> AjustesScreen()
}
```

**Screen** es una sealed class en `navigation/Destinos.kt`:

```kotlin
sealed class Screen {
    data object DASHBOARD : Screen()
    data object GARAJE : Screen()
    data object MOTO_DETALLE : Screen()
    data object AJUSTES : Screen()
}
```

### 9.4 Entry Point (`Main.kt`)

```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Torque", state = rememberWindowState(width = 1200.dp, height = 800.dp)) {
        TorqueThemeDesktop {
            App()
        }
    }
}
```

### 9.5 Screens (Estado Actual — Fase 1 MVP)

Todas las pantallas son **placeholders**. Solo muestran texto indicando el contenido futuro:

| Screen | Archivo | Estado |
|--------|---------|--------|
| `DashboardScreen` | `ui/screens/DashboardScreen.kt` | Placeholder |
| `GarajeScreen` | `ui/screens/GarajeScreen.kt` | Placeholder |
| `MotoDetalleScreen` | `ui/screens/MotoDetalleScreen.kt` | Placeholder (recibe `idMoto: Long`) |
| `AjustesScreen` | `ui/screens/AjustesScreen.kt` | Placeholder |

---

## 10. Sync con Android (Backup ZIP)

### 10.1 Formato del Backup

Android exporta un ZIP con:

```
torque_room.db         → Base de datos SQLite (las 4 tablas)
fotos/                 → Archivos de imagen (carpeta)
preferencias/          → DataStore (ignorado en desktop)
```

La DB contiene exactamente las mismas 4 tablas que el schema SQLDelight.

### 10.2 BackupImporter (`sync/BackupImporter.kt`)

```kotlin
class BackupImporter(private val repositorio: DesktopRepositorio) {
    fun importar(rutaZip: String)
}
```

**Proceso:**
1. Extrae el ZIP a un directorio temporal
2. Busca `torque_room.db` dentro
3. Abre la DB con `DriverManager.getConnection("jdbc:sqlite:...")`
4. Lee todas las filas de las 4 tablas
5. Mapea a `MotoData`, `MantenimientoDto`, `FotoData`, `FotoMantenimientoData`
6. Copia `fotos/` a `~/.torque/fotos/`
7. Llama a `repositorio.limpiarTodo()` + `repositorio.cargarBackup()`
8. Limpia el directorio temporal

**Manejo de null safety:**
- `matricula`, `color`, `cilindrada` — nullable
- `tipoVehiculo` se parsea con `try { TipoVehiculo.valueOf(...) } catch { MOTO }`
- `numeroPuertas` se lee con `wasNull()` check

---

## 11. Testing

### 11.1 Framework

JUnit 4 (`junit:junit:4.13.2`). Sin Mockito ni Kotest.

### 11.2 Tests del Modulo Shared

**`SaludMotoCalculatorTest`** (30 tests):

```kotlin
class SaludMotoCalculatorTest {
    // Helper: crea un MantenimientoData anonimo
    private fun mdto(nombre, kms, intervaloKms, tipoId, ...) = object : MantenimientoData { ... }
    
    @Test fun `sin mantenimientos la salud es 100`() { ... }
    @Test fun `mantenimiento al 50 por ciento del intervalo`() { ... }
    @Test fun `tipo desgaste con porcentaje bajo`() { ... }
    // ...
}
```

**`DateUtilsTest`** (2 tests) — Verifica constantes de formato.

### 11.3 Tests del Modulo Desktop

**`DesktopRepositorioTest`** (9 tests):

```kotlin
class DesktopRepositorioTest {
    private lateinit var repositorio: DesktopRepositorio
    private lateinit var driver: JdbcSqliteDriver

    @Before
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TorqueDatabase.Schema.create(driver)
        repositorio = DesktopRepositorio(testDb = TorqueDatabase(driver))
    }
}
```

Patron: in-memory SQLite + constructor injection.

**`DatabaseFactoryTest`** (8 tests) — Verifica creacion de tablas, CRUD, conteo, limpieza.

### 11.4 Ejecutar Tests

```bash
./gradlew :shared:test
./gradlew :desktop:test
./gradlew :shared:test :desktop:test  # ambos
```

---

## 12. Patrones y Convenciones

### 12.1 Estado

No usar ViewModel (no existe en Desktop). Usar `mutableStateOf` + `remember`:

```kotlin
var texto by remember { mutableStateOf("") }
var lista by remember { mutableStateOf(listOf<MotoData>()) }
var cargando by remember { mutableStateOf(false) }
```

Para logica mas compleja, extraer a funciones del mismo archivo.

### 12.2 Navegacion

```kotlin
var currentScreen by remember { mutableStateOf<Screen>(Screen.DASHBOARD) }

// Cambiar de pantalla:
currentScreen = Screen.GARAJE
currentScreen = Screen.MOTO_DETALLE  // si necesita parametros, usar remember adicional
```

### 12.3 Inyeccion de Dependencias

Manual — sin Koin ni Dagger:

```kotlin
// Singleton global
val repositorio = DesktopRepositorio()

// Pasar por constructor/parametro
@Composable
fun GarajeScreen(repositorio: DesktopRepositorio = DesktopRepositorio())
```

### 12.4 Nombrado de Tests

```kotlin
@Test
fun `descripcion del caso en espanol`() {
    // ...
}
```

### 12.5 Mapeo SQLDelight → DTOs

Siempre en extension functions privadas dentro de `DesktopRepositorio`:

```kotlin
private fun Motos.toMotoData(): MotoData = MotoData(
    id = idMoto,
    marca = marca,
    // ...atencion a tipos: anio.toInt(), kilometros.toInt(), esPrincipal == 1L
)
```

### 12.6 Transacciones

```kotlin
db.transaction {
    query1()
    query2()
}
```

---

## 13. Como Anadir una Pantalla

Ejemplo: anadir `ConfiguracionScreen` (suponiendo que ya no existe Ajustes).

### Paso 1: Crear el archivo

```
desktop/src/main/kotlin/.../desktop/ui/screens/ConfiguracionScreen.kt
```

```kotlin
package com.example.torque.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.torque.desktop.ui.theme.ColoresTorque

@Composable
fun ConfiguracionScreen() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Configuracion", style = MaterialTheme.typography.titleLarge, color = ColoresTorque.Primary)
        }
    }
}
```

### Paso 2: Registrar en Destinos.kt

```kotlin
sealed class Screen {
    // ... existentes
    data object CONFIGURACION : Screen()
}
```

### Paso 3: Anadir al NavigationRail en App.kt

```kotlin
NavigationRailItem(
    selected = currentScreen == Screen.CONFIGURACION,
    onClick = { currentScreen = Screen.CONFIGURACION },
    icon = { Icon(Icons.Default.Settings, contentDescription = "Config") },
    label = { Text("Config") }
)
```

### Paso 4: Anadir al when

```kotlin
when (currentScreen) {
    // ... existentes
    Screen.CONFIGURACION -> ConfiguracionScreen()
}
```

### Paso 5: (Opcional) Tests

```kotlin
@test
fun `configuracion screen se renderiza sin crash`() {
    // En Compose Desktop no hay testing de UI directo,
    // pero se puede verificar que no lance excepciones
}
```

---

## 14. Deploy y Distribucion

### 14.1 Generar Instalable

```bash
# Para el SO actual
./gradlew :desktop:packageDistributableForCurrentOS

# Formatos especificos
./gradlew :desktop:packageDmg   # macOS
./gradlew :desktop:packageDeb   # Linux
./gradlew :desktop:packageMsi   # Windows
```

### 14.2 Output

Los binarios se generan en:

```
desktop/build/compose/binaries/
├── main/
│   ├── dmg/     → Torque-1.0.0.dmg
│   ├── deb/     → torque-desktop_1.0.0_amd64.deb
│   └── msi/     → Torque-1.0.0.msi
```

### 14.3 Configuracion de Distribucion

En `desktop/build.gradle.kts`:

```kotlin
nativeDistributions {
    targetFormats(Dmg, Deb, Msi)
    packageName = "Torque"
    packageVersion = "1.0.0"
    description = "Gestion de mantenimiento de motocicletas"
    vendor = "Torque"
    windows { menuGroup = "Torque" }
    macOS { bundleID = "com.example.torque.desktop" }
    linux { packageName = "torque-desktop" }
}
```

---

## 15. Troubleshooting

### 15.1 "Cannot create Linux GL context"

Ocurre en entornos headless (SSH, CI) o sin GPU. En un escritorio real con controladores graficos no aparece.

### 15.2 "Plugin is already on the classpath"

El plugin `kotlin.jvm` esta declarado con `version` en un subproyecto pero ya esta disponible desde el root. Solucion: usar `alias(libs.plugins.kotlin.jvm)` sin version.

### 15.3 "Redeclaration: enum class TipoVehiculo"

`TipoVehiculo` esta definido en dos sitios. Mantener solo en `TipoMantenimiento.kt` (shared module) y eliminarlo de `MotoData.kt`.

### 15.4 SQLDelight no genera codigo

```bash
./gradlew :desktop:generateMainTorqueDatabaseInterface
```

Verificar que el archivo `.sq` esta en `desktop/src/main/sqldelight/` y que el `packageName` en `build.gradle.kts` coincide.

### 15.5 Base de datos no se encuentra

La DB se guarda en `~/.torque/torque.db`. Si la carpeta no existe, `DatabaseFactory` la crea automaticamente.

### 15.6 Error de tipo Long vs Int

SQLDelight genera `Long` para columnas INTEGER. Hacer `.toInt()` explicitamente al mapear a DTOs.

### 15.7 Tareas null en Mantenimientos

La columna `tareas` en SQLDelight es nullable (`String?`). En `MantenimientoDto` es `String`. Usar `tareas ?: ""` al mapear.
