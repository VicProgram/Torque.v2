package com.example.torque

import com.example.torque.room.entity.MantenimientoEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class PdfExporterDataTest {

    private fun mant(
        nombre: String,
        kms: Int = 0,
        intervaloKms: Int = 0,
        precio: String? = null,
        notas: String? = null,
        fecha: String = "01/01/2024"
    ) = MantenimientoEntity(
        idMoto = 1L,
        nombreMantenimiento = nombre,
        fecha = fecha,
        kms = kms,
        intervaloKms = intervaloKms,
        precio = precio,
        notas = notas
    )

    @Test
    fun `proxServicio con intervalo positivo muestra km futuro`() {
        val m = mant("Test", kms = 5000, intervaloKms = 5000)
        val prox = if (m.intervaloKms > 0) "${m.kms + m.intervaloKms}" else "—"
        assertEquals("10000", prox)
    }

    @Test
    fun `proxServicio con intervalo cero muestra guion`() {
        val m = mant("Test", kms = 5000, intervaloKms = 0)
        val prox = if (m.intervaloKms > 0) "${m.kms + m.intervaloKms}" else "—"
        assertEquals("—", prox)
    }

    @Test
    fun `fila de mantenimiento con datos completos`() {
        val m = mant("Cambio de Aceite Motor",
            kms = 10000, intervaloKms = 5000, precio = "45€",
            notas = "Aceite 10W40", fecha = "15/06/2024")
        val prox = if (m.intervaloKms > 0) "${m.kms + m.intervaloKms}" else "—"
        val fila = listOf(
            m.fecha.take(10),
            m.nombreMantenimiento,
            "${m.kms}",
            m.precio ?: "—",
            prox,
            m.notas ?: ""
        )
        assertEquals("15/06/2024", fila[0])
        assertEquals("Cambio de Aceite Motor", fila[1])
        assertEquals("10000", fila[2])
        assertEquals("45€", fila[3])
        assertEquals("15000", fila[4])
        assertEquals("Aceite 10W40", fila[5])
    }

    @Test
    fun `fila de mantenimiento con precio y notas nulos`() {
        val m = mant("Bujías", kms = 20000, intervaloKms = 0, precio = null, notas = null)
        val prox = if (m.intervaloKms > 0) "${m.kms + m.intervaloKms}" else "—"
        val fila = listOf(
            m.fecha.take(10),
            m.nombreMantenimiento,
            "${m.kms}",
            m.precio ?: "—",
            prox,
            m.notas ?: ""
        )
        assertEquals("Bujías", fila[1])
        assertEquals("—", fila[3])
        assertEquals("—", fila[4])
        assertEquals("", fila[5])
    }

    @Test
    fun `fila con fecha larga se trunca a 10 caracteres`() {
        val m = mant("Test", fecha = "15/06/2024 Extra")
        assertEquals("15/06/2024", m.fecha.take(10))
    }

    @Test
    fun `resumen total de registros y gasto`() {
        val lista = listOf(
            mant("A", precio = "10"),
            mant("B", precio = "20"),
            mant("C", precio = "30"),
        )
        val totalRegistros = lista.size
        val totalGastado = lista.sumOf {
            it.precio?.replace("€", "")?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        }
        assertEquals(3, totalRegistros)
        assertEquals(60.0, totalGastado, 0.001)
    }

    @Test
    fun `resumen con precios en diferentes formatos`() {
        val lista = listOf(
            mant("A", precio = "45€"),
            mant("B", precio = "120,50€"),
            mant("C", precio = null),
        )
        val total = lista.sumOf {
            it.precio?.replace("€", "")?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        }
        assertEquals(165.5, total, 0.001)
    }
}
