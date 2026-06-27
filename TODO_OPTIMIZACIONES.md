# Optimizaciones pendientes

## Alto impacto

- [ ] **`SaludMotoCalculator.kt:27`** — Bucle cuadrático O(N×T). Cambiar a `Map<Long, List<MantenimientoEntity>>` agrupado una sola vez.
- [ ] **`TorqueDashboardScreen.kt:96,97,107,120`** — Reemplazar `!!` con `?.let {}` safe calls. Puede crashear si el estado cambia entre null-check y uso.
- [ ] **`AppRepository.kt`** — No tragar excepciones silenciosamente en métodos críticos (`actualizarMoto`, `eliminarMoto`, `insertarMantenimiento`).

## Medio impacto

- [ ] **`ColoresTorque.kt:241 y 359`** — `FiltroChip` está definido dos veces. Eliminar duplicado.
- [ ] **`TorqueDashboardScreen.kt:898-906`** — 6 queries secuenciales en `DialogoAjusteDesgaste`. Usar una sola consulta con `IN(...)`.
- [ ] **`TorqueDashboardScreen.kt:845`** — `TarjetaEstadisticas` nunca se usa. Eliminar.
- [ ] **`MantenimientoDao.kt:28` vs `SaludMotoCalculator.kt:28`** — Inconsistencia en "último mantenimiento": DAO ordena por fecha, Calculator por kms.
- [ ] **`MantenimientoDao.kt:22 vs 28`** — `obtenerUltimoPorTipo` ordena por kms, `obtenerUltimoMantenimiento` ordena por fecha. Unificar criterio.
- [ ] **`SaludMotoCalculator.kt:90-91`** — `SimpleDateFormat` creado en cada llamada a `parseFecha()`. Mover a `companion object`.

## Bajo impacto

- [ ] **`FormularioMantenimiento.kt:89,94`** — `withContext(Dispatchers.Main)` redundante (ya se ejecuta en Main).
- [ ] **Reemplazar `0xFFEF4444`** con la constante `RojoError` de `Color.kt` (~20 ocurrencias).
- [ ] **Reemplazar `0xFF22C55E`** con `VerdeExito` y `0xFFEAB308` con `AmarilloAdvertencia`.
- [ ] **Unificar altura de cabeceras** — mezcla 56dp y 64dp entre pantallas.
- [ ] **Crear `DialogoTorque` reutilizable** — el patrón `AlertDialog` se repite ~10 veces.

## Refactor (arquitectura)

- [ ] **`TorqueDashboardScreen.kt` (~992 líneas)** — Extraer `DialogoAjusteDesgaste`, `DialogoKm`, `DialogoNotificaciones`, `TarjetaProximoServicio`.
- [ ] **`ColoresTorque.kt` (~391 líneas)** — Separa colores de los composables de UI (navegación, toasts, botones).
- [ ] **`MotoDetalle.kt` (~490 líneas)** — Extraer diálogos a archivos separados.
