# Optimizaciones pendientes

## 🟡 Media prioridad

- [ ] **Galería de fotos — carrusel automático** — Que las fotos de la moto principal cambien cada X segundos en el dashboard.
- [ ] **Galería de fotos — mejorar gestión** — Poder ver, eliminar y reordenar fotos desde la pantalla de detalle de moto.

## 🟢 Baja prioridad

- [ ] **Separar `ColoresTorque.kt`** — Separar los objetos de color de los composables de UI (navegación, toasts, botones).

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
