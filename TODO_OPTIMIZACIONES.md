# Optimizaciones pendientes

## 🔴 Alta prioridad

- [ ] **`TorqueDashboardScreen.kt`** — Revisar si hay `!!` que puedan crashear (antes en líneas 96,97,107,120 — refactorizadas).
- [ ] **`DialogoAjusteDesgaste.kt`** — 6 queries secuenciales (`obtenerUltimoMantenimientoPorTipo` × 6). Usar una sola consulta con `IN(...)` en el DAO.
- [ ] **`DialogoAjusteDesgaste` carga datos por su cuenta** — Usa `RepositorioApp.obtenerInstancia()` en vez de recibir porcentajes iniciales desde el ViewModel.

## 🟡 Media prioridad

- [ ] **Unificar altura de cabeceras** — Mezcla 56dp y 64dp entre pantallas.
- [ ] **Crear `DialogoTorque` reutilizable** — El patrón `AlertDialog` se repite ~10 veces.
- [ ] **Extraer diálogos de `MotoDetalle.kt`** (~490 líneas) — Mismo patrón que se usó en dashboard.

## 🟢 Baja prioridad

- [ ] **Separar `ColoresTorque.kt`** (~364 líneas) — Separar los objetos de color de los composables de UI (navegación, toasts, botones).

## ✅ Completadas recientemente

- [x] **`SaludMotoCalculator.kt`** — Bucle cuadrático O(N×T) → O(N+23) con pre-agrupación por ID.
- [x] **`AppRepository.kt`** — Excepciones en escrituras críticas ahora se relanzan tras loguear.
- [x] **`SaludMotoCalculator.kt`** — `SimpleDateFormat` movido a `companion object`.
- [x] **`FormularioMantenimiento.kt`** — `withContext(Dispatchers.Main)` redundante eliminado.
- [x] **`ColoresTorque.kt`** — `FiltroChip` duplicado eliminado.
- [x] **Reemplazar `0xFFEF4444`** con `RojoError` (~20 ocurrencias).
- [x] **Reemplazar `0xFF22C55E`/`0xFFEAB308`** con `VerdeExito`/`AmarilloAdvertencia`.
- [x] **`TorqueDashboardScreen.kt`** (~992→550 líneas) — Extraídos diálogos + ViewModel.
- [x] **`TarjetaEstadisticas`** eliminada (no se usaba).

## ℹ️ Intencional (no cambiar)

- **`MantenimientoDao.kt:28` vs `SaludMotoCalculator`** — DAO ordena por fecha (para "último registro"), Calculator por kms (para cumplimiento de intervalos). Cada uno sirve a su propósito.
- **`obtenerUltimoPorTipo` (kms) vs `obtenerUltimoMantenimiento` (fecha)** — Ídem, intencional.



hacer que las fotos de la moto que esta como principal vayan cambiando cada x tiempo
mejorar la galeria de fotos de la moto
