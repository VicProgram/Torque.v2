---
name: testing
description: Use when writing or running tests — unit tests (JUnit 4), instrumented tests, test patterns for SaludMotoCalculator, PlanMantenimiento, PdfExporter, and performance tests.
---

# Testing Conventions for Torque

## Commands

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run instrumented tests (requires emulator/device)
./gradlew connectedCheck

# Run a specific test class
./gradlew testDebugUnitTest --tests "com.example.torque.SaludMotoCalculatorTest"
```

## Test Structure

Tests live in `app/src/test/java/com/example/torque/`.

### Frameworks

- **JUnit 4** (`@Test`, `@Before`, assertions)
- No mocking framework — tests use real data objects

### SaludMotoCalculatorTest (78 tests)

Located in `app/src/test/java/com/example/torque/SaludMotoCalculatorTest.kt`.

Pattern: create `MantenimientoEntity` test data, call `SaludMotoCalculator.calcular()`, assert on `ResultadoSalud`.
```kotlin
@Test
fun `sin mantenimientos salud 100`() {
    val resultado = SaludMotoCalculator.calcular(emptyList(), 5000)
    assertEquals(100, resultado.saludGlobal)
}
```

Key test categories:
- Base cases (no data → 100%)
- Linear compliance (50% interval → 50%)
- Wear types (percentage-based)
- Matching logic (by ID, name, tasks field)
- Edge cases (no interval, negative kms, null values)
- Data integrity (all 23 types have valid IDs, weights > 0)

### PlanMantenimientoLogicTest (19 tests)

Located in `app/src/test/java/com/example/torque/PlanMantenimientoLogicTest.kt`.

Tests plan task states: `SIN_DATOS`, `OK`, `PROXIMO`, `VENCIDO`.
Tests next km calculation: `ultimoCumplimientoKm + intervaloKms`.

### PdfExporterDataTest (7 tests)

Located in `app/src/test/java/com/example/torque/PdfExporterDataTest.kt`.

Tests PDF data generation: next service calculation, table rows, summaries, price parsing.

### Performance Tests

`SaludMotoCalculatorPerformanceTest.kt` — 9 tests measuring execution time and memory.
Run with `./gradlew testDebugUnitTest --tests "*.SaludMotoCalculatorPerformanceTest"`.

### Instrumented Tests

`DashboardUITest.kt` (2 tests) — requires emulator. Verifies package name and welcome text.

## Test Rules

- Keep tests pure — no Android dependencies in unit tests.
- Test data objects should match real entity shapes.
- Cover edge cases: nulls, empty lists, boundary values (0%, 100%, negative).
- Performance tests assert max execution time (e.g. < 10ms mean, < 500ms for 10K records).
