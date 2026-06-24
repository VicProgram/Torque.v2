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

    @Query("SELECT * FROM mantenimientos WHERE idMoto = :idMoto ORDER BY fecha DESC LIMIT 1")
    suspend fun obtenerUltimoPorMoto(idMoto: Long): MantenimientoEntity?

    @Delete
    suspend fun eliminar(mantenimiento: MantenimientoEntity)
}