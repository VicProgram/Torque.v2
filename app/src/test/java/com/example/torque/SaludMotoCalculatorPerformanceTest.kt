package com.example.torque

import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.utils.SaludMotoCalculator
import com.example.torque.utils.TiposMantenimiento
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureNanoTime

class SaludMotoCalculatorPerformanceTest {

    private fun mant(
        nombre: String,
        kms: Int = 0,
        intervaloKms: Int = 0,
        tipoId: Long = 0,
        porcentajeVida: Int = 100,
        idMoto: Long = 1L,
        fecha: String = "01/01/2024",
        tareas: String? = null
    ) = MantenimientoEntity(
        idMoto = idMoto,
        nombreMantenimiento = nombre,
        fecha = fecha,
        kms = kms,
        intervaloKms = intervaloKms,
        tipoMantenimientoId = tipoId,
        porcentajeVida = porcentajeVida,
        tareas = tareas
    )

    private val tipos = TiposMantenimiento.TODOS

    @Test
    fun `calculo normal en menos de 10ms`() {
        val mantenimientos = tipos.map { mant(it.nombre, kms = 5000, tipoId = it.id) }
        val time = measureNanoTime {
            repeat(100) { SaludMotoCalculator.calcular(mantenimientos, kmActuales = 9500) }
        }
        val mediaMs = time / 100_000_000.0
        assertTrue("Media: ${"%.3f".format(mediaMs)}ms", mediaMs < 10)
    }

    @Test
    fun `1000 registros en menos de 100ms`() {
        val muchos = (0 until 1000).map { i ->
            val tipo = tipos[i % tipos.size]
            mant(tipo.nombre, kms = i * 100, tipoId = tipo.id)
        }
        val time = measureNanoTime {
            repeat(10) { SaludMotoCalculator.calcular(muchos, kmActuales = 50_000) }
        }
        val mediaMs = time / 10_000_000.0
        assertTrue("Media 1000 reg: ${"%.3f".format(mediaMs)}ms", mediaMs < 100)
    }

    @Test
    fun `10000 registros en menos de 500ms`() {
        val muchos = (0 until 10000).map { i ->
            val tipo = tipos[i % tipos.size]
            mant(tipo.nombre, kms = i * 50, tipoId = tipo.id)
        }
        val time = measureNanoTime {
            repeat(5) { SaludMotoCalculator.calcular(muchos, kmActuales = 500_000) }
        }
        val mediaMs = time / 5_000_000.0
        assertTrue("Media 10000 reg: ${"%.3f".format(mediaMs)}ms", mediaMs < 500)
    }

    @Test
    fun `100 llamadas no se degradan`() {
        val mantenimientos = tipos.map { mant(it.nombre, kms = 5000, tipoId = it.id) }
        val tiempos = (0 until 100).map {
            measureNanoTime { SaludMotoCalculator.calcular(mantenimientos, kmActuales = 9500) }
        }
        val mediana = tiempos.sorted()[49]
        val maximo = tiempos.max()
        assertTrue("Mediana ${mediana / 1_000_000}ms, Máx ${maximo / 1_000_000}ms",
            maximo < mediana * 10 || maximo < 100_000_000)
    }

    @Test
    fun `50 llamadas sin mantenimiento usan menos de 50ms total`() {
        val time = measureNanoTime {
            repeat(50) { SaludMotoCalculator.calcular(emptyList(), kmActuales = 0) }
        }
        assertTrue("Total 50 vacíos: ${time / 1_000_000}ms", time < 50_000_000)
    }

    @Test
    fun `carga constante de memoria tras warmup`() {
        val mantenimientos = tipos.map { mant(it.nombre, kms = 5000, tipoId = it.id) }

        System.gc()
        System.gc()
        val memAntes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        repeat(500) { SaludMotoCalculator.calcular(mantenimientos, kmActuales = 9500) }

        System.gc()
        System.gc()
        val memDespues = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        val diff = memDespues - memAntes
        val maxDiff = 2 * 1024 * 1024
        assertTrue("Fuga de memoria: ${diff / 1024}KB (límite ${maxDiff / 1024}KB)", diff < maxDiff)
    }
}
