package com.example.torque.utils

import android.content.Context
import android.net.Uri
import com.example.torque.room.BaseDeDatosApp
import com.example.torque.room.RepositorioApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    private const val NOMBRE_DB = "torque_room.db"
    private const val CARPETA_FOTOS = "fotos/"
    private const val PREFERENCIAS = "preferencias/"

    private fun vaciarWal(contexto: Context) {
        try {
            val db = BaseDeDatosApp.obtenerInstancia(contexto)
            db.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
        } catch (_: Exception) {
        }
    }

    private fun eliminarArchivosDb(contexto: Context) {
        val dbPath = contexto.getDatabasePath(NOMBRE_DB)
        val dir = dbPath.parentFile
        val nombreBase = dbPath.name
        dir?.listFiles()?.forEach { archivo ->
            if (archivo.name.startsWith(nombreBase)) {
                archivo.delete()
            }
        }
    }

    suspend fun exportar(contexto: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            vaciarWal(contexto)

            val cacheDir = File(contexto.cacheDir, "backup_temp")
            cacheDir.mkdirs()
            val zipTemp = File(cacheDir, "torque_backup_${System.currentTimeMillis()}.zip")

            ZipOutputStream(FileOutputStream(zipTemp)).use { zos ->
                val dbOrigen = contexto.getDatabasePath(NOMBRE_DB)
                if (dbOrigen.exists()) {
                    zos.putNextEntry(ZipEntry(NOMBRE_DB))
                    FileInputStream(dbOrigen).use { it.copyTo(zos) }
                    zos.closeEntry()
                }

                val dirFotos = contexto.filesDir
                dirFotos.listFiles()?.forEach { archivo ->
                    if (archivo.isFile) {
                        zos.putNextEntry(ZipEntry(CARPETA_FOTOS + archivo.name))
                        FileInputStream(archivo).use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }

                val dirDataStore = File(contexto.filesDir.parentFile, "datastore")
                if (dirDataStore.exists()) {
                    dirDataStore.listFiles()?.forEach { archivo ->
                        if (archivo.isFile) {
                            zos.putNextEntry(ZipEntry(PREFERENCIAS + archivo.name))
                            FileInputStream(archivo).use { it.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }
            }

            contexto.contentResolver.openOutputStream(uri)?.use { salida ->
                FileInputStream(zipTemp).use { it.copyTo(salida) }
            }

            zipTemp.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restaurar(contexto: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(contexto.cacheDir, "restore_temp")
            cacheDir.mkdirs()
            cacheDir.listFiles()?.forEach { it.delete() }

            val zipTemp = File(cacheDir, "restore.zip")
            contexto.contentResolver.openInputStream(uri)?.use { entrada ->
                FileOutputStream(zipTemp).use { salida -> entrada.copyTo(salida) }
            }

            val extraerDir = File(cacheDir, "extracted")
            extraerDir.mkdirs()

            ZipInputStream(FileInputStream(zipTemp)).use { zis ->
                var entrada = zis.nextEntry
                while (entrada != null) {
                    val archivoDestino = File(extraerDir, entrada.name)
                    if (entrada.isDirectory) {
                        archivoDestino.mkdirs()
                    } else {
                        archivoDestino.parentFile?.mkdirs()
                        FileOutputStream(archivoDestino).use { zis.copyTo(it) }
                    }
                    zis.closeEntry()
                    entrada = zis.nextEntry
                }
            }

            RepositorioApp.obtenerInstancia(contexto).cerrarBaseDeDatos()

            val dbOrigen = File(extraerDir, NOMBRE_DB)
            if (dbOrigen.exists()) {
                eliminarArchivosDb(contexto)
                val dbDestino = contexto.getDatabasePath(NOMBRE_DB)
                dbOrigen.copyTo(dbDestino, overwrite = true)
            }

            val dirFotosOrigen = File(extraerDir, CARPETA_FOTOS)
            if (dirFotosOrigen.exists()) {
                contexto.filesDir.listFiles()?.forEach { it.delete() }
                dirFotosOrigen.listFiles()?.forEach { archivo ->
                    archivo.copyTo(File(contexto.filesDir, archivo.name), overwrite = true)
                }
            }

            val dirPrefOrigen = File(extraerDir, PREFERENCIAS)
            if (dirPrefOrigen.exists()) {
                val dirPrefDestino = File(contexto.filesDir.parentFile, "datastore")
                dirPrefDestino.mkdirs()
                dirPrefOrigen.listFiles()?.forEach { archivo ->
                    archivo.copyTo(File(dirPrefDestino, archivo.name), overwrite = true)
                }
            }

            cacheDir.deleteRecursively()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}