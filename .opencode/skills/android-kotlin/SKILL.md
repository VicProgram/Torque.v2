---
name: android-kotlin
description: Use when working with Android/Kotlin code in this project — Jetpack Compose, Material 3, coroutines, null safety, ViewModel patterns, and project-specific conventions like Activity-based navigation and Koin DI.
---

# Android + Kotlin Conventions for Torque

Use these rules when writing or modifying Kotlin/Android code in this project.

## Null Safety

- **NEVER use `!!`** — Use `?.let`, `?:`, or `requireNotNull()` with a descriptive message.
- Unsafe casts (`as`) are forbidden — use `as?` with a fallback or `requireNotNull()`.
- Example:
  ```kotlin
  // BAD
  val app = application as Application
  // GOOD
  val app = application as? Application ?: return
  ```

## ViewModels

- Extend `AndroidViewModel` (not `ViewModel`) when the context is needed for DB/repos.
- Use a custom `Factory` (not `koinViewModel()`):
  ```kotlin
  companion object {
      val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
              return TorqueDashboardViewModel(application) as T
          }
      }
  }
  ```
- Expose state via `StateFlow<UiState>` using `stateIn(scope, SharingStarted.WhileSubscribed(5000), initialValue)`.

## Coroutines

- Use `viewModelScope` inside ViewModels.
- DB/File/Network work on `Dispatchers.IO` via `withContext(Dispatchers.IO)`.
- Mutate UI state on `Dispatchers.Main` (automatic with `viewModelScope.launch`).
- Avoid `GlobalScope` and `Dispatchers.Main` redundancies.

## Jetpack Compose

- Use `remember { RepositorioApp.obtenerInstancia(contexto) }` for repo access in composables.
- Use `LaunchedEffect` for side effects (notifications, navigation events).
- Use `collectAsState()` or `collectAsStateWithLifecycle()` for Flow observation.
- Prefer `HorizonalPager` with `PageSize.Fit` for carousel patterns.

## Material 3

- Use `MaterialTheme.colorScheme` for colors, not hardcoded values.
- The `ColoresTorque` object re-exports theme colors — prefer it over raw hex.
- `ALTURA_CABECERA = 56.dp` is the standard header height across all screens.

## Project Patterns

- **Activity-based navigation**: Use `Intent` with `putExtra`. Each screen is its own `ComponentActivity`.
- **DI**: Koin with `single { }` modules. Create in `TorqueApp.kt`.
- **Language**: Spanish — keep function/variable names in Spanish as the project convention.
- **Naming**: Package convention is `com.example.torque` (keep as-is).
- **Colors**: Use constants from `ColoresTorque` or `Color.kt`, not inline hex (`0xFFEF4444`).

## Architecture

- Single-direction: `:app` → `:data` → `:core`
- `:core` has NO Android dependencies (only `core-ktx`)
- Repository pattern via `RepositorioApp` singleton

## Error Handling

- Read-only DB methods: log with `Log.e()` and swallow exceptions.
- Critical writes (`insertar`, `actualizar`, `eliminar`): rethrow after logging.
