package com.example.torque.utils

import com.example.torque.room.entity.MantenimientoEntity
import java.util.Date

data class DetalleSalud(
    val tipo: TipoMantenimiento,
    val ultimoKms: Int?,
    val cumplimiento: Int,
    val relevancia: Int
)

data class ResultadoSalud(
    val saludGlobal: Int,
    val desglose: List<DetalleSalud>,
    val proximosVencidos: List<DetalleSalud>
)

object SaludMotoCalculator {

    fun calcular(
        mantenimientos: List<MantenimientoEntity>,
        kmActuales: Int
    ): ResultadoSalud {
        val detalles = TiposMantenimiento.TODOS.mapNotNull { tipo ->
            val ultimo = mantenimientos
                .filter { coincide(it, tipo) }
                .maxByOrNull { it.kms }

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

    private fun coincide(mantenimiento: MantenimientoEntity, tipo: TipoMantenimiento): Boolean {
        if (mantenimiento.tipoMantenimientoId == tipo.id) return true
        val nombre = mantenimiento.nombreMantenimiento
        if (nombre.equals(tipo.nombre, ignoreCase = true)) return true
        val tareas = mantenimiento.tareas ?: return false
        return tareas.split(",").any { it.trim().equals(tipo.nombre, ignoreCase = true) }
    }

    private fun parseFecha(fecha: String): Date? {
        return try {
            val formatos = listOf(
                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()),
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            )
            for (formato in formatos) {
                try {
                    return formato.parse(fecha)
                } catch (_: Exception) {}
            }
            null
        } catch (_: Exception) { null }
    }
}
