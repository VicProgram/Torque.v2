# Optimizaciones pendientes

## 🔴 Alta prioridad

- [ ] **`MotoDetalle.kt` — 13 usos de `!!` en `moto!!`** — Aunque hay null-check previo, en recomposición puede crashear. Usar `?.let` o `requireNotNull()`.
- [ ] **`PerfilActivity.kt` — 4 usos de `!!`** — `nombre!!`, `email!!`, `telefono!!`, `fechaNacimiento!!`.
- [ ] **`MantenimientoHistorial.kt` — 2 usos de `!!`** — `itemSeleccionado!!`, `itemAEliminar!!`.
- [ ] **`TorqueDashboardViewModel.kt:183` — `as Application` (unsafe cast)** — Usar `as?` con fallback.

## 🟡 Media prioridad

- [ ] **Galería de fotos — carrusel automático** — Que las fotos de la moto principal cambien cada X segundos en el dashboard.
- [ ] **Galería de fotos — mejorar gestión** — Poder ver, eliminar y reordenar fotos desde la pantalla de detalle de moto.
- [ ] **`AjustesActivity.kt` — extraer ~15 strings hardcoded** a `strings.xml` (toasts, botones, títulos).
- [ ] **`PerfilActivity.kt` — extraer ~10 strings hardcoded** a `strings.xml`.
- [ ] **`OpcionesMantenimiento.kt` — extraer ~6 strings hardcoded** a `strings.xml`.
- [ ] **`MotoDetalle.kt:391` — `File.delete()` en main thread** — Envolver en `Dispatchers.IO`.
- [ ] **`NotificacionSalud.kt:40` — parámetro `kmActuales` no usado** — Eliminar o implementar.

## 🟢 Baja prioridad

- [ ] **Separar `ColoresTorque.kt` (~400 líneas)** — Mover colores a un archivo, UI Components a otro.
- [ ] **`MarcasModelos.kt` (~678 líneas)** — Mover datos estáticos a JSON en `res/raw/`.
- [ ] **`TorqueDashboardScreen.kt` (~763 líneas)** — Dividir en archivos más pequeños.
- [ ] **Strings hardcoded en `ListaGaraje.kt`, `FormularioMantenimiento.kt`, `TorqueDashboardScreen.kt`** — Extraer a `strings.xml`.
- [ ] **Duplicación tareas: `RegistroMantenimiento.kt` vs `TiposMantenimiento.kt`** — Unificar fuentes de datos.

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
