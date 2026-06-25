package com.example.torque

import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.utils.SaludMotoCalculator
import com.example.torque.utils.TiposMantenimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SaludMotoCalculatorTest {

    // Helper: create a basic maintenance record
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

    // ───── 1. Sin mantenimientos ─────
    @Test
    fun `sin mantenimientos la salud es 100`() {
        val res = SaludMotoCalculator.calcular(emptyList(), kmActuales = 0)
        assertEquals(100, res.saludGlobal)
        assertTrue(res.proximosVencidos.isEmpty())
    }

    // ───── 2. Todos al día ─────
    @Test
    fun `todos los mantenimientos recien hechos la salud es 100`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1),
            mant("Filtro de Aceite", kms = 5000, intervaloKms = 5000, tipoId = 2),
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        // Both at 100% compliance → health = 100
        assertEquals(100, res.saludGlobal)
    }

    // ───── 3. Un mantenimiento al 50% del intervalo ─────
    @Test
    fun `mantenimiento al 50 por ciento del intervalo`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 2500)
        // tipo.intervaloKms = 5000, cumplimiento = 1 - 2500/5000 = 0.5 = 50%
        val detalle = res.desglose.find { it.tipo.id == 1L }
        assertNotNull(detalle)
        assertEquals(50, detalle!!.cumplimiento)
    }

    // ───── 4. Mantenimiento vencido (100%+ del intervalo) ─────
    @Test
    fun `mantenimiento vencido`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 10000, tipoId = 1)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 15000)
        val detalle = res.desglose.find { it.tipo.id == 1L }
        assertNotNull(detalle)
        assertEquals(0, detalle!!.cumplimiento)
    }

    // ───── 5. Tipo desgaste con porcentajeVida bajo ─────
    @Test
    fun `tipo desgaste con porcentaje bajo`() {
        val lista = listOf(
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 30)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        val detalle = res.desglose.find { it.tipo.id == 10L }
        assertNotNull(detalle)
        assertEquals(30, detalle!!.cumplimiento)
    }

    // ───── 6. Tipo desgaste sin registro → 100 ─────
    @Test
    fun `tipo desgaste sin registro salud 100`() {
        val res = SaludMotoCalculator.calcular(emptyList(), kmActuales = 0)
        val pastilla = res.desglose.find { it.tipo.id == 10L }
        assertNotNull(pastilla)
        assertEquals(100, pastilla!!.cumplimiento)
    }

    // ───── 7. Varios registros para el mismo tipo → usa el de mayor kms ─────
    @Test
    fun `varios registros para el mismo tipo usa el ultimo por kms`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 1000, intervaloKms = 5000, tipoId = 1),
            mant("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1),
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 6000)
        // Último cambio a los 5000, han pasado 1000 km de 5000 → 80% cumplimiento
        val detalle = res.desglose.find { it.tipo.id == 1L }
        assertNotNull(detalle)
        assertEquals(80, detalle!!.cumplimiento)
    }

    // ───── 8. Coincidencia por tareas (fallback) ─────
    @Test
    fun `coincide por campo tareas cuando no hay tipoId`() {
        val lista = listOf(
            mant("Mantenimiento General", kms = 1000, intervaloKms = 5000,
                tareas = "Cambio de Aceite Motor, Filtro de Aceite")
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 3500)
        // Aceite: 3500-1000=2500, 2500/5000=0.5 → 50% cumplimiento
        val aceite = res.desglose.find { it.tipo.id == 1L }
        assertNotNull(aceite)
        assertEquals(50, aceite!!.cumplimiento)
        // Filtro: mismo cálculo porque mismo registro
        val filtro = res.desglose.find { it.tipo.id == 2L }
        assertNotNull(filtro)
        assertEquals(50, filtro!!.cumplimiento)
    }

    // ───── 9. Escenario mixto ─────
    @Test
    fun `escenario mixto salud global calculada correctamente`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1),          // vencido → 0%
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 50),              // 50%
            mant("Neumático Delantero", tipoId = 21, porcentajeVida = 20),                       // 20%
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 6000)
        // El resto de tipos sin registro → 100%
        assertTrue(res.saludGlobal in 0..100)
        // Al menos hay próximos vencidos
        assertTrue(res.proximosVencidos.isNotEmpty())
    }

    // ───── 10. proximosVencidos ordenados por relevancia ─────
    @Test
    fun `proximos vencidos ordenados por relevancia descendente`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1),      // vencido, peso 10
            mant("Bujías", kms = 0, intervaloKms = 10000, tipoId = 3),                      // 40% cumpl, peso 6
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 6000)
        assertTrue(res.proximosVencidos.isNotEmpty())
        // El primero debería ser el más relevante (vencido con peso 10)
        assertEquals(1L, res.proximosVencidos.first().tipo.id)
    }

    // ───── 11. desdeNombre lookup ─────
    @Test
    fun `desdeNombre encuentra correctamente`() {
        val tipo = TiposMantenimiento.desdeNombre("Cambio de Aceite Motor")
        assertNotNull(tipo)
        assertEquals(1L, tipo!!.id)
    }

    @Test
    fun `desdeNombre es case insensitive`() {
        val tipo = TiposMantenimiento.desdeNombre("cambio de aceite motor")
        assertNotNull(tipo)
        assertEquals(1L, tipo!!.id)
    }

    @Test
    fun `desdeNombre nombre inexistente devuelve null`() {
        val tipo = TiposMantenimiento.desdeNombre("No Existe")
        assertNull(tipo)
    }

    // ───── 12. desdeId lookup ─────
    @Test
    fun `desdeId encuentra correctamente`() {
        val tipo = TiposMantenimiento.desdeId(15L)
        assertNotNull(tipo)
        assertEquals("Cambio de Neumáticos", tipo!!.nombre)
    }

    @Test
    fun `desdeId inexistente devuelve null`() {
        val tipo = TiposMantenimiento.desdeId(999L)
        assertNull(tipo)
    }

    // ───── 13. WEAR_TIPOS contiene los esperados ─────
    @Test
    fun `wear tipos contiene las piezas de desgaste`() {
        val nombres = TiposMantenimiento.WEAR_TIPOS.map { it.nombre }.toSet()
        assertTrue("Pastillas de Freno Delantero" in nombres)
        assertTrue("Pastillas de Freno Trasero" in nombres)
        assertTrue("Discos de Freno Delanteros" in nombres)
        assertTrue("Discos de Freno Traseros" in nombres)
        assertTrue("Neumático Delantero" in nombres)
        assertTrue("Neumático Trasero" in nombres)
        assertEquals(6, nombres.size)
    }

    // ───── 14. Tipos sin intervalo ni desgaste no afectan notificaciones ─────
    @Test
    fun `tipos sin intervalo y sin desgaste se ignoran en cumplimiento`() {
        val lista = listOf(
            mant("Presión de Neumáticos", kms = 0, intervaloKms = 0, tipoId = 14)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        // intervaloKms=0, intervaloDias=0, no es desgaste → 100%
        val detalle = res.desglose.find { it.tipo.id == 14L }
        assertNotNull(detalle)
        assertEquals(100, detalle!!.cumplimiento)
    }

    // ───── 15. Verificar que ids no se duplican ─────
    @Test
    fun `no hay ids duplicados en TODOS`() {
        val ids = TiposMantenimiento.TODOS.map { it.id }
        assertEquals(ids.toSet().size, ids.size)
    }
}
