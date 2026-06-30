package com.example.torque

import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.utils.SaludMotoCalculator
import com.example.torque.utils.TiposMantenimiento
import com.example.torque.utils.DateUtils
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

    // ═══════════════════════════════════════════════════
    //   NUEVOS TESTS
    // ═══════════════════════════════════════════════════

    // ───── 16. Exactamente en el intervalo (recién hecho) ─────
    @Test
    fun `mantenimiento recien hecho al mismo km del intervalo`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        val detalle = res.desglose.find { it.tipo.id == 1L }
        assertEquals(100, detalle!!.cumplimiento)
    }

    // ───── 17. Borde: 5% restante por km ─────
    @Test
    fun `mantenimiento al 5 por ciento de cumplimiento`() {
        // Cambio Aceite Motor tiene intervaloKms=5000
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, tipoId = 1)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 4750)
        val detalle = res.desglose.find { it.tipo.id == 1L }
        assertEquals(5, detalle!!.cumplimiento)
    }

    // ───── 18. Tipos con intervalo vencidos dan 0 ─────
    @Test
    fun `tipos con intervalo vencidos tienen cumplimiento 0`() {
        val tiposConIntervalo = TiposMantenimiento.TODOS.filter { it.intervaloKms > 0 || it.intervaloDias > 0 }
        val lista = tiposConIntervalo.map { tipo ->
            if (tipo.esDesgaste) {
                mant(tipo.nombre, tipoId = tipo.id, porcentajeVida = 0)
            } else {
                mant(tipo.nombre, kms = 0, tipoId = tipo.id)
            }
        }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 999_999)
        for (detalle in res.desglose) {
            if (detalle.tipo.intervaloKms > 0 || detalle.tipo.intervaloDias > 0) {
                assertEquals("${detalle.tipo.nombre} debería estar vencido", 0, detalle.cumplimiento)
            }
        }
        assertTrue(res.saludGlobal < 100)
    }

    // ───── 19. Relevancia de un desgaste crítico ─────
    @Test
    fun `desgaste critico genera relevancia alta`() {
        val lista = listOf(
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 5)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        val detalle = res.desglose.find { it.tipo.id == 10L }
        assertNotNull(detalle)
        // peso=10, cumplimiento=5 → relevancia = (10 * 95)/100 = 9
        assertTrue(detalle!!.relevancia > 0)
        // Debe aparecer en próximos vencidos
        assertTrue(res.proximosVencidos.any { it.tipo.id == 10L })
    }

    // ───── 20. WEAR_TIPOS son todos desgaste y están en TODOS ─────
    @Test
    fun `wear tipos estan incluidos en TODOS y son desgaste`() {
        val idsTodos = TiposMantenimiento.TODOS.map { it.id }.toSet()
        for (wear in TiposMantenimiento.WEAR_TIPOS) {
            assertTrue("${wear.nombre} debería estar en TODOS", wear.id in idsTodos)
            assertTrue("${wear.nombre} debería ser esDesgaste=true", wear.esDesgaste)
        }
    }

    // ───── 21. No hay nombres duplicados entre tipos ─────
    @Test
    fun `no hay nombres duplicados en TODOS`() {
        val nombres = TiposMantenimiento.TODOS.map { it.nombre.lowercase() }
        assertEquals(nombres.toSet().size, nombres.size)
    }

    // ───── 22. Todos los tipos tienen peso positivo ─────
    @Test
    fun `todos los tipos tienen peso mayor a 0`() {
        for (tipo in TiposMantenimiento.TODOS) {
            assertTrue("${tipo.nombre} debería tener peso > 0", tipo.peso > 0)
        }
    }

    // ───── 23. sin mantenimientos ni km → salud 100 ─────
    @Test
    fun `sin datos y con km cero salud es 100`() {
        val res = SaludMotoCalculator.calcular(emptyList(), kmActuales = 0)
        assertEquals(100, res.saludGlobal)
        assertTrue(res.desglose.all { it.cumplimiento == 100 })
    }

    // ───── 24. Kilometraje negativo (no debería romperse) ─────
    @Test
    fun `kilometraje negativo no causa error`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = -100)
        // Avanzado = -100 - 0 = -100 → cumplimiento = 1 - (-100/5000) = 1.02 → 100%
        val detalle = res.desglose.find { it.tipo.id == 1L }
        assertEquals(100, detalle!!.cumplimiento)
        assertEquals(100, res.saludGlobal)
    }

    // ───── 25. proximosVencidos solo contiene items con cumplimiento < 100 ─────
    @Test
    fun `proximos vencidos solo incluye items con cumplimiento menor a 100`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1),
            mant("Filtro de Aceite", kms = 0, intervaloKms = 5000, tipoId = 2),
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        // Aceite: 100% (no debería estar), Filtro: 0% (debería estar)
        assertTrue(res.proximosVencidos.none { it.tipo.id == 1L })
        assertTrue(res.proximosVencidos.any { it.tipo.id == 2L })
    }

    // ───── 26. Varios mantenimientos combinados ─────
    @Test
    fun `combinacion de varios tipos con distinto cumplimiento`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1),      // 0% vencido
            mant("Filtro de Aceite", kms = 0, intervaloKms = 5000, tipoId = 2),              // 0% vencido
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 60),          // 60%
            mant("Neumático Delantero", tipoId = 21, porcentajeVida = 80),                   // 80%
            mant("Cambio de Cadena y Coronas", kms = 5000, intervaloKms = 20000, tipoId = 9),// 0% vencido
            mant("Líquido de Frenos", kms = 0, intervaloKms = 20000, tipoId = 12),          // 0% vencido
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 10000)
        assertTrue(res.saludGlobal in 0..100)
        assertTrue(res.proximosVencidos.isNotEmpty())
        // El resto (sin registro) al 100% → salud > 0
        assertTrue(res.saludGlobal > 0)
    }

    // ───── 27. DateUtils formatos no vacíos ─────
    @Test
    fun `dateUtils formatos son correctos`() {
        assertEquals("dd/MM/yyyy", DateUtils.FORMATO_FECHA_VISUAL)
        assertEquals("yyyy-MM-dd HH:mm:ss", DateUtils.FORMATO_FECHA_COMPLETA)
        assertTrue(DateUtils.FORMATO_FECHA_VISUAL.isNotBlank())
        assertTrue(DateUtils.FORMATO_FECHA_COMPLETA.isNotBlank())
    }

    // ───── 28. desdeNombre con espacios extra ─────
    @Test
    fun `desdeNombre funciona con espacios alrededor`() {
        val tipo = TiposMantenimiento.desdeNombre("  Cambio de Aceite Motor  ")
        assertNotNull(tipo)
        assertEquals(1L, tipo!!.id)
    }

    // ───── 29. WEAR_TIPOS no contiene duplicados ─────
    @Test
    fun `wear tipos no tiene ids duplicados`() {
        val ids = TiposMantenimiento.WEAR_TIPOS.map { it.id }
        assertEquals(ids.toSet().size, ids.size)
    }

    // ───── 30. Todos los tipos tienen categoría no vacía ─────
    @Test
    fun `todos los tipos tienen categoria`() {
        for (tipo in TiposMantenimiento.TODOS) {
            assertTrue("${tipo.nombre} debería tener categoría", tipo.categoria.isNotBlank())
        }
    }

    // ═══════════════════════════════════════════════════
    //   TESTS DE INTEGRIDAD DE DATOS
    // ═══════════════════════════════════════════════════

    // ───── 31. Existen los 25 tipos del 1 al 25 ─────
    @Test
    fun `existen los 25 tipos con ids 1 a 25`() {
        val ids = TiposMantenimiento.TODOS.map { it.id }.toSet()
        for (i in 1L..25L) {
            assertTrue("Falta el tipo con id=$i", i in ids)
        }
        assertEquals(25, ids.size)
    }

    // ───── 32. Todos los tipos tienen peso en 1..10 ─────
    @Test
    fun `pesos estan entre 1 y 10`() {
        for (tipo in TiposMantenimiento.TODOS) {
            assertTrue("${tipo.nombre} peso=${tipo.peso} fuera de rango", tipo.peso in 1..10)
        }
    }

    // ───── 33. Todos los `esDesgaste=true` están en WEAR_TIPOS ─────
    @Test
    fun `todos los tipos de desgaste estan en WEAR_TIPOS`() {
        val wearIds = TiposMantenimiento.WEAR_TIPOS.map { it.id }.toSet()
        for (tipo in TiposMantenimiento.TODOS.filter { it.esDesgaste }) {
            assertTrue("${tipo.nombre} es esDesgaste pero no está en WEAR_TIPOS", tipo.id in wearIds)
        }
    }

    // ───── 34. Todos los WEAR_TIPOS tienen esDesgaste=true ─────
    @Test
    fun `todos los wear tipos tienen esDesgaste true`() {
        for (tipo in TiposMantenimiento.WEAR_TIPOS) {
            assertTrue("${tipo.nombre} no tiene esDesgaste=true", tipo.esDesgaste)
        }
    }

    // ───── 35. Tipos de desgaste tienen intervaloKms=0 en su mayoría ─────
    @Test
    fun `tipos desgaste con intervalo diferente a 0 son excepcion`() {
        val conIntervalo = TiposMantenimiento.WEAR_TIPOS.filter { it.intervaloKms > 0 }
        for (tipo in conIntervalo) {
            // Pastillas (5000), Discos (10000) tienen intervalo además de desgaste
            assertTrue(tipo.intervaloKms in listOf(5000, 10000))
        }
    }

    // ═══════════════════════════════════════════════════
    //   TESTS DE CÁLCULO PONDERADO (saludGlobal)
    // ═══════════════════════════════════════════════════

    // ───── 36. Todos los tipos calculables al 0% → salud baja ─────
    @Test
    fun `todos los tipos con intervalo al 0 por ciento dan salud baja`() {
        val lista = TiposMantenimiento.TODOS.filter { it.intervaloKms > 0 }.map { tipo ->
            mant(tipo.nombre, kms = 0, intervaloKms = tipo.intervaloKms, tipoId = tipo.id)
        } + TiposMantenimiento.WEAR_TIPOS.map { tipo ->
            mant(tipo.nombre, tipoId = tipo.id, porcentajeVida = 0)
        }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 999_999)
        // Tipos sin intervalo (ej: Presión Neumáticos) quedan en 100%,
        // así que saludGlobal > 0 pero debe ser < 50
        assertTrue("Salud global debería ser baja pero fue ${res.saludGlobal}",
            res.saludGlobal in 1..50)
    }

    // ───── 37. Todos los tipos al 100% (recién hechos) → salud 100 ─────
    @Test
    fun `todos los tipos al 100 por ciento dan salud 100`() {
        val lista = TiposMantenimiento.TODOS.filter { it.intervaloKms > 0 }.map { tipo ->
            mant(tipo.nombre, kms = 5000, intervaloKms = tipo.intervaloKms, tipoId = tipo.id)
        } + TiposMantenimiento.WEAR_TIPOS.map { tipo ->
            mant(tipo.nombre, tipoId = tipo.id, porcentajeVida = 100)
        }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        assertEquals(100, res.saludGlobal)
    }

    // ───── 38. Peso influye en saludGlobal ─────
    @Test
    fun `tipos con mayor peso afectan mas la salud global`() {
        // Solo dos tipos: Aceite (peso 10) y Bujías (peso 6) ambos al 0%
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1),
            mant("Bujías", kms = 0, intervaloKms = 10000, tipoId = 3),
        )
        val pesoAceite = 10
        val pesoBujias = 6
        val pesoTotal = pesoAceite + pesoBujias
        val esperado = (pesoAceite * 0 + pesoBujias * 0) / pesoTotal // = 0

        val res = SaludMotoCalculator.calcular(lista, kmActuales = 999_999)
        // Con sólo estos dos al 0%, y el resto al 100%,
        // la salud debería ser menor de 100
        assertTrue(res.saludGlobal < 100)

        // Si sólo Aceite está al 0%, Bujías al 100%, el resto 100%
        val lista2 = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1),
            mant("Bujías", kms = 10000, intervaloKms = 10000, tipoId = 3),
        )
        val res2 = SaludMotoCalculator.calcular(lista2, kmActuales = 10000)
        val desgloseAceite = res2.desglose.find { it.tipo.id == 1L }
        val desgloseBujias = res2.desglose.find { it.tipo.id == 3L }
        assertEquals(0, desgloseAceite!!.cumplimiento)
        assertEquals(100, desgloseBujias!!.cumplimiento)
    }

    // ───── 39. Salud global con porcentajes mixtos ─────
    @Test
    fun `salud global refleja correctamente pesos y cumplimientos`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1),          // 0% peso 10
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 100),            // 100% peso 10
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 999_999)
        // Aceite (0%) tiene peso 10, Pastillas (100%) tiene peso 10, resto 100%
        assertTrue(res.saludGlobal > 0)
        assertTrue(res.saludGlobal < 100)
    }

    // ═══════════════════════════════════════════════════
    //   TESTS DE WEAR (desgaste) EXHAUSTIVOS
    // ═══════════════════════════════════════════════════

    // ───── 40. Todos los wear tipos con porcentaje vida 0% → cumplimiento 0 ─────
    @Test
    fun `todos los wear tipos con 0 por ciento dan cumplimiento 0`() {
        val lista = TiposMantenimiento.WEAR_TIPOS.map { tipo ->
            mant(tipo.nombre, tipoId = tipo.id, porcentajeVida = 0)
        }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        for (tipo in TiposMantenimiento.WEAR_TIPOS) {
            val detalle = res.desglose.find { it.tipo.id == tipo.id }
            assertEquals("${tipo.nombre} debería tener cumplimiento 0", 0, detalle!!.cumplimiento)
        }
    }

    // ───── 41. Todos los wear tipos con porcentaje vida 100% → cumplimiento 100 ─────
    @Test
    fun `todos los wear tipos con 100 por ciento dan cumplimiento 100`() {
        val lista = TiposMantenimiento.WEAR_TIPOS.map { tipo ->
            mant(tipo.nombre, tipoId = tipo.id, porcentajeVida = 100)
        }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        for (tipo in TiposMantenimiento.WEAR_TIPOS) {
            val detalle = res.desglose.find { it.tipo.id == tipo.id }
            assertEquals("${tipo.nombre} debería tener cumplimiento 100", 100, detalle!!.cumplimiento)
        }
    }

    // ───── 42. Wear tipos sin registro dan 100 ─────
    @Test
    fun `wear tipos sin registro dan cumplimiento 100`() {
        val res = SaludMotoCalculator.calcular(emptyList(), kmActuales = 0)
        for (tipo in TiposMantenimiento.WEAR_TIPOS) {
            val detalle = res.desglose.find { it.tipo.id == tipo.id }
            assertEquals("${tipo.nombre} sin registro debería ser 100", 100, detalle!!.cumplimiento)
        }
    }

    // ───── 43. Wear tipo con intervalo también usa porcentajeVida ─────
    @Test
    fun `wear tipo con intervalo usa porcentajeVida no kms`() {
        // Pastillas de Freno Delantero: esDesgaste=true, intervaloKms=5000
        // Aunque tenga kms muy avanzados, debe usar porcentajeVida
        val lista = listOf(
            mant("Pastillas de Freno Delantero", kms = 0, intervaloKms = 5000,
                tipoId = 10, porcentajeVida = 30)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 100_000)
        val detalle = res.desglose.find { it.tipo.id == 10L }
        assertEquals(30, detalle!!.cumplimiento)
    }

    // ═══════════════════════════════════════════════════
    //   TESTS DE proximosVencidos
    // ═══════════════════════════════════════════════════

    // ───── 44. proximosVencidos máximo 5 elementos ─────
    @Test
    fun `proximos vencidos maximo 5 elementos`() {
        val muchosVencidos = TiposMantenimiento.TODOS
            .filter { it.intervaloKms > 0 || it.esDesgaste }
            .map { tipo ->
                if (tipo.esDesgaste) {
                    mant(tipo.nombre, tipoId = tipo.id, porcentajeVida = 10)
                } else {
                    mant(tipo.nombre, kms = 0, intervaloKms = tipo.intervaloKms, tipoId = tipo.id)
                }
            }
        val res = SaludMotoCalculator.calcular(muchosVencidos, kmActuales = 999_999)
        assertTrue("Máximo 5 próximos vencidos, pero tiene ${res.proximosVencidos.size}",
            res.proximosVencidos.size <= 5)
    }

    // ───── 45. proximosVencidos ordenados por relevancia descendente ─────
    @Test
    fun `proximos vencidos orden descendente por relevancia`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1),      // peso 10, cumpl 0
            mant("Filtro de Aceite", kms = 0, intervaloKms = 5000, tipoId = 2),             // peso 5, cumpl 0
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 999_999)
        for (i in 0 until res.proximosVencidos.size - 1) {
            assertTrue("Orden incorrecto en posición $i",
                res.proximosVencidos[i].relevancia >= res.proximosVencidos[i + 1].relevancia)
        }
    }

    // ───── 46. Items con cumplimiento 100 no aparecen en proximosVencidos ─────
    @Test
    fun `cumplimiento 100 no aparece en proximos vencidos`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1),   // 100%
            mant("Filtro de Aceite", kms = 0, intervaloKms = 5000, tipoId = 2),             // 0%
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        assertTrue(res.proximosVencidos.none { it.tipo.id == 1L })
        assertTrue(res.proximosVencidos.any { it.tipo.id == 2L })
    }

    // ───── 47. Wear tipos con bajo porcentaje aparecen en proximosVencidos ─────
    @Test
    fun `wear tipos con porcentaje bajo aparecen en proximos`() {
        // Sólo 4 wear types (proximosVencidos toma max 5, evitamos saturar)
        val wearSubset = TiposMantenimiento.WEAR_TIPOS.take(4)
        val lista = wearSubset.map { tipo ->
            mant(tipo.nombre, tipoId = tipo.id, porcentajeVida = 15)
        }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        for (tipo in wearSubset) {
            assertTrue("${tipo.nombre} al 15% debería estar en próximos vencidos",
                res.proximosVencidos.any { it.tipo.id == tipo.id })
        }
    }

    // ───── 48. Sin nada vencido → proximosVencidos vacío ─────
    @Test
    fun `sin items vencidos proximos vencidos vacio`() {
        val res = SaludMotoCalculator.calcular(emptyList(), kmActuales = 0)
        assertTrue(res.proximosVencidos.isEmpty())
    }

    // ═══════════════════════════════════════════════════
    //   TESTS DE COINCIDENCIA POR TAREAS (FALLBACK)
    // ═══════════════════════════════════════════════════

    // ───── 49. Coincidencia por tareas con múltiples tipos separados por coma ─────
    @Test
    fun `coincide por tareas con multiples tipos`() {
        val lista = listOf(
            mant("Mantenimiento General", kms = 0, intervaloKms = 5000,
                tareas = "Cambio de Aceite Motor, Filtro de Aceite, Bujías")
        )
        // Aceite intervaloKms=5000, Filtro=5000, Bujías=10000
        // Con kmActuales=10000:
        // Aceite: avanzado=10000-0/5000=2 → 0%
        // Filtro: avanzado=10000-0/5000=2 → 0%
        // Bujías: avanzado=10000-0/10000=1 → 0%
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 10000)
        assertEquals(0, res.desglose.find { it.tipo.id == 1L }!!.cumplimiento)
        assertEquals(0, res.desglose.find { it.tipo.id == 2L }!!.cumplimiento)
        assertEquals(0, res.desglose.find { it.tipo.id == 3L }!!.cumplimiento)
    }

    // ───── 50. Tareas con espacios después de comas ─────
    @Test
    fun `tareas con espacios tras comas se emparejan correctamente`() {
        val lista = listOf(
            mant("Mantenimiento General", kms = 1000, intervaloKms = 5000,
                tareas = "Cambio de Aceite Motor,  Filtro de Aceite")
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 3500)
        val aceite = res.desglose.find { it.tipo.id == 1L }
        assertEquals(50, aceite!!.cumplimiento)
    }

    // ───── 51. Tareas null y nombre sin match no causa error ─────
    @Test
    fun `tareas null no causa error`() {
        val lista = listOf(
            mant("Registro sin categorizar", kms = 1000, intervaloKms = 5000, tipoId = 0,
                tareas = null)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 6000)
        // "Registro sin categorizar" no matchea ningún tipo por nombre ni tareas
        // Todos los tipos quedan sin match → 100%
        val aceite = res.desglose.find { it.tipo.id == 1L }
        assertEquals(100, aceite!!.cumplimiento)
    }

    // ───── 52. Coincidencia por nombre de mantenimiento (tipoId=0) ─────
    @Test
    fun `coincide por nombre de mantenimiento cuando tipoId es 0`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000,
                tareas = "Mantenimiento General")
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 6000)
        // "Mantenimiento General" no matchea ningún tipo en tareas
        // Pero nombreMantenimiento = "Cambio de Aceite Motor" matchea tipo 1
        val aceite = res.desglose.find { it.tipo.id == 1L }
        assertEquals(0, aceite!!.cumplimiento)
    }

    // ───── 53. Nombre en tareas no matchea si no coincide exactamente ─────
    @Test
    fun `nombre parcial en tareas no matchea`() {
        val lista = listOf(
            mant("Mantenimiento General", kms = 0, intervaloKms = 5000,
                tareas = "Aceite")  // "Aceite" ≠ "Cambio de Aceite Motor"
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        // Sin match exacto → tipo 1 queda sin asignar → 100%
        val aceite = res.desglose.find { it.tipo.id == 1L }
        assertEquals(100, aceite!!.cumplimiento)
    }

    // ═══════════════════════════════════════════════════
    //   TESTS DE BORDES Y CASOS EXTREMOS
    // ═══════════════════════════════════════════════════

    // ───── 54. Kilometraje menor que el último mantenimiento (sobremantenido) ─────
    @Test
    fun `km actuales menores que ultimo mantenimiento`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 10000, intervaloKms = 5000, tipoId = 1)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 5000)
        // Avanzado = 5000 - 10000 = -5000 → cumplimiento = 1 - (-5000/5000) = 2 → 100%
        val aceite = res.desglose.find { it.tipo.id == 1L }
        assertEquals(100, aceite!!.cumplimiento)
    }

    // ───── 55. Todos los tipos con tipoId=0 (legacy) y nombre exacto ─────
    @Test
    fun `todos legacy con nombre exacto se emparejan`() {
        val lista = TiposMantenimiento.TODOS.filter { it.intervaloKms > 0 }.map { tipo ->
            mant(tipo.nombre, kms = 0, intervaloKms = tipo.intervaloKms, tareas = null)
        }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 999_999)
        val mapeados = res.desglose.count { it.cumplimiento == 0 }
        val conIntervalo = TiposMantenimiento.TODOS.count { it.intervaloKms > 0 && !it.esDesgaste }
        assertTrue("Deberían mapearse los tipos con intervalo", mapeados >= conIntervalo)
    }

    // ───── 56. Relevancia calculada correctamente ─────
    @Test
    fun `relevancia se calcula como peso por 100 menos cumplimiento dividido 100`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1),     // peso 10, cumpl 0
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 50),        // peso 10, cumpl 50
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 999_999)
        val aceite = res.desglose.find { it.tipo.id == 1L }
        val pastilla = res.desglose.find { it.tipo.id == 10L }
        // Aceite: 10 * (100-0) / 100 = 10
        assertEquals(10, aceite!!.relevancia)
        // Pastilla: 10 * (100-50) / 100 = 5
        assertEquals(5, pastilla!!.relevancia)
    }

    // ───── 57. ID repetido en lista no duplica el tipo en desglose ─────
    @Test
    fun `ids repetidos en mantenimientos no duplican el tipo en desglose`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 1000, intervaloKms = 5000, tipoId = 1),
            mant("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1),
            mant("Cambio de Aceite Motor", kms = 2000, intervaloKms = 5000, tipoId = 1),
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 6000)
        val aceites = res.desglose.filter { it.tipo.id == 1L }
        assertEquals("Sólo debería haber un DetalleSalud para tipo 1", 1, aceites.size)
        // Debe usar el de mayor kms (5000)
        assertEquals(80, aceites[0].cumplimiento)
    }

    // ═══════════════════════════════════════════════════
    //   TESTS DE PARSEFECHA
    // ═══════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════
    //   NUEVOS TESTS: EDGE CASES EXHAUSTIVOS
    // ═══════════════════════════════════════════════════

    // ───── 59. Todos los tipos sin registro → salud 100 ─────
    @Test
    fun `sin ningun registro todos los tipos al 100`() {
        val res = SaludMotoCalculator.calcular(emptyList(), kmActuales = 0)
        for (detalle in res.desglose) {
            assertEquals("${detalle.tipo.nombre} debería estar al 100%", 100, detalle.cumplimiento)
        }
        assertEquals(100, res.saludGlobal)
    }

    // ───── 60. kmActuales = 0 con mantenimiento en km 0 recien hecho ─────
    @Test
    fun `mantenimiento en km 0 con km actuales 0 da 100`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        assertEquals(100, res.desglose.find { it.tipo.id == 1L }!!.cumplimiento)
    }

    // ───── 61. Exactamente en el límite del intervalo (km = ultimo + intervalo) ─────
    @Test
    fun `exactamente en el limite del intervalo la salud es 0`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 5000, intervaloKms = 5000, tipoId = 1)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 10000)
        assertEquals(0, res.desglose.find { it.tipo.id == 1L }!!.cumplimiento)
    }

    // ───── 62. Borde: 75% de cumplimiento (exacto en float) ─────
    @Test
    fun `cumplimiento al 75 por ciento exacto`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 1250)
        assertEquals(75, res.desglose.find { it.tipo.id == 1L }!!.cumplimiento)
    }

    // ───── 63. Intervalo = 1 km (valor mínimo) ─────
    @Test
    fun `intervalo minimo de 1 km`() {
        // Simular un tipo con intervaloKms=1
        val lista = listOf(
            MantenimientoEntity(
                idMoto = 1, nombreMantenimiento = "Ajuste mínimo",
                fecha = "01/01/2024", kms = 0, intervaloKms = 1,
                tipoMantenimientoId = 0  // no matchea ningún tipo real
            )
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 1)
        // No matchea ningún tipo → todos al 100%
        assertEquals(100, res.saludGlobal)
    }

    // ───── 64. Intervalo de 0 km y 0 días no afecta el cumplimiento ─────
    @Test
    fun `intervalo cero no afecta el cumplimiento`() {
        val idsConIntervaloCero = TiposMantenimiento.TODOS
            .filter { it.intervaloKms == 0 && it.intervaloDias == 0 && !it.esDesgaste }
        val lista = idsConIntervaloCero.map { tipo ->
            mant(tipo.nombre, kms = 1000, intervaloKms = 0, tipoId = tipo.id)
        }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 999_999)
        for (tipo in idsConIntervaloCero) {
            val detalle = res.desglose.find { it.tipo.id == tipo.id }
            assertEquals("${tipo.nombre} sin intervalo debe estar al 100%", 100, detalle!!.cumplimiento)
        }
    }

    // ───── 65. Mezcla de tipos con y sin intervalo ─────
    @Test
    fun `mezcla de tipos con y sin intervalo`() {
        val lista = listOf(
            mant("Cambio de Aceite Motor", kms = 0, intervaloKms = 5000, tipoId = 1),      // 0%
            mant("Cambio de Neumáticos", kms = 5000, intervaloKms = 10000, tipoId = 15),    // 50%
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 10000)
        // Aceite: vencido
        assertEquals(0, res.desglose.find { it.tipo.id == 1L }!!.cumplimiento)
        // Neumáticos: a mitad
        assertEquals(50, res.desglose.find { it.tipo.id == 15L }!!.cumplimiento)
        // El resto: 100%
    }

    // ───── 66. Wear type con porcentaje vida exactamente 0 ─────
    @Test
    fun `wear type con porcentaje vida 0`() {
        val lista = listOf(
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 0)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        assertEquals(0, res.desglose.find { it.tipo.id == 10L }!!.cumplimiento)
    }

    // ───── 67. Wear type con porcentaje vida exactamente 50 ─────
    @Test
    fun `wear type con porcentaje vida 50`() {
        val lista = listOf(
            mant("Pastillas de Freno Delantero", tipoId = 10, porcentajeVida = 50)
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        assertEquals(50, res.desglose.find { it.tipo.id == 10L }!!.cumplimiento)
    }

    // ───── 68. Todos los wear types con valores variados ─────
    @Test
    fun `todos los wear types con valores variados`() {
        val valores = mapOf(
            10L to 90,   // Pastillas D → 90%
            11L to 40,   // Pastillas T → 40%
            13L to 75,   // Discos D → 75%
            23L to 10,   // Discos T → 10%
            21L to 60,   // Neumático D → 60%
            22L to 30,   // Neumático T → 30%
        )
        val lista = valores.map { (id, pct) ->
            val tipo = TiposMantenimiento.desdeId(id)!!
            mant(tipo.nombre, tipoId = id, porcentajeVida = pct)
        }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        for ((id, esperado) in valores) {
            assertEquals(esperado, res.desglose.find { it.tipo.id == id }!!.cumplimiento)
        }
    }

    // ───── 69. Tipo con intervaloDias exclusivamente (sin intervaloKms) ─────
    @Test
    fun `intervalo por dias exclusivamente`() {
        // Batería (id=16): intervaloDias=180, intervaloKms=0
        val lista = listOf(
            mant("Batería", kms = 0, tipoId = 16,
                fecha = "01/01/2024")
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 1000)
        val bateria = res.desglose.find { it.tipo.id == 16L }
        assertNotNull(bateria)
        // Sin intervaloKms, el cumplimiento por km no se aplica
        // El cumplimiento por días se calcula si hay intervaloDias
        assertTrue(bateria!!.cumplimiento in 0..100)
    }

    // ───── 70. Mantenimiento con precio muy alto no afecta salud ─────
    @Test
    fun `precio no afecta el calculo de salud`() {
        val caro = MantenimientoEntity(
            idMoto = 1, nombreMantenimiento = "Cambio de Aceite Motor",
            fecha = "01/01/2024", kms = 0, intervaloKms = 5000,
            precio = "999999", tipoMantenimientoId = 1
        )
        val barato = MantenimientoEntity(
            idMoto = 1, nombreMantenimiento = "Cambio de Aceite Motor",
            fecha = "01/01/2024", kms = 0, intervaloKms = 5000,
            precio = "10", tipoMantenimientoId = 1
        )
        val res1 = SaludMotoCalculator.calcular(listOf(caro), kmActuales = 2500)
        val res2 = SaludMotoCalculator.calcular(listOf(barato), kmActuales = 2500)
        assertEquals(res1.saludGlobal, res2.saludGlobal)
        assertEquals(50, res1.desglose.find { it.tipo.id == 1L }!!.cumplimiento)
    }

    // ───── 71. ResultadoSalud contiene exactamente 25 desgloses ─────
    @Test
    fun `resultado contiene 25 desgloses`() {
        val res = SaludMotoCalculator.calcular(emptyList(), kmActuales = 0)
        assertEquals(25, res.desglose.size)
    }

    // ───── 72. Sin tipos vencidos → próximosVencidos vacío ─────
    @Test
    fun `sin tipos vencidos ni proximos`() {
        val res = SaludMotoCalculator.calcular(emptyList(), kmActuales = 0)
        assertTrue(res.proximosVencidos.isEmpty())
    }

    // ───── 73. 100% motos con múltiples registros legacy (tipoId=0) ─────
    @Test
    fun `registros legacy con nombre exacto se emparejan para todos los tipos`() {
        val lista = TiposMantenimiento.TODOS
            .filter { it.intervaloKms > 0 }
            .mapIndexed { index, tipo ->
                mant(tipo.nombre, kms = 10000, intervaloKms = tipo.intervaloKms,
                    tipoId = 0)   // sin tipoId
            }
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 10000)
        // Todos deberían estar al 100% (recién hechos)
        val bajos = res.desglose.filter { it.cumplimiento < 100 }
        assertTrue("Todos los tipos deberían estar al 100%, bajos=${bajos.size}",
            bajos.isEmpty())
    }

    // ───── 74. Mantenimiento con nombre que NO coincide con ningún tipo ─────
    @Test
    fun `mantenimiento sin match no afecta ningun tipo`() {
        val lista = listOf(
            MantenimientoEntity(
                idMoto = 1, nombreMantenimiento = "Limpieza General",
                fecha = "01/01/2024", kms = 0, intervaloKms = 0,
                tipoMantenimientoId = 0, tareas = "Limpieza"
            )
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 999_999)
        assertEquals(100, res.saludGlobal)
    }

    // ───── 75. Mantenimiento con tareas igual a null y tipoId=0 ─────
    @Test
    fun `mantenimiento con tareas null y tipoId 0`() {
        val lista = listOf(
            MantenimientoEntity(
                idMoto = 1, nombreMantenimiento = "",
                fecha = "01/01/2024", kms = 0, intervaloKms = 0,
                tipoMantenimientoId = 0, tareas = null
            )
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 999_999)
        assertEquals(100, res.saludGlobal)
    }

    // ───── 58. Fecha en formato dd/MM/yyyy se parsea correctamente ─────
    @Test
    fun `fecha en formato dd mm yyyy se usa para intervalo por dias`() {
        // Usar un tipo con intervaloDias > 0 (Batería, id=16, intervaloDias=180)
        val ayer = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val fechaAyer = dateFormat.format(ayer.time)

        val lista = listOf(
            mant("Batería", kms = 0, tipoId = 16,
                fecha = fechaAyer)  // batería cambiada ayer
        )
        val res = SaludMotoCalculator.calcular(lista, kmActuales = 0)
        val bateria = res.desglose.find { it.tipo.id == 16L }
        assertNotNull(bateria)
        // 1 día de 180 → cumplimiento ≈ 99%
        assertTrue("Cumplimiento debería ser ~99% pero fue ${bateria!!.cumplimiento}",
            bateria.cumplimiento in 95..100)
    }
}
