# Plan: Añadir soporte para coches a Torque.v2

## Principios
- `MotoEntity` NO se renombra (seguridad datos existentes)
- `tipoVehiculo` se implementa como **enum** + **TypeConverter** (profesional)
- Cada archivo que se toque por necesidad se actualiza nominalmente (refactor progresivo)
- Migration test obligatorio
- Tests de unidad para ambos tipos de vehículo

---

## Fase 0 — Modelo de datos (seguro)

| Paso | Archivo | Cambio |
|------|---------|--------|
| 0.1 | `core/.../utils/TiposMantenimiento.kt` | Crear `enum class TipoVehiculo { MOTO, COCHE }`. Añadir `val tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO` a `TipoMantenimiento` |
| 0.2 | `data/.../room/entity/MotoEntity.kt` | Añadir `val tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO`, más campos coche: `tipoCarroceria: String?`, `tipoCombustible: String?`, `tipoTransmision: String?`, `numeroPuertas: Int?` |
| 0.3 | **Nuevo**: `data/.../room/Convertidores.kt` | TypeConverters para `TipoVehiculo` y campos nullable. Registrar en `BaseDeDatosApp` con `@TypeConverters` |
| 0.4 | `data/.../room/AppDatabase.kt` | Migración 4→5: `ALTER TABLE motos ADD COLUMN tipoVehiculo TEXT NOT NULL DEFAULT 'MOTO'` + resto columnas |
| 0.5 | `data/.../room/entity/dao/MotoDao.kt` | Añadir `obtenerPorTipoFlow(tipoVehiculo)` y `contarPorTipo(tipoVehiculo)` |
| 0.6 | `data/.../room/AppRepository.kt` | Propagar nuevos métodos del DAO |

---

## Fase 1 — Marcas y modelos

| Paso | Archivo | Cambio |
|------|---------|--------|
| 1.1 | `app/.../garage/MarcasModelos.kt` | Añadir `object MarcasModelosCoche` (~25 marcas con modelos). Función `obtenerMarcasModelos(tipo: TipoVehiculo)` que retorna el map correcto |
| 1.2 | `app/.../garage/AnadirMotoActivity.kt` | Selector de tipo vehículo al inicio (chips). Selectores marca/modelo dependientes del tipo. Campos coche condicionales (carrocería, combustible, transmisión, puertas). Guardar `tipoVehiculo` en el `MotoEntity` |

---

## Fase 2 — Tipos de mantenimiento de coche

| Paso | Archivo | Cambio |
|------|---------|--------|
| 2.1 | `core/.../utils/TiposMantenimiento.kt` | Duplicar 25 tipos actuales como `MOTO`. Añadir ~18 tipos para `COCHE`: aceite motor 8.000 km, filtro aceite, bujías, filtro aire, correa distribución (60.000 km), refrigerante (40.000 km), aceite cambio manual/automático, pastillas/discos freno, líquido frenos (40.000 km), presión neumáticos, cambio neumáticos, rotación neumáticos (10.000 km), batería, luces, ECU, filtro habitáculo (15.000 km), gas aire acondicionado (2 años), líquido dirección (60.000 km), filtro gasóleo (30.000 km) |
| 2.2 | `app/.../maintenance/RegistroMantenimiento.kt` | Recibir `tipoVehiculo`. Filtrar tareas por tipo. Añadir secciones coche (climatización, combustible, dirección) |
| 2.3 | `app/.../maintenance/PlanMantenimiento.kt` | `cargar()` filtra `TiposMantenimiento.TODOS` por `tipoVehiculo` |
| 2.4 | `data/.../utils/SaludMotoCalculator.kt` | `calcular()` recibe `tipoVehiculo` y solo considera tipos relevantes |
| 2.5 | `app/.../components/ui/dashboard/DialogoAjusteDesgaste.kt` | `WEAR_TIPOS` filtrado por `tipoVehiculo` |

---

## Fase 3 — UI: textos, garaje, detalle, dashboard

