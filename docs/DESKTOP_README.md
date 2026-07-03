# Torque Desktop

Gestion de mantenimiento de vehiculos para Windows, Mac y Linux.
Version de escritorio compatibile con la app Android Torque.

## Caracteristicas

- Dashboard con estado de salud de cada vehiculo
- Garaje: lista de motos/coches con datos basicos
- Detalle de vehiculo con historial de mantenimientos
- Importacion de backup desde Android (ZIP)
- Tema Material 3 con modo claro/oscuro

## Requisitos

- Java 17+ (JVM)
- Gradle 8.12.1 (incluido wrapper)

## Compilar y ejecutar

```bash
./gradlew :desktop:run
```

Para generar un ejecutable nativo:

```bash
./gradlew :desktop:packageDistributableForCurrentOS
```

Los archivos se generan en `desktop/build/compose/binaries/`.

## Importar datos desde Android

1. En Android: Ajustes → Exportar backup
2. En Desktop: Ajustes → Importar backup → seleccionar el archivo ZIP

El ZIP debe contener `torque_room.db` y opcionalmente `fotos/`.

## Estado del proyecto

Fase 1 — MVP solo lectura + importacion de backup.

| Funcionalidad | Estado |
|--------------|--------|
| Dashboard | Placeholder |
| Garaje (lista) | Placeholder |
| Detalle moto | Placeholder |
| Ajustes | Placeholder con import |
| Importar backup | Implementado |
| Exportar backup | Pendiente |
| CRUD completo | Pendiente |
| Plan mantenimiento | Pendiente |
| ITV / Seguro | Pendiente |
| Fotos | Pendiente |
| PDF | Pendiente |

## Arquitectura

```
:shared (modelos + algoritmos, JVM puro)
  └── :data (Android, Room)
  └── :desktop (JVM, SQLDelight)
       └── backup ZIP (formato compatible)
```
