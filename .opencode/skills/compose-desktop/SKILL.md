---
name: compose-desktop
description: Use when writing Compose Desktop code — Window, menus, file dialogs, system tray, keyboard navigation, packaging, and Desktop-specific Compose patterns.
---

# Compose Desktop Conventions for Torque Desktop

## Key Differences from Android Compose

| Android | Desktop | Notes |
|---------|---------|-------|
| `fun Activity.onCreate()` | `fun main() = application { Window(...) }` | Entry point: JVM `main`, not Activity |
| `setContent { }` | `Window(onCloseRequest = ::exitApplication) { }` | Window replaces Activity |
| `Modifier.systemBarsPadding()` | No equivalent | Desktop has no system bars |
| `rememberLauncherForActivityResult` | `fileChooser()` (compose-desktop API) | File pickers are built-in |
| `ActivityResultContracts` | `java.awt.FileDialog` or Compose `FileDialog` | Different API |
| `Context` everywhere | No `Context` — use composition local or parameters | Desktop has no Android context |
| `NavHost` (Compose Navigation) | `remember` + `mutableStateOf` or custom nav | No Compose Navigation for Desktop (use manual state or Voyager) |
| `coil` for images | `ImageIO.read()` or `Skia` `loadImageBitmap()` | No Coil on desktop |
| `R.string.xxx` | Hardcoded strings or resource bundle | No Android resources |

## Window Configuration

```kotlin
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Torque - Gestion de Mantenimiento",
        state = rememberWindowState(
            width = 1200.dp,
            height = 800.dp,
            position = WindowPosition(Alignment.Center)
        )
    ) {
        TorqueThemeDesktop {
            App()
        }
    }
}
```

## Navigation Pattern

No NavHost available. Use simple state-based navigation:

```kotlin
enum class Screen { DASHBOARD, GARAJE, MOTO_DETALLE, AJUSTES }

@Composable
fun App() {
    var screen by remember { mutableStateOf(Screen.DASHBOARD) }
    var selectedMotoId by remember { mutableStateOf<Long?>(null) }

    Row {
        NavigationRail { /* items que cambian screen */ }
        when (screen) {
            Screen.DASHBOARD -> DashboardScreen(onIrGaraje = { screen = Screen.GARAJE })
            Screen.GARAJE -> GarajeScreen(onSeleccionarMoto = { id -> selectedMotoId = id; screen = Screen.MOTO_DETALLE })
            Screen.MOTO_DETALLE -> MotoDetalleScreen(selectedMotoId!!)
            Screen.AJUSTES -> AjustesScreen()
        }
    }
}
```

## Common Desktop APIs

### File Dialogs
```kotlin
// Desktop Compose does not have built-in file dialog composables.
// Use java.awt.FileDialog via remember:
val fileChooser = remember {
    FileDialog(null as Frame?, "Seleccionar backup", FileDialog.LOAD).apply {
        file = "*.zip"
        isVisible = true  // blocking
    }
}
// Or use a JFileChooser wrapper.
```

### Keyboard Shortcuts
```kotlin
onKeyEvent = { event ->
    if (event.key == Key.C && event.isCtrlPressed) {
        // copy
        true
    } else false
}
```

### Menu Bar
```kotlin
MenuBar {
    Menu("Archivo") {
        Item("Importar backup", onClick = { mostrarSelectorArchivo = true })
        Item("Exportar backup", onClick = { mostrarSelectorGuardado = true })
        Separator()
        Item("Salir", onClick = ::exitApplication)
    }
    Menu("Ayuda") {
        Item("Acerca de Torque", onClick = { mostrarAcercaDe = true })
    }
}
```

### System Tray
```kotlin
if (SystemTray.isSupported()) {
    val tray = SystemTray.getSystemTray()
    val popup = PopupMenu()
    // ... add items
    val icon = TrayIcon(ImageIO.read(File("icon.png")), "Torque", popup)
    tray.add(icon)
}
```

## State Management

- No ViewModel (Android only). Use simple composable state or a manual observable pattern.
- For complex screens, create plain classes with `mutableStateOf` or `StateFlow`:

```kotlin
class DashboardState(
    private val repositorio: DesktopRepositorio
) {
    var motos by mutableStateOf<List<MotoData>>(emptyList())
        private set

    fun cargar() {
        motos = repositorio.obtenerMotos()
    }
}

@Composable
fun rememberDashboardState(repositorio: DesktopRepositorio): DashboardState {
    return remember { DashboardState(repositorio).also { it.cargar() } }
}
```

## Packaging

```kotlin
// build.gradle.kts
compose.desktop {
    application {
        mainClass = "com.example.torque.desktop.MainKt"
        nativeDistributions {
            targetFormats(Dmg, Deb, Msi, Exe)
            packageName = "Torque"
            packageVersion = "1.0.0"
            description = "Gestion de mantenimiento de motocicletas"
            vendor = "Torque"
            windows {
                menuGroup = "Torque"
                upgradeUuid = "some-uuid"
            }
            macOS {
                bundleID = "com.example.torque.desktop"
            }
            linux {
                packageName = "torque-desktop"
            }
        }
    }
}
```

## Images (no Coil)

Use Skia/Compose native image loading:

```kotlin
@Composable
fun FotoMoto(ruta: String?, modifier: Modifier = Modifier) {
    val bitmap = remember(ruta) {
        ruta?.let { org.jetbrains.skia.Image.makeFromEncoded(File(it).readBytes()).toComposeImageBitmap() }
    }
    bitmap?.let {
        Image(it, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
    } ?: PlaceholderIcon()
}
```

## Do NOT use

- `View`, `ViewGroup`, `findViewById`, `LayoutInflater` — Compose only
- `Activity`, `Fragment`, `Context` — Desktop has none of these
- `NavHost` / `NavController` — Not available on Desktop; use manual state
- `rememberLauncherForActivityResult` — Use file dialogs instead
- `Coil`, `Glide`, `Picasso` — Use Skia image loading
- `Room` — Use SQLDelight
- `DataStore` — Use Preferences API or JSON files
- `AndroidViewModel` — Does not exist on Desktop
