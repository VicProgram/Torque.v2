package com.example.torque.shared.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {
    @Test
    fun `formatos son correctos`() {
        assertEquals("dd/MM/yyyy", DateUtils.FORMATO_FECHA_VISUAL)
        assertEquals("yyyy-MM-dd HH:mm:ss", DateUtils.FORMATO_FECHA_COMPLETA)
        assertTrue(DateUtils.FORMATO_FECHA_VISUAL.isNotBlank())
        assertTrue(DateUtils.FORMATO_FECHA_COMPLETA.isNotBlank())
    }
}
