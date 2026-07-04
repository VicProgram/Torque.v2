package com.example.torque.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ArchivoUtils {
    fun copiarImagenAInterno(contexto: Context, uri: Uri, comprimir: Boolean = true): String? {
        return try {
            val nombreArchivo = obtenerNombreArchivo(contexto, uri) ?: "img_${System.currentTimeMillis()}.jpg"
            val archivo = File(contexto.filesDir, nombreArchivo)
            if (comprimir) comprimirImagen(contexto, uri, archivo) else copiarSinComprimir(contexto, uri, archivo)
            archivo.absolutePath
        } catch (e: Exception) {
            Log.e("ArchivoUtils", "Error al copiar imagen", e)
            null
        }
    }

    private fun comprimirImagen(contexto: Context, uri: Uri, archivo: File) {
        contexto.contentResolver.openInputStream(uri)?.use { entrada ->
            val bitmap = BitmapFactory.decodeStream(entrada)
            if (bitmap != null) {
                FileOutputStream(archivo).use { salida ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, salida)
                }
                bitmap.recycle()
            } else {
                copiarDesdeStream(contexto, uri, archivo)
            }
        }
    }

    private fun copiarSinComprimir(contexto: Context, uri: Uri, archivo: File) {
        contexto.contentResolver.openInputStream(uri)?.use { entrada ->
            archivo.outputStream().use { salida -> entrada.copyTo(salida) }
        }
    }

    private fun copiarDesdeStream(contexto: Context, uri: Uri, archivo: File) {
        contexto.contentResolver.openInputStream(uri)?.use { rein ->
            archivo.outputStream().use { salida -> rein.copyTo(salida) }
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
