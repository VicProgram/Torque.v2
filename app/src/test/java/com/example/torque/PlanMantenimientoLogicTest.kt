package com.example.torque

import com.example.torque.maintenance.EstadoTarea
import com.example.torque.maintenance.TareaPlan
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.utils.TiposMantenimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanMantenimientoLogicTest {

    private val hoy: String by lazy {
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    }

    private fun mant(
        nombre: String,
        kms: Int = 0,
        intervaloKms: Int = 0,
        tipoId: Long = 0,
        porcentajeVida: Int = 100,
        fecha: String = hoy,
        tareas: String? = null
    ) = MantenimientoEntity(
        idMoto = 1L,
        nombreMantenimiento = nombre,
        fecha = fecha,
        kms = kms,
        intervaloKms = intervaloKms,
        tipoMantenimientoId = tipoId,
        porcentajeVida = porcentajeVida,
        tareas = tareas
    )

    private fun calcularTarea(
        tipoId: Long,
        mantenimientos: List<MantenimientoEntity>,
        kmActuales: Int
    ): TareaPlan {
        val tipo = TiposMantenimiento.desdeId(tipoId) ?: error("Tipo $tipoId no encontrado")
        val agrupados = mantenimientos
            .filter { it.tipoMantenimientoId > 0L }
            .groupBy { it.tipoMantenimientoId }
        val ultimo = agrupados[tipo.id]?.maxByOrNull { it.kms }

        val cumplimiento = if (ultimo != null) {
            if (tipo.esDesgaste) {
                ultimo.porcentajeVida
            } else {
                var cumpl = 100
                if (tipo.intervaloKms > 0) {
                    val avanzado = kmActuales - ultimo.kms
                    cumpl = ((1f - avanzado.toFloat() / tipo.intervaloKms) * 100).toInt().coerceIn(0, 100)
                }
                cumpl
            }
        } else 100

        val proximoKms = if (ultimo != null && tipo.intervaloKms > 0) {
            ultimo.kms + tipo.intervaloKms
        } else null

        val estado = when {
            ultimo == null -> EstadoTarea.SIN_DATOS
            cumplimiento <= 0 -> EstadoTarea.VENCIDO
            cumplimiento <= 20 -> EstadoTarea.PROXIMO
            else -> EstadoTarea.OK
        }

        return TareaPlan(tipo, ultimo, cumplimiento, proximoKms, estado)
    }

    @Test
    fun `tarea sin registro es SIN_DATOS con cumplimiento 100`() {
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = emptyList(), kmActuales = 0)
        assertEquals(EstadoTarea.SIN_DATOS, tarea.estado)
        assertEquals(100, tarea.cumplimiento)
        assertNull(tarea.proximoKms)
        assertNull(tarea.ultimoRegistro)
    }

    @Test
    fun `tarea recien hecha es OK con cumplimiento 100`() {
        val lista = listOf(mant("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1))
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = lista, kmActuales = 5000)
        assertEquals(EstadoTarea.OK, tarea.estado)
        assertEquals(100, tarea.cumplimiento)
    }

    @Test
    fun `tarea al 50 por ciento del intervalo es OK`() {
        val lista = listOf(mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1))
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = lista, kmActuales = 2500)
        assertEquals(EstadoTarea.OK, tarea.estado)
        assertEquals(50, tarea.cumplimiento)
    }

    @Test
    fun `tarea al 25 por ciento es OK`() {
        val lista = listOf(mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1))
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = lista, kmActuales = 3750)
        assertEquals(EstadoTarea.OK, tarea.estado)
        assertEquals(25, tarea.cumplimiento)
    }

    @Test
    fun `tarea al 12 por ciento es PROXIMO`() {
        val lista = listOf(mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1))
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = lista, kmActuales = 4375)
        assertEquals(EstadoTarea.PROXIMO, tarea.estado)
        assertEquals(12, tarea.cumplimiento)
    }

    @Test
    fun `tarea al 75 por ciento es OK`() {
        val lista = listOf(mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1))
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = lista, kmActuales = 1250)
        assertEquals(EstadoTarea.OK, tarea.estado)
        assertEquals(75, tarea.cumplimiento)
    }

    @Test
    fun `tarea vencida al 0 por ciento es VENCIDO`() {
        val lista = listOf(mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1))
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = lista, kmActuales = 5000)
        assertEquals(EstadoTarea.VENCIDO, tarea.estado)
        assertEquals(0, tarea.cumplimiento)
    }

    @Test
    fun `tarea vencida mas alla del intervalo es VENCIDO`() {
        val lista = listOf(mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1))
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = lista, kmActuales = 10000)
        assertEquals(EstadoTarea.VENCIDO, tarea.estado)
        assertEquals(0, tarea.cumplimiento)
    }

    @Test
    fun `tarea de desgaste usa porcentajeVida`() {
        val lista = listOf(
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 30)
        )
        val tarea = calcularTarea(tipoId = 10L, mantenimientos = lista, kmActuales = 0)
        assertEquals(30, tarea.cumplimiento)
        assertEquals(EstadoTarea.OK, tarea.estado)
    }

    @Test
    fun `tarea de desgaste al 15 por ciento es PROXIMO`() {
        val lista = listOf(
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 15)
        )
        val tarea = calcularTarea(tipoId = 10L, mantenimientos = lista, kmActuales = 0)
        assertEquals(15, tarea.cumplimiento)
        assertEquals(EstadoTarea.PROXIMO, tarea.estado)
    }

    @Test
    fun `tarea de desgaste al 0 por ciento es VENCIDO`() {
        val lista = listOf(
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 0)
        )
        val tarea = calcularTarea(tipoId = 10L, mantenimientos = lista, kmActuales = 0)
        assertEquals(0, tarea.cumplimiento)
        assertEquals(EstadoTarea.VENCIDO, tarea.estado)
    }

    @Test
    fun `tarea con varios registros usa el de mayor kms`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 1000, intervaloKms = 5000, tipoId = 1),
            mant("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1),
        )
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = lista, kmActuales = 6000)
        assertEquals(80, tarea.cumplimiento)
        assertEquals(EstadoTarea.OK, tarea.estado)
        assertNotNull(tarea.ultimoRegistro)
        assertEquals(5000, tarea.ultimoRegistro!!.kms)
    }

    @Test
    fun `tarea con proximoKms calculado correctamente`() {
        val lista = listOf(mant("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1))
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = lista, kmActuales = 5000)
        assertNotNull(tarea.proximoKms)
        assertEquals(10000, tarea.proximoKms)
    }

    @Test
    fun `tarea sin intervalo de km no tiene proximoKms`() {
        val lista = listOf(mant("Batería", kms = 0, tipoId = 16)) // batería: intervaloKms=0
        val tarea = calcularTarea(tipoId = 16L, mantenimientos = lista, kmActuales = 5000)
        assertNull(tarea.proximoKms)
    }

    @Test
    fun `tarea de desgaste sin intervalo no tiene proximoKms`() {
        val lista = listOf(
            mant("Neumático Delantero", tipoId = 21, porcentajeVida = 50)
        )
        val tarea = calcularTarea(tipoId = 21L, mantenimientos = lista, kmActuales = 0)
        assertNull(tarea.proximoKms)
    }

    @Test
    fun `tarea de desgaste con intervalo si tiene proximoKms`() {
        val lista = listOf(
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 50)
        )
        val tarea = calcularTarea(tipoId = 10L, mantenimientos = lista, kmActuales = 0)
        assertNotNull(tarea.proximoKms)
        assertEquals(5000, tarea.proximoKms)
    }

    @Test
    fun `km actuales menores que km del ultimo registro da 100`() {
        val lista = listOf(mant("Cambio de Aceite Motor", kms = 10000, intervaloKms = 5000, tipoId = 1))
        val tarea = calcularTarea(tipoId = 1L, mantenimientos = lista, kmActuales = 5000)
        assertEquals(100, tarea.cumplimiento)
        assertEquals(EstadoTarea.OK, tarea.estado)
    }

    @Test
    fun `todas las tareas para todos los tipos producen resultado valido`() {
        val mantenimientos = TiposMantenimiento.TODOS.map { tipo ->
            mant(tipo.nombre, kms = 0, intervaloKms = tipo.intervaloKms, tipoId = tipo.id)
        }
        for (tipo in TiposMantenimiento.TODOS) {
            val tarea = calcularTarea(tipo.id, mantenimientos, kmActuales = 5000)
            assertNotNull(tarea)
            assertTrue("Cumplimiento de ${tipo.nombre} fuera de rango: ${tarea.cumplimiento}",
                tarea.cumplimiento in 0..100)
        }
    }

    @Test
    fun `tipo sin intervalo Kms ni desgaste y sin registro da SIN_DATOS`() {
        // Presión de Neumáticos (id=14): sin intervaloKms, sin intervaloDias, no es desgaste
        val tarea = calcularTarea(tipoId = 14L, mantenimientos = emptyList(), kmActuales = 0)
        assertEquals(EstadoTarea.SIN_DATOS, tarea.estado)
        assertEquals(100, tarea.cumplimiento)
    }

    @Test
    fun `tipo sin intervalo Kms con registro da OK`() {
        // Presión de Neumáticos (id=14): sin intervalo
        val lista = listOf(mant("Presión de Neumáticos", kms = 0, tipoId = 14))
        val tarea = calcularTarea(tipoId = 14L, mantenimientos = lista, kmActuales = 5000)
        assertEquals(EstadoTarea.OK, tarea.estado)
        assertEquals(100, tarea.cumplimiento)
    }
}
