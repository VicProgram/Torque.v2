package com.example.torque.shared.algorithm

import com.example.torque.shared.model.DetalleSalud
import com.example.torque.shared.model.MantenimientoData
import com.example.torque.shared.model.ResultadoSalud
import com.example.torque.shared.model.TipoMantenimiento
import com.example.torque.shared.model.TiposMantenimiento
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SaludMotoCalculator {

    private val FORMATO_DD_MM_YYYY = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val FORMATO_YYYY_MM_DD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun calcular(
        mantenimientos: List<MantenimientoData>,
        kmActuales: Int
    ): ResultadoSalud {
        val agrupadosPorId = mantenimientos
            .filter { it.tipoMantenimientoId > 0L }
            .groupBy { it.tipoMantenimientoId }

        val sinId = mantenimientos.filter { it.tipoMantenimientoId == 0L }

        fun mejorMatch(tipo: TipoMantenimiento): MantenimientoData? {
            val porId = agrupadosPorId[tipo.id]?.maxByOrNull { it.kms }
            if (porId != null) return porId
            return sinId.filter { coincide(it, tipo) }.maxByOrNull { it.kms }
        }

        val detalles = TiposMantenimiento.TODOS.mapNotNull { tipo ->
            val ultimo = mejorMatch(tipo)

            val cumplimiento = if (ultimo != null) {
                if (tipo.esDesgaste) {
                    ultimo.porcentajeVida
                } else {
                    val cumplimientoKm = if (tipo.intervaloKms > 0) {
                        val avanzado = kmActuales - ultimo.kms
                        val porcentaje = avanzado.toFloat() / tipo.intervaloKms
                        1f - porcentaje
                    } else 1f

                    val cumplimientoDias = if (tipo.intervaloDias > 0) {
                        val fechaUltimo = parseFecha(ultimo.fecha) ?: return@mapNotNull null
                        val diasDesdeUltimo = ((Date().time - fechaUltimo.time) / (1000 * 60 * 60 * 24)).toInt()
                        val porcentaje = diasDesdeUltimo.toFloat() / tipo.intervaloDias
                        1f - porcentaje
                    } else 1f

                    val cumplimientoFinal = minOf(cumplimientoKm, cumplimientoDias)
                    (cumplimientoFinal.coerceIn(0f, 1f) * 100).toInt()
                }
            } else 100

            val relevancia = (tipo.peso * (100 - cumplimiento)) / 100

            DetalleSalud(
                tipo = tipo,
                ultimoKms = ultimo?.kms,
                cumplimiento = cumplimiento,
                relevancia = relevancia
            )
        }

        val pesoTotal = detalles.sumOf { it.tipo.peso }
        val saludGlobal = if (pesoTotal > 0) {
            (detalles.sumOf { it.tipo.peso * it.cumplimiento } / pesoTotal)
        } else 0

        val proximos = detalles
            .filter { it.cumplimiento < 100 }
            .sortedByDescending { it.relevancia }
            .take(5)

        return ResultadoSalud(
            saludGlobal = saludGlobal,
            desglose = detalles,
            proximosVencidos = proximos
        )
    }

    private fun coincide(mantenimiento: MantenimientoData, tipo: TipoMantenimiento): Boolean {
        if (mantenimiento.tipoMantenimientoId == tipo.id) return true
        val nombre = mantenimiento.nombreMantenimiento
        if (nombre.equals(tipo.nombre, ignoreCase = true)) return true
        val tareas = mantenimiento.tareas
        return tareas.split(",").any { it.trim().equals(tipo.nombre, ignoreCase = true) }
    }

    private fun parseFecha(fecha: String): Date? {
        return try {
            FORMATO_DD_MM_YYYY.parse(fecha)
        } catch (_: Exception) {
            try {
                FORMATO_YYYY_MM_DD.parse(fecha)
            } catch (_: Exception) {
                null
            }
        }
    }
}
