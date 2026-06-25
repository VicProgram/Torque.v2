# Torque 🏍️

Aplicación Android para la gestión del mantenimiento de motocicletas. Desarrollada con Jetpack Compose y Room.

## Características

- **Panel de control** — Vista general con la moto principal, salud global, kilometraje, próximo servicio y acciones rápidas.
- **Gestión de garaje** — Añade, edita y elimina motos. Cada moto con foto, marca, modelo, año, matrícula, cilindrada y potencia.
- **Registro de mantenimiento** — Selecciona tareas de una lista predefinida (cambio de aceite, filtros, cadena, neumáticos, etc.) y registra fecha, kilometraje, precio y notas.
- **Historial de mantenimiento** — Busca, filtra por categoría y consulta el detalle de cualquier registro.
- **Control de desgaste** — Ajusta el porcentaje de vida restante de consumibles (neumáticos, pastillas de freno, discos) mediante sliders intuitivos.
- **Salud de la moto** — Algoritmo que calcula un porcentaje de salud global (0–100%) ponderando todos los mantenimientos según su intervalo y peso.
- **Notificaciones** — Recibe avisos cuando un mantenimiento está próximo a vencer (1–20% restante) o ya ha vencido (0%).
- **Perfil de usuario** — Foto, nombre, email, teléfono, fecha de nacimiento y categorías del carnet de conducir.
- **Exportación de datos** — Copia de seguridad de la base de datos en la carpeta Descargas.
- **Modo oscuro** — Interfaz con tema oscuro optimizado para su uso en taller y carretera.

## Tecnologías

| Tecnología | Versión |
|---|---|
| Kotlin | 2.1.20 |
| Jetpack Compose | BOM 2025.05.01 |
| Material 3 | — |
| Room | 2.6.1 |
| DataStore Preferences | 1.1.2 |
| Coil | 2.7.0 |
| KSP | 2.1.20-1.0.32 |
| Gradle AGP | 8.9.2 |
| Min SDK / Target | 28 / 35 |

## Estructura del proyecto

```
app/src/main/java/com/example/torque/
├── MenuPrincipal/           # Navegación (NavHost)
├── ajustes/                 # Preferencias y configuración
├── components/
│   └── ui/dashboard/        # Dashboard y sistema de diseño
├── garage/                  # Lista, detalle, alta/edición de motos
├── maintenance/             # Registro, historial, formulario
├── perfil/                  # Perfil de usuario
├── room/                    # Base de datos, entidades y DAOs
│   ├── entity/              # MotoEntity, MantenimientoEntity, FotoEntity
│   │   └── dao/             # MotoDao, MantenimientoDao, FotoDao
│   ├── AppDatabase.kt
│   └── AppRepository.kt
├── ui/theme/                # Colores, tema y tipografía
└── utils/                   # Calculadoras, notificaciones y utilidades
```

## Capturas de pantalla

*(pendiente)*

## Cómo compilar

1. Abre el proyecto en Android Studio.
2. Sincroniza Gradle.
3. Selecciona un dispositivo o emulador con API 28+.
4. Ejecuta `Run` o usa el siguiente comando:

```bash
./gradlew assembleDebug
```

El APK se generará en `app/build/outputs/apk/debug/`.

## Tests

```bash
./gradlew test
```

Los tests unitarios del algoritmo de salud están en `app/src/test/java/com/example/torque/SaludMotoCalculatorTest.kt`.

## Licencia

Uso personal.