| Paso | Archivo | Cambio |
|------|---------|--------|
| 3.1 | `app/src/main/res/values/strings.xml` | Añadir strings: `type_motorcycle`, `type_car`, `body_type`, `fuel_type`, `transmission_type`, `num_doors`, tipos carrocería/combustible/transmisión, `category_climate`, `category_steering`, `category_fuel`, tareas de coche con descripciones. Actualizar genéricos: `menu_add_vehicle`, `garage_title` → "Mis Vehículos", `no_bike` → "Sin Vehículo" |
| 3.2 | `app/src/main/res/values-en/strings.xml` | Traducciones al inglés |
| 3.3 | `app/.../garage/ListaGaraje.kt` | `TarjetaMoto` con icono según tipo. Título "Mis Vehículos". Chip filtro: Todos / Motos / Coches |
| 3.4 | `app/.../garage/MotoDetalle.kt` | Mostrar tipo vehículo y campos coche en especificaciones |
| 3.5 | `app/.../components/ui/dashboard/TorqueDashboardScreen.kt` | Icono vehículo según tipo. "Tu vehículo principal" |
| 3.6 | `app/.../components/ui/dashboard/CabeceraDashboard.kt` | "Añadir Vehículo" |
| 3.7 | `app/.../components/ui/dashboard/TarjetaProximoServicio.kt` | "SALUD DEL VEHÍCULO" |

---

## Fase 4 — Perfil (carnets)

| Paso | Archivo | Cambio |
|------|---------|--------|
| 4.1 | `app/.../perfil/PerfilActivity.kt` | Añadir chips B, C (coche) junto a AM, A1, A2, A |
| 4.2 | `data/.../perfil/PerfilRepository.kt` | Validar nuevos valores en `CARNETS_DISPONIBLES` |

---

## Fase 5 — PDF, notificaciones, tests

| Paso | Archivo | Cambio |
|------|---------|--------|
| 5.1 | `data/.../utils/PdfExporter.kt` | Incluir tipo vehículo en subtítulo |
| 5.2 | **Nuevo**: test migración 4→5 | Crear BD v4, insertar moto, migrar a v5, verificar `tipoVehiculo = "MOTO"` |
| 5.3 | `PlanMantenimientoLogicTest.kt` | Tests plan con datos de coche |
| 5.4 | `SaludMotoCalculatorTest.kt` | Tests calculadora con datos de coche |
| 5.5 | `DashboardUITest.kt` | Verificar textos nuevos |

---

## Fase 6 — Limpieza nominal progresiva

- Cada archivo que se toque: parámetros/funciones con nombre "moto" que ya no correspondan se renombran en ese mismo PR
- Sin refactor masivo global
- Ejemplo: en `ListaGaraje.kt`, `TarjetaMoto` → `TarjetaVehiculo`

---

## Archivos a tocar (~40)

| Módulo | Archivos |
|--------|----------|
| **core** | `TiposMantenimiento.kt` (+enum, +18 tipos coche) |
| **data** | `MotoEntity.kt`, `AppDatabase.kt` (+migración), `MotoDao.kt`, `AppRepository.kt`, `SaludMotoCalculator.kt`, `PdfExporter.kt`, `PerfilRepository.kt`, **nuevo**: `Convertidores.kt` |
| **app** | `AnadirMotoActivity.kt`, `MarcasModelos.kt`, `ListaGaraje.kt`, `MotoDetalle.kt`, `RegistroMantenimiento.kt`, `PlanMantenimiento.kt`, `TorqueDashboardScreen.kt`, `TorqueDashboardViewModel.kt`, `CabeceraDashboard.kt`, `TarjetaProximoServicio.kt`, `DialogoAjusteDesgaste.kt`, `PerfilActivity.kt` |
| **res** | `strings.xml`, `values-en/strings.xml` |
| **test** | **nuevo**: `Migracion4a5Test.kt`; actualizar: `PlanMantenimientoLogicTest.kt`, `SaludMotoCalculatorTest.kt`, `DashboardUITest.kt` |
