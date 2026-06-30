package com.example.torque.room

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.torque.room.entity.MotoEntity
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.room.entity.FotoEntity
import com.example.torque.room.entity.FotoMantenimientoEntity
import kotlinx.coroutines.flow.Flow

open class RepositorioApp(private val db: BaseDeDatosApp) {

    companion object {
        private const val TAG = "RepositorioApp"
        @Volatile
        private var instance: RepositorioApp? = null

        fun obtenerInstancia(context: Context): RepositorioApp =
            instance ?: synchronized(this) {
                instance ?: RepositorioApp(
                    BaseDeDatosApp.obtenerInstancia(context)
                ).also { instance = it }
            }
    }

    open fun obtenerMotosFlow(): Flow<List<MotoEntity>> = db.motoDao().obtenerTodasFlow()

    suspend fun insertarMoto(moto: MotoEntity): Long = try {
        db.motoDao().insertar(moto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al insertar moto", e)
        -1L
    }

    suspend fun actualizarMoto(moto: MotoEntity) {
        try {
            db.motoDao().actualizar(moto)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar moto", e)
            throw e
        }
    }

    suspend fun eliminarMoto(moto: MotoEntity) {
        try {
            db.motoDao().eliminar(moto)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar moto", e)
            throw e
        }
    }

    suspend fun obtenerMotoPorId(id: Long): MotoEntity? = try {
        db.motoDao().obtenerPorId(id)
    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener moto por id", e)
        null
    }

    suspend fun contarMotos(): Int = try {
        db.motoDao().contarMotos()
    } catch (e: Exception) {
        Log.e(TAG, "Error al contar motos", e)
        0
    }

    open fun obtenerMotoPrincipalFlow(): Flow<MotoEntity?> = db.motoDao().obtenerPrincipalFlow()

    suspend fun establecerMotoPrincipal(idMoto: Long) = try {
        db.withTransaction {
            db.motoDao().resetearPrincipales()
            db.motoDao().marcarComoPrincipal(idMoto)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error al establecer moto principal", e)
    }

    open fun obtenerMantenimientosPorMoto(idMoto: Long) = db.mantenimientoDao().obtenerPorMotoFlow(idMoto)

    suspend fun obtenerTodosMantenimientosPorMoto(idMoto: Long): List<MantenimientoEntity> = try {
        db.mantenimientoDao().obtenerPorMoto(idMoto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener mantenimientos", e)
        emptyList()
    }

    open suspend fun insertarMantenimiento(mantenimiento: MantenimientoEntity): Long {
        return try {
            db.mantenimientoDao().insertar(mantenimiento)
        } catch (e: Exception) {
            Log.e(TAG, "Error al insertar mantenimiento", e)
            throw e
        }
    }

    suspend fun eliminarMantenimiento(mantenimiento: MantenimientoEntity) = try {
        db.mantenimientoDao().eliminar(mantenimiento)
    } catch (e: Exception) {
        Log.e(TAG, "Error al eliminar mantenimiento", e)
    }

    open suspend fun actualizarKilometros(idMoto: Long, kilometros: Int) = try {
        db.motoDao().actualizarKilometros(idMoto, kilometros)
    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar kilómetros", e)
    }

    open suspend fun obtenerUltimoMantenimiento(idMoto: Long) = try {
        db.mantenimientoDao().obtenerUltimoPorMoto(idMoto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener último mantenimiento", e)
        null
    }

    open suspend fun obtenerUltimoMantenimientoPorTipo(idMoto: Long, tipoId: Long) = try {
        db.mantenimientoDao().obtenerUltimoPorTipo(idMoto, tipoId)
    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener mantenimiento por tipo", e)
        null
    }

    open suspend fun actualizarPorcentajeVida(id: Long, porcentaje: Int) = try {
        db.mantenimientoDao().actualizarPorcentajeVida(id, porcentaje)
    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar porcentaje de vida", e)
    }

    open fun obtenerFotosPorMoto(idMoto: Long) = db.fotoDao().obtenerPorMotoFlow(idMoto)

    suspend fun insertarFoto(foto: FotoEntity) = try {
        db.fotoDao().insertar(foto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al insertar foto", e)
    }

    suspend fun eliminarFoto(foto: FotoEntity) = try {
        db.fotoDao().eliminar(foto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al eliminar foto", e)
    }

    open fun obtenerFotosPorMantenimiento(idMantenimiento: Long) = db.fotoMantenimientoDao().obtenerPorMantenimientoFlow(idMantenimiento)

    open suspend fun obtenerFotosPorMantenimientoList(idMantenimiento: Long) = db.fotoMantenimientoDao().obtenerPorMantenimiento(idMantenimiento)

    open suspend fun obtenerFotosPorMantenimientoBatch(ids: List<Long>) = db.fotoMantenimientoDao().obtenerPorIdsMantenimiento(ids)

    suspend fun insertarFotoMantenimiento(foto: FotoMantenimientoEntity) = try {
        db.fotoMantenimientoDao().insertar(foto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al insertar foto de mantenimiento", e)
    }

    suspend fun eliminarFotoMantenimiento(foto: FotoMantenimientoEntity) = try {
        db.fotoMantenimientoDao().eliminar(foto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al eliminar foto de mantenimiento", e)
    }

    fun cerrarBaseDeDatos() {
        BaseDeDatosApp.cerrarYReiniciar()
        instance = null
    }
}