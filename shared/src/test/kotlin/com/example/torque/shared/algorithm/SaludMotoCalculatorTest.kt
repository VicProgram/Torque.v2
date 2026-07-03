package com.example.torque.shared.algorithm

import com.example.torque.shared.model.MantenimientoData
import com.example.torque.shared.model.TiposMantenimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SaludMotoCalculatorTest {

    private fun mdto(
        nombre: String,
        kms: Int = 0,
        intervaloKms: Int = 0,
        tipoId: Long = 0,
        porcentajeVida: Int = 100,
        fecha: String = "01/01/2024",
        tareas: String = "",
        id: Long = 0
    ) = object : MantenimientoData {
        override val id: Long get() = id
        override val idMoto: Long get() = 1L
        override val nombreMantenimiento: String get() = nombre
        override val fecha: String get() = fecha
        override val kms: Int get() = kms
        override val intervaloKms: Int get() = intervaloKms
        override val precio: String? get() = null
        override val tiempo: String? get() = null
        override val notas: String? get() = null
        override val tareas: String get() = tareas
        override val tipoMantenimientoId: Long get() = tipoId
        override val porcentajeVida: Int get() = porcentajeVida
        override val creadoEn: String? get() = null
    }

    @Test
    fun `sin mantenimientos la salud es 100`() {
        val res = SaludMotoCalculator.calcular(emptyList(), 0)
        assertEquals(100, res.saludGlobal)
        assertTrue(res.proximosVencidos.isEmpty())
    }

    @Test
    fun `todos los mantenimientos al dia la salud es 100`() {
        val lista = listOf(
            mdto("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1),
            mdto("Filtro de Aceite", kms = 5000, intervaloKms = 5000, tipoId = 2),
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        assertEquals(100, res.saludGlobal)
    }

    @Test
    fun `mantenimiento al 50 por ciento del intervalo`() {
        val lista = listOf(mdto("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1))
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 2500)
        val detalle = res.desglose.find { it.tipo.id == 1L }
        assertNotNull(detalle)
        assertEquals(50, detalle!!.cumplimiento)
    }

    @Test
    fun `mantenimiento vencido`() {
        val lista = listOf(mdto("Cambio de Aceite Motor", kms = 0, intervaloKms = 10000, tipoId = 1))
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 15000)
        val detalle = res.desglose.find { it.tipo.id == 1L }
        assertNotNull(detalle)
        assertEquals(0, detalle!!.cumplimiento)
    }

    @Test
    fun `tipo desgaste con porcentaje bajo`() {
        val lista = listOf(mdto("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 30))
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        val detalle = res.desglose.find { it.tipo.id == 10L }
        assertNotNull(detalle)
        assertEquals(30, detalle!!.cumplimiento)
    }

    @Test
    fun `tipo desgaste sin registro salud 100`() {
        val res = SaludMotoCalculator.calcular(emptyList(), 0)
        val pastilla = res.desglose.find { it.tipo.id == 10L }
        assertNotNull(pastilla)
        assertEquals(100, pastilla!!.cumplimiento)
    }

    @Test
    fun `varios registros para el mismo tipo usa el de mayor kms`() {
        val lista = listOf(
            mdto("Cambio de Aceite Motor", kms = 1000, intervaloKms = 5000, tipoId = 1),
            mdto("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1),
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 6000)
        val detalle = res.desglose.find { it.tipo.id == 1L }
        assertNotNull(detalle)
        assertEquals(80, detalle!!.cumplimiento)
    }

    @Test
    fun `coincide por campo tareas cuando no hay tipoId`() {
        val lista = listOf(
            mdto("Mantenimiento General", kms = 1000, intervaloKms = 5000,
                tareas = "Cambio de Aceite Motor, Filtro de Aceite")
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 3500)
        val aceite = res.desglose.find { it.tipo.id == 1L }
        assertNotNull(aceite)
        assertEquals(50, aceite!!.cumplimiento)
    }

    @Test
    fun `coincide por nombre de mantenimiento cuando tipoId es 0`() {
        val lista = listOf(
            mdto("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tareas = "Mantenimiento General")
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 6000)
        val aceite = res.desglose.find { it.tipo.id == 1L }
        assertNotNull(aceite)
        assertEquals(0, aceite!!.cumplimiento)
    }

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
        assertNull(TiposMantenimiento.desdeNombre("No Existe"))
    }

    @Test
    fun `no hay ids duplicados en TODOS`() {
        val ids = TiposMantenimiento.TODOS.map { it.id }
        assertEquals(ids.toSet().size, ids.size)
    }

    @Test
    fun `existen los 25 tipos con ids 1 a 25`() {
        val ids = TiposMantenimiento.TODOS.map { it.id }.toSet()
        for (i in 1L..25L) assertTrue("Falta el tipo con id=$i", i in ids)
        assertEquals(25, ids.size)
    }

    @Test
    fun `wear tipos estan incluidos en TODOS`() {
        val idsTodos = TiposMantenimiento.TODOS.map { it.id }.toSet()
        for (wear in TiposMantenimiento.WEAR_TIPOS) {
            assertTrue(wear.id in idsTodos)
            assertTrue(wear.esDesgaste)
        }
    }

    @Test
    fun `proximos vencidos maximo 5 elementos`() {
        val muchos = TiposMantenimiento.TODOS
            .filter { it.intervaloKms > 0 || it.esDesgaste }
            .map { tipo ->
                if (tipo.esDesgaste) mdto(tipo.nombre, tipoId = tipo.id, porcentajeVida = 10)
                else mdto(tipo.nombre, kms = 0, intervaloKms = tipo.intervaloKms, tipoId = tipo.id)
            }
        val res = SaludMotoCalculator.calcular(muchos, kmActuales = 999_999)
        assertTrue("Max 5 proximos, tiene ${res.proximosVencidos.size}", res.proximosVencidos.size <= 5)
    }

    @Test
    fun `todos los tipos tienen peso mayor a 0`() {
        for (tipo in TiposMantenimiento.TODOS) assertTrue(tipo.peso > 0)
    }

    @Test
    fun `resultado contiene 25 desgloses`() {
        val res = SaludMotoCalculator.calcular(emptyList(), 0)
        assertEquals(25, res.desglose.size)
    }

    @Test
    fun `km actuales menores que ultimo mantenimiento`() {
        val lista = listOf(mdto("Cambio de Aceite Motor", kms = 10000, intervaloKms = 5000, tipoId = 1))
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        assertEquals(100, res.desglose.find { it.tipo.id == 1L }!!.cumplimiento)
    }

    @Test
    fun `kilometraje negativo no causa error`() {
        val lista = listOf(mdto("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1))
        val res = SaludMotoCalculator.calcular(lista, kmActuales = -100)
        assertEquals(100, res.desglose.find { it.tipo.id == 1L }!!.cumplimiento)
    }

    @Test
    fun `escenario mixto salud global calculada correctamente`() {
        val lista = listOf(
            mdto("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1),
            mdto("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 50),
            mdto("Neumatico Delantero", tipoId = 21, porcentajeVida = 20),
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 6000)
        assertTrue(res.saludGlobal in 0..100)
        assertTrue(res.proximosVencidos.isNotEmpty())
    }

    @Test
    fun `todos los tipos al 100 por ciento dan salud 100`() {
        val lista = TiposMantenimiento.TODOS.filter { it.intervaloKms > 0 }.map { tipo ->
            mdto(tipo.nombre, kms = 5000, intervaloKms = tipo.intervaloKms, tipoId = tipo.id)
        } + TiposMantenimiento.WEAR_TIPOS.map { tipo ->
            mdto(tipo.nombre, tipoId = tipo.id, porcentajeVida = 100)
        }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        assertEquals(100, res.saludGlobal)
    }

    @Test
    fun `tareas null no causa error`() {
        val lista = listOf(
            object : MantenimientoData {
                override val id: Long get() = 1
                override val idMoto: Long get() = 1L
                override val nombreMantenimiento: String get() = "Registro sin categorizar"
                override val fecha: String get() = "01/01/2024"
                override val kms: Int get() = 1000
                override val intervaloKms: Int get() = 5000
                override val precio: String? get() = null
                override val tiempo: String? get() = null
                override val notas: String? get() = null
                override val tareas: String get() = ""
                override val tipoMantenimientoId: Long get() = 0
                override val porcentajeVida: Int get() = 100
                override val creadoEn: String? get() = null
            }
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 6000)
        assertEquals(100, res.desglose.find { it.tipo.id == 1L }!!.cumplimiento)
    }
}
