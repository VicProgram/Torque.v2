package com.example.torque.room.entity.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.torque.room.entity.FotoMantenimientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoMantenimientoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(foto: FotoMantenimientoEntity): Long

    @Query("SELECT * FROM fotos_mantenimiento WHERE idMantenimiento = :idMantenimiento ORDER BY creadoEn DESC")
    fun obtenerPorMantenimientoFlow(idMantenimiento: Long): Flow<List<FotoMantenimientoEntity>>

    @Query("SELECT * FROM fotos_mantenimiento WHERE idMantenimiento = :idMantenimiento")
    suspend fun obtenerPorMantenimiento(idMantenimiento: Long): List<FotoMantenimientoEntity>

    @Delete
    suspend fun eliminar(foto: FotoMantenimientoEntity)
}
