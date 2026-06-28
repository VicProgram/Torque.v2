# Optimizaciones pendientes

## 🔴 Alta prioridad

- [ ] **`MotoDetalle.kt` — 11 usos de `!!`** (`moto!!`, etc.) — Aunque hay null-check previo, en recomposición puede crashear. Usar `?.let` o `requireNotNull()`.
- [ ] **`PerfilActivity.kt` — 4 usos de `!!`** — `nombre!!`, `email!!`, `telefono!!`, `fechaNacimiento!!`.
- [ ] **`MantenimientoHistorial.kt` — 2 usos de `!!`** — `itemSeleccionado!!`, `itemAEliminar!!`.
- [ ] **`TorqueDashboardViewModel.kt:183` — `as Application` (unsafe cast)** — Usar `as?` con fallback.
- [ ] **`NotificacionSalud.kt:33` — `as NotificationManager` (unsafe cast)** — Usar `as?` con fallback.

## 🟡 Media prioridad

- [ ] **Galería de fotos — carrusel automático** — Que las fotos cambien cada X segundos en el dashboard.
- [ ] **Galería de fotos — mejorar gestión** — Ver, eliminar y reordenar fotos desde detalle de moto.
- [ ] **`AjustesActivity.kt` — extraer ~15 strings hardcoded** a `strings.xml` (toasts, botones, títulos, confirmación restore).
- [ ] **`PerfilActivity.kt` — extraer ~12 strings hardcoded** a `strings.xml` (títulos, placeholders, labels).
- [ ] **`OpcionesMantenimiento.kt` — extraer ~7 strings hardcoded** a `strings.xml`.
- [ ] **`MantenimientoHistorial.kt` — extraer 7 strings de filtros** a `strings.xml` (`"Todos"`, `"Aceite"`, etc.).
- [ ] **`RegistroMantenimiento.kt` — extraer ~11 strings de chips/secciones** a `strings.xml`.
- [ ] **`NotificacionSalud.kt` — extraer ~5 strings de notificaciones** a `strings.xml`.
- [ ] **`MotoDetalle.kt:391` — `File.delete()` en main thread** — Envolver en `withContext(Dispatchers.IO)`.
- [ ] **`NotificacionSalud.kt:40` — parámetro `kmActuales` no usado** — Eliminar o implementar.
- [ ] **`TorqueDashboardScreen.kt:525` — `scopeExportar.launch` on Main** — Aunque BackupManager usa IO, código previo corre en Main. Mover toda la lógica a `viewModelScope`.

## 🟢 Baja prioridad

- [ ] **Separar `ColoresTorque.kt` (~400 líneas)** — Mover colores a un archivo, UI Components a otro.
- [ ] **`MarcasModelos.kt` (~678 líneas)** — Mover datos estáticos a JSON en `res/raw/`.
- [ ] **`TorqueDashboardScreen.kt` (~763 líneas)** — Dividir en archivos más pequeños (CabeceraDashboard, TarjetaProximoServicio, TarjetaAccionRapida).
- [ ] **`MotoDetalle.kt` (~426 líneas) — función `VistaMotoDetalle` (~340 líneas)** — Extraer secciones en composables hijos.
- [ ] **`AnadirMotoActivity.kt` — función `VistaAnadirMoto` (~303 líneas)** — Dividir en composables más pequeños.
- [ ] **`RegistroMantenimiento.kt` — función `PantallaRegistroMantenimiento` (~240 líneas)** — Dividir en composables.
- [ ] **`PerfilActivity.kt` — función `PerfilScreen` (~213 líneas)** — Dividir.
- [ ] **`AjustesActivity.kt` — función `AjustesScreen` (~200 líneas)** — Dividir.
- [ ] **Strings hardcoded en `ListaGaraje.kt`, `FormularioMantenimiento.kt`, `TorqueDashboardScreen.kt`** — Extraer a `strings.xml`.
- [ ] **Duplicación tareas: `RegistroMantenimiento.kt` vs `TiposMantenimiento.kt`** — Unificar fuentes de datos.
- [ ] **Mover `ContextUtils.kt` y `FileUtils.kt` de `:data` a `:app`** — Tienen dependencias Android, violan clean architecture.
- [ ] **Mover `PerfilRepository.kt` de `:app` a `:data`** — Es capa de datos, debería estar en módulo data.
- [ ] **Mover `Context.dataStore` de `AjustesRepository.kt` a `:app`** — Extension property con contexto Android.
- [ ] **Package no estándar: `components.ui.dashboard`** — Mover a `ui.dashboard` (convención estándar).
- [ ] **Nombres mixtos español/inglés** — Decidir idioma y unificar (ej: `DialogoKm` vs `DialogoActualizarKm`, `FiltroChip` mixto).
- [ ] **`FileUtils.kt` en data acopla Android** — Mover a app o abstraer dependency inversion.

## ✅ Completadas (todas las sesiones)

- [x] **`!!` en `TorqueDashboardScreen.kt`** — No hay ninguno.
- [x] **`DialogoAjusteDesgaste` — 6→1 query** — Ahora recibe datos desde ViewModel, sin queries.
- [x] **`DialogoAjusteDesgaste` carga datos propios** — Recibe `initialPorcentajes` del ViewModel.
- [x] **Unificar altura de cabeceras** — `ColoresTorque.ALTURA_CABECERA` (56dp) en 10 pantallas.
- [x] **Crear `DialogoTorque` reutilizable** — Refactorizados `DialogoKm`, `DialogoAjusteDesgaste`, `DialogoNotificaciones`.
- [x] **Extraer diálogos de `MotoDetalle.kt`** — Creados `DialogoActualizarKm`, `DialogoEliminarMoto`, `DialogoEliminarFoto`.
- [x] **`SaludMotoCalculator.kt`** — Bucle cuadrático O(N×T) → O(N+23) con pre-agrupación.
- [x] **`AppRepository.kt`** — Excepciones en escrituras críticas se relanzan tras loguear.
- [x] **`SimpleDateFormat` a `companion object`** — En ViewModel y Calculator.
- [x] **`withContext(Dispatchers.Main)` redundante** eliminado de `FormularioMantenimiento`.
- [x] **`FiltroChip` duplicado** eliminado de `ColoresTorque.kt`.
- [x] **Hardcoded colors reemplazados** — `0xFFEF4444`/`0xFF22C55E`/`0xFFEAB308` → constantes.

## ℹ️ Intencional (no cambiar)

- **`MantenimientoDao.kt` vs `SaludMotoCalculator`** — DAO ordena por fecha, Calculator por kms. Cada uno a su propósito.
- **`com.example.torque` como package raíz** — Placeholder ok para desarrollo; cambiar solo para release.
- **Nombres en español** — Decisión del proyecto original, aceptado como convención interna.

## Resumen de hallazgos (análisis 2026-06-28)

| Categoría | Cantidad | Severidad |
|-----------|----------|-----------|
| Archivos > 400 líneas | 4 (763, 678, 426, 400) | 🔴 Mantenibilidad |
| `!!` en producción | 17 (11+4+2) | 🔴 Crash risk |
| Strings hardcoded | ~50+ en 8+ archivos | 🟡 i18n |
| Funciones > 50 líneas | ~14 composables | 🟡 Mantenibilidad |
| Parámetros no usados | 1 (`kmActuales`) | 🟢 Low |
| Unsafe casts | 2 | 🟡 Crash risk |
| Clean arch violations | 3 archivos en módulo incorrecto | 🟡 Acoplamiento |
| Coroutine issues | 2 | 🟡 ANR risk |
| Package no estándar | 1 (`components.ui.dashboard`) | 🟢 Convención |
