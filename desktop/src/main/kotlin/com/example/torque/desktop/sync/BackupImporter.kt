package com.example.torque.desktop.sync

import com.example.torque.desktop.data.DesktopRepositorio
import com.example.torque.shared.model.FotoData
import com.example.torque.shared.model.FotoMantenimientoData
import com.example.torque.shared.model.MantenimientoDto
import com.example.torque.shared.model.MotoData
import com.example.torque.shared.model.TipoVehiculo
import java.io.File
import java.sql.DriverManager
import java.util.zip.ZipFile

class BackupImporter(private val repositorio: DesktopRepositorio) {

    fun importar(rutaZip: String) {
        val zipFile = ZipFile(rutaZip)
        val tempDir = File(System.getProperty("java.io.tmpdir"), "torque_restore_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        try {
            zipFile.extractTo(tempDir)

            val dbFile = tempDir.resolve("torque_room.db")
            if (!dbFile.exists()) throw IllegalStateException("No se encuentra torque_room.db en el backup")

            val motos = mutableListOf<MotoData>()
            val mantenimientos = mutableListOf<MantenimientoDto>()
            val fotos = mutableListOf<FotoData>()
            val fotosMantenimiento = mutableListOf<FotoMantenimientoData>()

            DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}").use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT * FROM motos").use { rs ->
                        while (rs.next()) {
                            motos.add(MotoData(
                                id = rs.getLong("idMoto"),
                                marca = rs.getString("marca") ?: "",
                                modelo = rs.getString("modelo") ?: "",
                                anio = rs.getInt("anio"),
                                matricula = rs.getString("matricula") ?: "",
                                color = rs.getString("color"),
                                cilindrada = rs.getString("cilindrada"),
                                caballos = rs.getString("caballos"),
                                estilo = rs.getString("estilo"),
                                kilometros = rs.getInt("kilometros"),
                                fechaCompra = rs.getString("fechaCompra"),
                                esPrincipal = rs.getInt("esPrincipal") == 1,
                                uriFoto = rs.getString("uriFoto"),
                                creadoEn = rs.getString("creadoEn"),
                                actualizadoEn = rs.getString("actualizadoEn"),
                                fechaItv = rs.getString("fechaItv"),
                                fechaSeguro = rs.getString("fechaSeguro"),
                                tipoVehiculo = try { TipoVehiculo.valueOf(rs.getString("tipoVehiculo") ?: "MOTO") } catch (_: Exception) { TipoVehiculo.MOTO },
                                tipoCarroceria = rs.getString("tipoCarroceria"),
                                tipoCombustible = rs.getString("tipoCombustible"),
                                tipoTransmision = rs.getString("tipoTransmision"),
                                numeroPuertas = try { rs.getInt("numeroPuertas"); rs.getInt("numeroPuertas").let { if (rs.wasNull()) null else it } } catch (_: Exception) { null }
                            ))
                        }
                    }

                    stmt.executeQuery("SELECT * FROM mantenimientos").use { rs ->
                        while (rs.next()) {
                            mantenimientos.add(MantenimientoDto(
                                id = rs.getLong("id"),
                                idMoto = rs.getLong("idMoto"),
                                nombreMantenimiento = rs.getString("nombreMantenimiento") ?: "",
                                fecha = rs.getString("fecha") ?: "",
                                kms = rs.getInt("kms"),
                                intervaloKms = rs.getInt("intervaloKms"),
                                precio = rs.getString("precio"),
                                tiempo = rs.getString("tiempo"),
                                notas = rs.getString("notas"),
                                tareas = rs.getString("tareas") ?: "",
                                tipoMantenimientoId = rs.getLong("tipoMantenimientoId"),
                                porcentajeVida = rs.getInt("porcentajeVida"),
                                creadoEn = rs.getString("creadoEn")
                            ))
                        }
                    }

                    stmt.executeQuery("SELECT * FROM fotos_moto").use { rs ->
                        while (rs.next()) {
                            fotos.add(FotoData(
                                id = rs.getLong("id"),
                                idMoto = rs.getLong("idMoto"),
                                ruta = rs.getString("ruta") ?: "",
                                creadoEn = rs.getString("creadoEn")
                            ))
                        }
                    }

                    stmt.executeQuery("SELECT * FROM fotos_mantenimiento").use { rs ->
                        while (rs.next()) {
                            fotosMantenimiento.add(FotoMantenimientoData(
                                id = rs.getLong("id"),
                                idMantenimiento = rs.getLong("idMantenimiento"),
                                ruta = rs.getString("ruta") ?: "",
                                tipo = rs.getString("tipo") ?: "foto",
                                creadoEn = rs.getString("creadoEn")
                            ))
                        }
                    }
                }
            }

            val dirFotosTemp = tempDir.resolve("fotos")
            if (dirFotosTemp.exists()) {
                val dirDestino = File(System.getProperty("user.home"), ".torque/fotos")
                dirDestino.mkdirs()
                dirFotosTemp.copyRecursively(dirDestino, overwrite = true)
            }

            repositorio.limpiarTodo()
            repositorio.cargarBackup(motos, mantenimientos, fotos, fotosMantenimiento)

        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun ZipFile.extractTo(destDir: File) {
        entries().asSequence().forEach { entry ->
            val file = destDir.resolve(entry.name)
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                file.parentFile.mkdirs()
                getInputStream(entry).use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
    }
}
