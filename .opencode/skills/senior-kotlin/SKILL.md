---
name: senior-kotlin
description: Use when writing Kotlin code that needs to be clean, idiomatic, and maintainable — null safety, immutability, coroutines, sealed classes, extension functions, and patterns for senior-level code.
---

# Senior Kotlin Practices for Torque

## Null Safety

**Forbidden**: `!!` (force-unwrap), unsafe casts `as` (use `as?`).
**Preferred**: `?.`, `?:`, `.let { }`, `.also { }`, `requireNotNull`, `checkNotNull`.

```kotlin
// Bad
val nombre = moto.nombre!!

// Good
val nombre = moto.nombre ?: "Desconocido"
val longitud = moto.nombre?.length ?: 0

// Bad
val dao = dao as MotoDao

// Good
val dao = dao as? MotoDao ?: return
```

## Immutability

```kotlin
// Prefer val over var
val motos: List<MotoData>  // si no va a cambiar la referencia

// Prefer immutable collections
listOf(), mapOf(), setOf()

// Use copy() for data classes
fun MotoData.withActualizadoEn(fecha: String): MotoData = copy(actualizadoEn = fecha)

// Mutable state only when needed (UI state)
var motos by mutableStateOf(listOf<MotoData>())
```

## Data Classes

```kotlin
// Good — data class with meaningful defaults
data class MotoData(
    val id: Long = 0,
    val marca: String = "",
    val modelo: String = "",
    val anio: Int = 0,
    val kilometros: Int = 0,
    val esPrincipal: Boolean = false,
    val uriFoto: String? = null
)

// Bad — mixing concerns
data class MotoUIState(
    val id: Long,
    val marca: String,
    val isSelected: Boolean,  // UI state, not domain
    val isLoading: Boolean    // UI state, not domain
)
```

## Sealed Classes for State

```kotlin
sealed class SyncResult {
    data class Success(val cambios: List<Cambio>) : SyncResult()
    data class Error(val mensaje: String, val excepcion: Throwable? = null) : SyncResult()
    data object NoChanges : SyncResult()
}

// Usage
when (val result = syncService.sincronizar()) {
    is SyncResult.Success -> mostrarResultado(result.cambios)
    is SyncResult.Error -> mostrarError(result.mensaje)
    is SyncResult.NoChanges -> { /* no op */ }
}
```

## Extension Functions

```kotlin
// Good — useful, discoverable
fun List<MantenimientoData>.ordenarPorFecha(): List<MantenimientoData> =
    sortedByDescending { it.fecha }

fun MotoData.nombreCompleto(): String = "$marca $modelo ($anio)"

// Prefer extension functions over utility objects
// Bad
object MotoUtils {
    fun nombreCompleto(moto: MotoData) = "${moto.marca} ${moto.modelo}"
}

// Good
fun MotoData.nombreCompleto() = "$marca $modelo"
```

## Coroutines

```kotlin
// Scope management
val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

// Launch with error handling
scope.launch {
    try {
        val resultado = withContext(Dispatchers.IO) { repositorio.obtenerMotos() }
        _motos.value = resultado
    } catch (e: Exception) {
        _error.value = "Error al cargar: ${e.message}"
    }
}

// Use supervisorScope for independent child coroutines
supervisorScope {
    launch { tarea1() }  // failure doesn't cancel tarea2
    launch { tarea2() }
}

// Never use GlobalScope
```

## Error Handling

```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String, val cause: Throwable? = null) : Result<T>()
}

// Usage
fun cargarMotos(): Result<List<MotoData>> = try {
    Result.Success(repositorio.obtenerMotos())
} catch (e: Exception) {
    Result.Error("No se pudieron cargar las motos", e)
}
```

## Single Responsibility

```kotlin
// Bad
class MotoService {
    fun obtenerMotos() { /* DB query */ }
    fun validarFormulario() { /* validation logic */ }
    fun exportarPDF() { /* PDF generation */ }
    fun enviarNotificacion() { /* notification */ }
}

// Good
class MotoRepositorio { fun obtenerMotos(): List<MotoData> }
class ValidadorMoto { fun validar(moto: MotoData): Boolean }
class ExportadorPDF { fun exportar(moto: MotoData, destino: Path) }
class Notificador { fun notificar(mensaje: String) }
```

## Function Design

```kotlin
// Bad — too many parameters, unclear
fun guardar(marca: String, modelo: String, anio: Int, kms: Int, precio: String?, notas: String?)

// Good — use a parameter object
data class DatosGuardado(
    val marca: String,
    val modelo: String,
    val anio: Int,
    val kilometros: Int,
    val precio: String? = null,
    val notas: String? = null
)
fun guardar(datos: DatosGuardado)
```

## Naming Conventions

- **Functions**: verb or verb phrase: `obtenerMotos()`, `guardarMoto()`, `calcularSalud()`
- **Classes/objects**: noun: `MotoRepositorio`, `SaludMotoCalculator`, `BackupImporter`
- **Boolean variables**: `esPrincipal`, `tieneFotos`, `editando`
- **Keep Spanish** (project convention): `guardarMoto`, `obtenerMotos`, `calcularCumplimiento`
- **Constants**: UPPER_SNAKE_CASE: `MAX_FOTOS`, `ALTURA_CABECERA`

## Testability

```kotlin
// Inject dependencies via constructor (no hardcoded singletons)
class DashboardViewModel(
    private val repositorio: RepositorioApp,
    private val calculador: SaludMotoCalculator
)

// Use interfaces for platform-specific code
interface BackupManager {
    fun exportar(ruta: String)
    fun importar(ruta: String)
}

// Test with fake implementations
class FakeBackupManager : BackupManager {
    var exportado = false
    override fun exportar(ruta: String) { exportado = true }
    override fun importar(ruta: String) { /* no-op */ }
}
```

## What to Avoid

| Anti-pattern | Why | Solution |
|-------------|-----|----------|
| `!!` operator | NPE at runtime | `?:`, `?.let`, `requireNotNull` |
| `object` for everything | Hard to test | Constructor injection |
| Global mutable state | Race conditions, unpredictable | State hoisting, ViewModel |
| `Thread.sleep()` | Blocks thread | `delay()` (coroutines) |
| `when` on primitive types | Type-unsafe | Sealed classes |
| Long parameter lists (>5) | Unreadable, error-prone | Parameter object / builder |
| Comments explaining what | Code should be self-documenting | Extract well-named function |
