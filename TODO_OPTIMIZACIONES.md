# Optimizaciones pendientes

## Alto impacto

- [ ] **`SaludMotoCalculator.kt:27`** — Bucle cuadrático O(N×T). Cambiar a `Map<Long, List<MantenimientoEntity>>` agrupado una sola vez.
- [ ] **`AppRepository.kt`** — No tragar excepciones silenciosamente en métodos críticos (`actualizarMoto`, `eliminarMoto`, `insertarMantenimiento`).
- [ ] **`TorqueDashboardScreen.kt`** — Revisar si hay `!!` que puedan crashear. (Antes en líneas 96,97,107,120 — refactorizadas)

## Medio impacto

- [x] **`ColoresTorque.kt`** — `FiltroChip` duplicado. Eliminado.
- [ ] **`DialogoAjusteDesgaste.kt`** — 6 queries secuenciales (antes en `TorqueDashboardScreen.kt:898-906`). Usar una sola consulta con `IN(...)`.
- [x] **`TorqueDashboardScreen.kt`** — `TarjetaEstadisticas` nunca se usa. Eliminada.
- [ ] **`MantenimientoDao.kt:28` vs `SaludMotoCalculator.kt:28`** — Inconsistencia en "último mantenimiento": DAO ordena por fecha, Calculator por kms.
- [ ] **`MantenimientoDao.kt:22 vs 28`** — `obtenerUltimoPorTipo` ordena por kms, `obtenerUltimoMantenimiento` ordena por fecha. Unificar criterio.
- [ ] **`SaludMotoCalculator.kt:90-91`** — `SimpleDateFormat` creado en cada llamada a `parseFecha()`. Mover a `companion object`.

## Bajo impacto

- [ ] **`FormularioMantenimiento.kt:89,94`** — `withContext(Dispatchers.Main)` redundante (ya se ejecuta en Main).
- [x] **Reemplazar `0xFFEF4444`** con `RojoError` (~20 ocurrencias). Hecho.
- [x] **Reemplazar `0xFF22C55E`** con `VerdeExito` y `0xFFEAB308` con `AmarilloAdvertencia`. Hecho.
- [ ] **Unificar altura de cabeceras** — mezcla 56dp y 64dp entre pantallas.
- [ ] **Crear `DialogoTorque` reutilizable** — el patrón `AlertDialog` se repite ~10 veces.

## Refactor (arquitectura)

- [x] **`TorqueDashboardScreen.kt` (~992→550 líneas)** — Extraídos `DialogoAjusteDesgaste`, `DialogoKm`, `DialogoNotificaciones` a archivos propios. Usa ViewModel.
- [ ] **`ColoresTorque.kt` (~391→364 líneas)** — Separar colores de los composables de UI (navegación, toasts, botones).
- [ ] **`MotoDetalle.kt` (~490 líneas)** — Extraer diálogos a archivos separados.

## Nuevas (identificadas durante refactor)

- [ ] **`TorqueDashboardViewModel.kt`** — `DialogoAjusteDesgaste` carga datos por su cuenta (usa `RepositorioApp.obtenerInstancia()`) en vez de recibirlos del ViewModel.
