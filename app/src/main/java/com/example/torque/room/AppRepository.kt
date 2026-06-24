package com.example.torque.room

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.torque.room.entity.MotoEntity
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.room.entity.FotoEntity
import kotlinx.coroutines.flow.Flow

class RepositorioApp private constructor(private val db: BaseDeDatosApp) {

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

    fun obtenerMotosFlow(): Flow<List<MotoEntity>> = db.motoDao().obtenerTodasFlow()

    suspend fun insertarMoto(moto: MotoEntity): Long = try {
        db.motoDao().insertar(moto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al insertar moto", e)
        -1L
    }

    suspend fun actualizarMoto(moto: MotoEntity) = try {
        db.motoDao().actualizar(moto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar moto", e)
    }

    suspend fun eliminarMoto(moto: MotoEntity) = try {
        db.motoDao().eliminar(moto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al eliminar moto", e)
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

    fun obtenerMotoPrincipalFlow(): Flow<MotoEntity?> = db.motoDao().obtenerPrincipalFlow()

    suspend fun establecerMotoPrincipal(idMoto: Long) = try {
        db.withTransaction {
            db.motoDao().resetearPrincipales()
            db.motoDao().marcarComoPrincipal(idMoto)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error al establecer moto principal", e)
    }

    fun obtenerMantenimientosPorMoto(idMoto: Long) = db.mantenimientoDao().obtenerPorMotoFlow(idMoto)

    suspend fun insertarMantenimiento(mantenimiento: MantenimientoEntity): Long = try {
        db.mantenimientoDao().insertar(mantenimiento)
    } catch (e: Exception) {
        Log.e(TAG, "Error al insertar mantenimiento", e)
        -1L
    }

    suspend fun eliminarMantenimiento(mantenimiento: MantenimientoEntity) = try {
        db.mantenimientoDao().eliminar(mantenimiento)
    } catch (e: Exception) {
        Log.e(TAG, "Error al eliminar mantenimiento", e)
    }

    suspend fun actualizarKilometros(idMoto: Long, kilometros: Int) = try {
        db.motoDao().actualizarKilometros(idMoto, kilometros)
    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar kilómetros", e)
    }

    suspend fun obtenerUltimoMantenimiento(idMoto: Long) = try {
        db.mantenimientoDao().obtenerUltimoPorMoto(idMoto)
    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener último mantenimiento", e)
        null
    }

    fun obtenerFotosPorMoto(idMoto: Long) = db.fotoDao().obtenerPorMotoFlow(idMoto)

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
}