---
name: room-database
description: Use when working with Room database code — entities, DAOs, migrations, repository pattern, and database operations in this project.
---

# Room Database Conventions for Torque

## Database Architecture

- `BaseDeDatosApp` extends `RoomDatabase()` with double-check locking singleton.
- Database version is currently **4**. Migrations: v1→v2→v3→v4.
- `RepositorioApp` is the ONLY access point to DB from UI layer.

## Entities

Located in `:data` module under `data/src/main/java/.../room/entity/`:

| Entity | Table | PK |
|--------|-------|----|
| `MotoEntity` | `motos` | `idMoto` (autogenerate) |
| `MantenimientoEntity` | `mantenimientos` | `id` (autogenerate) |
| `FotoEntity` | `fotos_moto` | `id` (autogenerate) |
| `MantenimientoPlanEntity` | `plan_mantenimiento` | `id` (autogenerate) |
| `MotoItvEntity` | `moto_itv` | `idMoto` (FK → motos) |
| `MotoSeguroEntity` | `moto_seguro` | `idMoto` (FK → motos) |

Pattern:
```kotlin
@Entity(tableName = "motos")
data class MotoEntity(
    @PrimaryKey(autoGenerate = true) val idMoto: Long = 0,
    val marca: String,
    val modelo: String,
    ...
)
```

## DAOs

- Return `Flow<List<Entity>>` for reactive queries (`Flow` for single results).
- Return `List<Entity>` (suspending) for one-shot queries.
- DAO file: `data/src/main/java/.../room/entity/dao/<Name>Dao.kt`.

## Repository

`RepositorioApp` (`data/src/main/java/.../room/AppRepository.kt`):
- Singleton with double-check locking (`@Volatile` + `synchronized`).
- ALL DB access goes through here. Never use DAOs directly from UI.
- Obtain in composables: `remember { RepositorioApp.obtenerInstancia(contexto) }`

## Migrations

Define as `object : Migration(oldVersion, newVersion)` in `BaseDeDatosApp`.
Use `db.execSQL()` for schema changes.
Current migration chain: `MIGRACION_1_2`, `MIGRACION_2_3`, `MIGRACION_3_4`.

## UPSERT Pattern

Use `INSERT OR REPLACE` for upserts (see `DatosAnualesDao`):
```kotlin
@Query("INSERT OR REPLACE INTO moto_itv (idMoto, fechaItv, fechaRecordatorio) VALUES (:idMoto, :fechaItv, :fechaRecordatorio)")
suspend fun insertarOActualizarItv(idMoto: Long, fechaItv: String?, fechaRecordatorio: String?)
```

## Writes

- Critical writes rethrow exceptions after logging (`Log.e`).
- Read-only queries swallow exceptions.

## Cleanup

- `cerrarYReiniciar()`: closes DB instance and sets to null (used during backup restore).
