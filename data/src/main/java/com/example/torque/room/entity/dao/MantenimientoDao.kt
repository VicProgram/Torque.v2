package com.example.torque.room.entity.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.torque.room.entity.MantenimientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MantenimientoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(mantenimiento: MantenimientoEntity): Long

    @Query("SELECT * FROM mantenimientos WHERE idMoto = :idMoto ORDER BY fecha DESC")
    fun obtenerPorMotoFlow(idMoto: Long): Flow<List<MantenimientoEntity>>

    @Query("SELECT * FROM mantenimientos WHERE idMoto = :idMoto ORDER BY fecha DESC")
    suspend fun obtenerPorMoto(idMoto: Long): List<MantenimientoEntity>

    @Query("SELECT * FROM mantenimientos WHERE idMoto = :idMoto AND tipoMantenimientoId = :tipoId ORDER BY kms DESC LIMIT 1")
    suspend fun obtenerUltimoPorTipo(idMoto: Long, tipoId: Long): MantenimientoEntity?

    @Query("""
        SELECT m1.* FROM mantenimientos m1
        INNER JOIN (
            SELECT tipoMantenimientoId, MAX(kms) AS max_kms
            FROM mantenimientos
            WHERE idMoto = :idMoto AND tipoMantenimientoId IN (:tipoIds)
            GROUP BY tipoMantenimientoId
        ) m2 ON m1.tipoMantenimientoId = m2.tipoMantenimientoId AND m1.kms = m2.max_kms
        WHERE m1.idMoto = :idMoto
    """)
    suspend fun obtenerUltimosPorTipos(idMoto: Long, tipoIds: List<Long>): List<MantenimientoEntity>

    @Query("UPDATE mantenimientos SET porcentajeVida = :porcentaje WHERE id = :id")
    suspend fun actualizarPorcentajeVida(id: Long, porcentaje: Int)

    @Query("SELECT * FROM mantenimientos WHERE idMoto = :idMoto ORDER BY fecha DESC LIMIT 1")
    suspend fun obtenerUltimoPorMoto(idMoto: Long): MantenimientoEntity?

    @Delete
    suspend fun eliminar(mantenimiento: MantenimientoEntity)
}