---
name: desktop-gui
description: Use when designing desktop GUI — window management, layout patterns, keyboard accessibility, platform integration, responsive resizing, and UX conventions for desktop applications.
---

# Desktop GUI Patterns for Torque

## Window Layout Strategy

Single-window app with navigation rail + content panel:

```
┌─MenuBar─────────────────────────────────────────┐
│  Archivo    Herramientas    Ayuda                │
├────────┬────────────────────────────────────────┤
│ Nav    │                                        │
│ Rail   │         Content Panel                   │
│        │                                        │
│ 🏍     │   (cambia segun seleccion)              │
│ 🔧     │                                        │
│ ⚙      │                                        │
│        │                                        │
├────────┴────────────────────────────────────────┤
│  Status bar (opcional)                          │
└─────────────────────────────────────────────────┘
```

## Responsive Resizing

Desktop windows resize freely. Use these patterns:

```kotlin
@Composable
fun ContentPane(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < 600.dp) {
            // Layout compacto (stack vertical)
            Column { /* ... */ }
        } else {
            // Layout expandido (paneles lado a lado)
            Row { /* ... */ }
        }
    }
}
```

## Navigation Rail

```kotlin
@Composable
fun AppNavigationRail(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationRail {
        NavigationRailItem(
            selected = currentScreen == Screen.DASHBOARD,
            onClick = { onNavigate(Screen.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, "Dashboard") },
            label = { Text("Panel") }
        )
        NavigationRailItem(
            selected = currentScreen == Screen.GARAJE,
            onClick = { onNavigate(Screen.GARAJE) },
            icon = { Icon(Icons.Default.DirectionsBike, "Garaje") },
            label = { Text("Garaje") }
        )
        // ...
    }
}
```

## Lists and Tables

- Use `LazyColumn` for vertical scrolling lists (same as Android).
- Use `DataTable` (Material3) for tabular data:
```kotlin
@Composable
fun TablaMantenimientos(mantenimientos: List<MantenimientoData>) {
    DataTable {
        HeaderRow {
            HeaderCell("Fecha")
            HeaderCell("Tarea")
            HeaderCell("Km")
            HeaderCell("Precio")
        }
        for (m in mantenimientos) {
            Row {
                Cell(m.fecha)
                Cell(m.nombreMantenimiento)
                Cell(m.kms.toString())
                Cell(m.precio ?: "—")
            }
        }
    }
}
```

## Dialogs and Modals

```kotlin
@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirmar) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
```

## Form Fields

Same `OutlinedTextField`, `DropdownMenu`, `Checkbox`, `Switch` as Android Compose. API is identical:

```kotlin
OutlinedTextField(
    value = marca,
    onValueChange = { marca = it },
    label = { Text("Marca") },
    modifier = Modifier.fillMaxWidth()
)
```

## Keyboard Navigation

```kotlin
@Composable
fun AppContent() {
    var focusState by remember { mutableStateOf(0) }

    Box(modifier = Modifier.onKeyEvent { event ->
        when {
            event.key == Key.Tab && event.type == KeyEventType.KeyDown -> {
                focusState = (focusState + 1) % 3
                true
            }
            event.key == Key.Enter && event.type == KeyEventType.KeyDown -> {
                // confirm action
                true
            }
            else -> false
        }
    }) {
        // focused elements
    }
}
```

## Data Organization (Grid vs List)

- **Dashboard**: Grid/cards for motorcycles (2-4 columns depending on window width).
- **Garaje**: List with thumbnail + info (single column, scrollable).
- **Mantenimientos**: DataTable (tabular) for compact info display.
- **Detalle Moto**: Two-panel layout: photo left, specs right.

## Color Scheme

Use the same dark palette from Android (`TorquePrimary = #135BEC`, `Background = #101622`, etc.). The `TorqueThemeDesktop` wraps `MaterialTheme` with the same colors.

## Desktop-Specific Components

| Desktop Feature | Implementation |
|----------------|---------------|
| Menu bar | `MenuBar { Menu("Archivo") { Item("Salir") } }` |
| Status bar | `BottomAppBar` or custom `Row` at bottom |
| Tooltips | `Box { Text("hover"); TooltipArea({ Text("Ayuda") }) { Icon(...) } }` |
| Context menus | `ContextMenuDataProvider` or `DropdownMenu` on right-click |
| Progress indicators | `LinearProgressIndicator` (same as Android) |
| Tabs | `TabRow` + `HorizontalPager` |
| Split panes | Manual `Row` with `Layout` or `Box` with draggable divider |
| Search field | `TextField` with `Icons.Default.Search` |
| Sortable columns | Click on header cell to toggle sort order |

## Performance

- Use `remember` and `derivedStateOf` to avoid recomposition.
- For large lists, `LazyColumn` with `key` parameter.
- Avoid `Modifier.weight()` in deeply nested layouts (use `fillMaxWidth` + fixed heights).
- Keep state as close to where it's used as possible.
