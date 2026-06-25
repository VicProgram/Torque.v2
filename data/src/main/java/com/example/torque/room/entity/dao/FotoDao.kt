package com.example.torque.room.entity.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.torque.room.entity.FotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(foto: FotoEntity): Long

    @Query("SELECT * FROM fotos_moto WHERE idMoto = :idMoto ORDER BY creadoEn DESC")
    fun obtenerPorMotoFlow(idMoto: Long): Flow<List<FotoEntity>>

    @Query("SELECT * FROM fotos_moto WHERE idMoto = :idMoto")
    suspend fun obtenerPorIdMoto(idMoto: Long): List<FotoEntity>

    @Delete
    suspend fun eliminar(foto: FotoEntity)
}