package com.example.torque.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object ArchivoUtils {
    fun copiarImagenAInterno(contexto: Context, uri: Uri, comprimir: Boolean = true): String? {
        return try {
            val nombreArchivo = obtenerNombreArchivo(contexto, uri) ?: "img_${System.currentTimeMillis()}.jpg"
            val archivo = File(contexto.filesDir, nombreArchivo)

            if (comprimir) {
                val flujoEntrada = contexto.contentResolver.openInputStream(uri)
                flujoEntrada?.use { entrada ->
                    val bitmap = BitmapFactory.decodeStream(entrada)
                    if (bitmap != null) {
                        FileOutputStream(archivo).use { salida ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, salida)
                        }
                        bitmap.recycle()
                    } else {
                        contexto.contentResolver.openInputStream(uri)?.use { rein ->
                            archivo.outputStream().use { salida -> rein.copyTo(salida) }
                        }
                    }
                }
            } else {
                contexto.contentResolver.openInputStream(uri)?.use { entrada ->
                    archivo.outputStream().use { salida -> entrada.copyTo(salida) }
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