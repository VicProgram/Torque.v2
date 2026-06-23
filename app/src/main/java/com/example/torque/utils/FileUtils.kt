package com.example.torque.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

object ArchivoUtils {
    fun copiarImagenAInterno(contexto: Context, uri: Uri): String? {
        return try {
            val flujoEntrada = contexto.contentResolver.openInputStream(uri)
            val nombreArchivo = obtenerNombreArchivo(contexto, uri) ?: "img_${System.currentTimeMillis()}.jpg"
            val archivo = File(contexto.filesDir, nombreArchivo)
            flujoEntrada?.use { entrada ->
                archivo.outputStream().use { salida ->
                    entrada.copyTo(salida)
                }
            }
            archivo.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun obtenerNombreArchivo(contexto: Context, uri: Uri): String? {
        var nombre: String? = null
        contexto.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val indiceNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (indiceNombre != -1 && cursor.moveToFirst()) {
                nombre = cursor.getString(indiceNombre)
            }
        }
        return nombre
    }
}