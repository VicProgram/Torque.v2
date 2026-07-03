# Torque Desktop — Guia de Desarrollo

## Stack

| Componente | Version |
|------------|---------|
| Kotlin | 2.1.20 (JVM) |
| Compose Desktop | 1.7.3 (Material 3) |
| SQLDelight | 2.0.2 (SQLite) |
| Gradle | 8.12.1 |

## Estructura del modulo `:desktop`

```
desktop/
├── build.gradle.kts
└── src/main/
    ├── kotlin/com/example/torque/desktop/
    │   ├── Main.kt                          # Entry point (fun main())
    │   ├── App.kt                           # Composable raiz con navegacion
    │   ├── navigation/
    │   │   └── Destinos.kt                  # Sealed class de rutas
    │   ├── ui/
    │   │   ├── theme/Color.kt              # Paleta de colores
    │   │   ├── theme/Theme.kt              # TorqueThemeDesktop
    │   │   ├── components/                 # Componentes reutilizables
    │   │   │   ├── BotonTorque.kt
    │   │   │   ├── TarjetaTorque.kt
    │   │   │   ├── DialogoTorque.kt
    │   │   │   ├── CampoTextoTorque.kt
    │   │   │   └── EncabezadoSeccion.kt
    │   │   └── screens/                    # Pantallas
    │   │       ├── DashboardScreen.kt       # Resumen / inicio
    │   │       ├── GarajeScreen.kt          # Lista de vehiculos
    │   │       ├── MotoDetalleScreen.kt     # Detalle + mantenimientos
    │   │       └── AjustesScreen.kt         # Configuracion / importar backup
    │   ├── data/
    │   │   ├── DesktopRepositorio.kt        # Fachada de acceso a datos
    │   │   └── database/
    │   │       └── DatabaseFactory.kt       # Creacion de instancia SQLDelight
    │   └── sync/
    │       └── BackupImporter.kt            # Importar ZIP de backup Android
    └── sqldelight/com/example/torque/desktop/db/
        └── Torque.sq                        # Schema SQL e identicas a Room Android
```

## Modulo `:shared` (logica comun)

Modelos y algoritmos compartidos entre Android y Desktop:

```
shared/src/main/kotlin/com/example/torque/shared/
├── model/
│   ├── MotoData.kt
│   ├── MantenimientoData.kt
│   ├── MantenimientoDto.kt
│   ├── FotoData.kt
│   ├── FotoMantenimientoData.kt
│   ├── PlanData.kt
│   ├── ItvSeguroData.kt
│   ├── SaludModels.kt
│   └── TipoMantenimiento.kt
├── algorithm/
│   └── SaludMotoCalculator.kt
└── util/
    └── DateUtils.kt
```

## Base de datos (SQLDelight)

El schema en `Torque.sq` replica exactamente las tablas de Room en Android:

| Tabla | Columnas clave |
|-------|----------------|
| `motos` | idMoto, marca, modelo, anio, ..., fechaItv, fechaSeguro, tipoVehiculo... |
| `mantenimientos` | id, idMoto, nombreMantenimiento, fecha, kms, tipoMantenimientoId, porcentajeVida |
| `fotos_moto` | id, idMoto, ruta, creadoEn |
| `fotos_mantenimiento` | id, idMantenimiento, ruta, tipo, creadoEn |

## Sincronizacion (Backup File Sync)

Android exporta un ZIP (`BackupManager.kt`) con:
- `torque_room.db` — SQLite con las 4 tablas
- `fotos/` — imagenes asociadas
- `preferencias/` — DataStore (ignorado en desktop)

Desktop lee el mismo ZIP y puebla SQLDelight via `BackupImporter`.

## Convenciones de codigo

- **Estado**: `mutableStateOf` + `remember` (no hay ViewModel en Desktop)
- **Navegacion**: `mutableStateOf<Destino>` con `when` (no hay NavHost)
- **DI**: Manual (singletons via `object` o paso por constructor)
- **Tema**: Copia fiel del tema Android (`TorqueThemeDesktop`)

## Comandos utiles

```bash
# Compilar
./gradlew :desktop:compileKotlinDesktop

# Ejecutar
./gradlew :desktop:run

# Limpiar
./gradlew :desktop:clean
```
