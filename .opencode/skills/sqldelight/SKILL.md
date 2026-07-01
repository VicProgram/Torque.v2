---
name: sqldelight
description: Use when configuring SQLDelight, writing .sq schema files, defining queries, or setting up database access for the desktop module.
---

# SQLDelight Conventions for Torque Desktop

## Setup

```kotlin
// desktop/build.gradle.kts
plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("app.cash.sqldelight") version "2.0.2"
}

sqldelight {
    databases {
        create("TorqueDatabase") {
            packageName.set("com.example.torque.desktop.db")
            srcDirs("src/main/sqldelight")
        }
    }
}

dependencies {
    implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
}
```

## Schema File Location

```
desktop/src/main/sqldelight/com/example/torque/desktop/db/
└── Torque.sq
```

## Schema Rules

- Keep schema identical to Room schema: same table names, column names, types.
- Use `INTEGER` for `Long`, `Int`, `Boolean` (0/1).
- Use `TEXT` for `String`.
- Use `REAL` for `Double`, `Float`.
- Mark `NOT NULL` where Room has `NOT NULL DEFAULT`.

```sql
CREATE TABLE IF NOT EXISTS motos (
    idMoto INTEGER PRIMARY KEY AUTOINCREMENT,
    marca TEXT NOT NULL,
    modelo TEXT NOT NULL,
    anio INTEGER NOT NULL DEFAULT 0,
    matricula TEXT,
    cilindrada TEXT,
    caballos TEXT,
    estilo TEXT,
    kilometros INTEGER NOT NULL DEFAULT 0,
    esPrincipal INTEGER NOT NULL DEFAULT 0,
    uriFoto TEXT,
    fechaItv TEXT,
    fechaSeguro TEXT,
    creadoEn TEXT,
    actualizadoEn TEXT
);
```

## Query Definitions

Place named queries below schema:

```sql
obtenerTodasMotos:
SELECT * FROM motos ORDER BY creadoEn DESC;

obtenerMotoPorId:
SELECT * FROM motos WHERE idMoto = ?;

obtenerMantenimientosPorMoto:
SELECT * FROM mantenimientos WHERE idMoto = ? ORDER BY fecha DESC;

obtenerUltimoMantenimientoPorTipo:
SELECT * FROM mantenimientos
WHERE idMoto = ? AND tipoMantenimientoId = ?
ORDER BY kms DESC LIMIT 1;

contarMotos:
SELECT COUNT(*) FROM motos;

insertarMoto:
INSERT INTO motos(marca, modelo, anio, matricula, kilometros, cilindrada, caballos, estilo, uriFoto, esPrincipal, creadoEn, actualizadoEn)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

actualizarMoto:
UPDATE motos SET marca = ?, modelo = ?, anio = ?, matricula = ?, kilometros = ?,
    cilindrada = ?, caballos = ?, estilo = ?, uriFoto = ?, actualizadoEn = ?
WHERE idMoto = ?;

eliminarMoto:
DELETE FROM motos WHERE idMoto = ?;
```

## Generated Code

SQLDelight generates Kotlin types from SQL:

```kotlin
// Generated data class (example)
data class Moto(
    val idMoto: Long,
    val marca: String,
    val modelo: String,
    // ...
)

// Generated DAO-like interface
val queries: TorqueDatabaseQueries
val motos: List<Moto> = queries.obtenerTodasMotos().executeAsList()
val moto: Moto? = queries.obtenerMotoPorId(id).executeAsOneOrNull()
queries.transaction { queries.insertarMoto(...) }
```

## Converting to Shared DTOs

```kotlin
// Extensions to convert SQLDelight types → :shared model types
fun Moto.toMotoData(): MotoData = MotoData(
    id = idMoto,
    marca = marca,
    modelo = modelo,
    anio = anio,
    matricula = matricula ?: "",
    kilometros = kilometros,
    cilindrada = cilindrada,
    caballos = caballos,
    estilo = estilo,
    uriFoto = uriFoto,
    esPrincipal = esPrincipal == 1L,
    fechaItv = fechaItv,
    fechaSeguro = fechaSeguro,
    creadoEn = creadoEn,
    actualizadoEn = actualizadoEn
)
```

## Database Factory

```kotlin
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

object DatabaseFactory {
    private var driver: SqlDriver? = null

    fun obtenerDriver(ruta: String = System.getProperty("user.home") + "/.torque/torque.db"): SqlDriver {
        return driver ?: JdbcSqliteDriver("jdbc:sqlite:$ruta").also {
            TorqueDatabase.Schema.create(it)
            driver = it
        }
    }

    fun obtenerBaseDeDatos(): TorqueDatabase {
        return TorqueDatabase(obtenerDriver())
    }
}
```

## Migrations Strategy

- Desktop always starts fresh (no legacy data). Use `Schema.create(driver)` for initial creation.
- If schema changes, delete old DB file or use PRAGMA user_version for version tracking:

```kotlin
fun migrar(db: SqlDriver, versionActual: Int, versionDestino: Int) {
    db.execute(null, "PRAGMA user_version = $versionActual", 0)
    // apply migrations incrementally
    db.execute(null, "PRAGMA user_version = $versionDestino", 0)
}
```

## Performance

- Use `executeAsList()` for small result sets.
- Use `executeAsOne()` when exactly one row expected.
- Use `executeAsOneOrNull()` for optional results.
- Wrap writes in `transaction { }` for atomicity.
- Use `coroutinesExtensions` for Flow-based reactive queries (optional).
