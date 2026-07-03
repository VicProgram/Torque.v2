package com.example.torque.desktop.data

import com.example.torque.desktop.data.database.DatabaseFactory
import com.example.torque.desktop.db.TorqueDatabase
import com.example.torque.shared.model.FotoData
import com.example.torque.shared.model.FotoMantenimientoData
import com.example.torque.shared.model.MantenimientoDto
import com.example.torque.shared.model.MotoData

class DesktopRepositorio {
    private val db: TorqueDatabase by lazy { DatabaseFactory.obtenerBaseDeDatos() }
    private val queries get() = db.torqueQueries

    fun obtenerMotos(): List<MotoData> {
        return queries.obtenerTodasMotos().executeAsList().map { it.toMotoData() }
    }

    fun obtenerMotoPorId(id: Long): MotoData? {
        return queries.obtenerMotoPorId(id).executeAsOneOrNull()?.toMotoData()
    }

    fun contarMotos(): Long {
        return queries.contarMotos().executeAsOne()
    }

    fun obtenerMantenimientosPorMoto(idMoto: Long): List<MantenimientoDto> {
        return queries.obtenerMantenimientosPorMoto(idMoto).executeAsList().map { it.toMantenimientoDto() }
    }

    fun obtenerFotosPorMoto(idMoto: Long): List<FotoData> {
        return queries.obtenerFotosPorMoto(idMoto).executeAsList().map {
            FotoData(id = it.id, idMoto = it.idMoto, ruta = it.ruta, creadoEn = it.creadoEn)
        }
    }

    fun cargarBackup(
        motos: List<MotoData>,
        mantenimientos: List<MantenimientoDto>,
        fotosMoto: List<FotoData>,
        fotosMantenimiento: List<FotoMantenimientoData>
    ) {
        db.transaction {
            motos.forEach { m ->
                queries.insertarMoto(
                    marca = m.marca, modelo = m.modelo, anio = m.anio.toLong(),
                    matricula = m.matricula.ifEmpty { null },
                    cilindrada = m.cilindrada, caballos = m.caballos, estilo = m.estilo,
                    kilometros = m.kilometros.toLong(),
                    esPrincipal = if (m.esPrincipal) 1L else 0L,
                    uriFoto = m.uriFoto, creadoEn = m.creadoEn, actualizadoEn = m.actualizadoEn
                )
            }
            mantenimientos.forEach { m ->
                queries.insertarMantenimiento(
                    idMoto = m.idMoto, nombreMantenimiento = m.nombreMantenimiento,
                    fecha = m.fecha, kms = m.kms.toLong(), intervaloKms = m.intervaloKms.toLong(),
                    precio = m.precio, tiempo = m.tiempo, notas = m.notas,
                    tareas = m.tareas, creadoEn = m.creadoEn,
                    tipoMantenimientoId = m.tipoMantenimientoId, porcentajeVida = m.porcentajeVida.toLong()
                )
            }
            fotosMoto.forEach { f ->
                queries.insertarFoto(idMoto = f.idMoto, ruta = f.ruta, creadoEn = f.creadoEn)
            }
            fotosMantenimiento.forEach { f ->
                queries.insertarFotoMantenimiento(
                    idMantenimiento = f.idMantenimiento, ruta = f.ruta,
                    tipo = f.tipo, creadoEn = f.creadoEn
                )
            }
        }
    }

    fun limpiarTodo() {
        db.transaction {
            queries.eliminarTodoMantenimientos()
            queries.eliminarTodoFotos()
            queries.eliminarTodoFotosMantenimiento()
            queries.eliminarTodoMotos()
        }
    }
}

private fun com.example.torque.desktop.db.Moto.toMotoData(): MotoData = MotoData(
    id = idMoto, marca = marca, modelo = modelo, anio = anio.toInt(),
    matricula = matricula ?: "", kilometros = kilometros.toInt(),
    cilindrada = cilindrada, caballos = caballos, estilo = estilo,
    uriFoto = uriFoto, esPrincipal = esPrincipal == 1L,
    fechaItv = fechaItv, fechaSeguro = fechaSeguro,
    creadoEn = creadoEn, actualizadoEn = actualizadoEn
)

private fun com.example.torque.desktop.db.Mantenimiento.toMantenimientoDto(): MantenimientoDto = MantenimientoDto(
    id = id, idMoto = idMoto, nombreMantenimiento = nombreMantenimiento,
    fecha = fecha, kms = kms.toInt(), intervaloKms = intervaloKms.toInt(),
    precio = precio, tiempo = tiempo, notas = notas, tareas = tareas ?: "",
    tipoMantenimientoId = tipoMantenimientoId, porcentajeVida = porcentajeVida.toInt(),
    creadoEn = creadoEn
)
