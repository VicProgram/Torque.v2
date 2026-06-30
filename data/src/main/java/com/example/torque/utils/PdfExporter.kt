package com.example.torque.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.room.entity.MotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import com.example.torque.data.R

object PdfExporter {

    suspend fun exportarHistorial(
        contexto: Context,
        uri: Uri,
        moto: MotoEntity,
        mantenimientos: List<MantenimientoEntity>,
        resultadoSalud: ResultadoSalud?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val documento = PdfDocument()
            val pinturaTitulo = Paint().apply {
                color = Color.rgb(0x3B, 0x82, 0xF6)
                textSize = 24f
                isFakeBoldText = true
            }
            val pinturaSubtitulo = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
            }
            val pinturaTexto = Paint().apply {
                color = Color.BLACK
                textSize = 12f
            }
            val pinturaEncabezado = Paint().apply {
                color = Color.WHITE
                textSize = 12f
                isFakeBoldText = true
            }
            val pinturaFondoEncabezado = Paint().apply {
                color = Color.rgb(0x3B, 0x82, 0xF6)
            }
            val pinturaLinea = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 0.5f
            }
            val pinturaSalud = Paint().apply {
                color = Color.rgb(0x4C, 0xAF, 0x50)
                textSize = 18f
                isFakeBoldText = true
            }

            var numeroPagina = 1
            val paginaInfo = PdfDocument.PageInfo.Builder(595, 842, numeroPagina).create()
            var paginaActual = documento.startPage(paginaInfo)
            var canvasActual: Canvas = paginaActual.canvas

            var y = 40f
            val margen = 40f
            val anchoPagina = 515f

            canvasActual.drawText(contexto.getString(R.string.pdf_title), margen, y, pinturaTitulo)
            y += 30f
            canvasActual.drawText(contexto.getString(R.string.pdf_subtitle, "${moto.marca} ${moto.modelo}", moto.kilometros), margen, y, pinturaSubtitulo)
            y += 20f
            canvasActual.drawText(contexto.getString(R.string.pdf_plate, moto.matricula ?: ""), margen, y, pinturaSubtitulo)
            y += 25f

            if (resultadoSalud != null) {
                canvasActual.drawText(contexto.getString(R.string.pdf_health, resultadoSalud.saludGlobal), margen, y, pinturaSalud)
                y += 30f
            }

            canvasActual.drawLine(margen, y, margen + anchoPagina, y, pinturaLinea)
            y += 15f

            val columnas = listOf(
                Columna(contexto.getString(R.string.pdf_col_date), 70f),
                Columna(contexto.getString(R.string.pdf_col_type), 120f),
                Columna(contexto.getString(R.string.pdf_col_kms), 60f),
                Columna(contexto.getString(R.string.pdf_col_price), 70f),
                Columna(contexto.getString(R.string.pdf_col_next_service), 80f),
                Columna(contexto.getString(R.string.pdf_col_notes), 115f)
            )

            canvasActual.drawRect(margen, y - 5f, margen + anchoPagina, y + 18f, pinturaFondoEncabezado)
            var x = margen
            for (col in columnas) {
                canvasActual.drawText(col.nombre, x + 4f, y + 12f, pinturaEncabezado)
                x += col.ancho
            }
            y += 28f

            for (m in mantenimientos) {
                if (y > 800f) {
                    documento.finishPage(paginaActual)
                    numeroPagina++
                    val nuevaPaginaInfo = PdfDocument.PageInfo.Builder(595, 842, numeroPagina).create()
                    paginaActual = documento.startPage(nuevaPaginaInfo)
                    canvasActual = paginaActual.canvas
                    y = 40f
                }

                val proxServicio = if (m.intervaloKms > 0) "${m.kms + m.intervaloKms}" else contexto.getString(R.string.pdf_dash)
                val fila = listOf(
                    m.fecha.take(10),
                    m.nombreMantenimiento,
                    "${m.kms}",
                    m.precio ?: contexto.getString(R.string.pdf_dash),
                    proxServicio,
                    m.notas ?: ""
                )
                x = margen
                for (i in fila.indices) {
                    val texto = fila[i]
                    val anchoCol = columnas[i].ancho
                    val textoRecortado = if (pinturaTexto.measureText(texto) > anchoCol - 4f)
                        texto.take(15) + "…"
                    else texto
                    canvasActual.drawText(textoRecortado, x + 4f, y + 10f, pinturaTexto)
                    x += anchoCol
                }
                y += 20f
            }

            documento.finishPage(paginaActual)

            val cacheDir = File(contexto.cacheDir, "pdf_temp")
            cacheDir.mkdirs()
            val pdfTemp = File(cacheDir, "historial_${System.currentTimeMillis()}.pdf")
            FileOutputStream(pdfTemp).use { documento.writeTo(it) }
            documento.close()

            contexto.contentResolver.openOutputStream(uri)?.use { salida ->
                pdfTemp.inputStream().use { it.copyTo(salida) }
            }

            pdfTemp.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class Columna(val nombre: String, val ancho: Float)
}
