package com.example.torque.room.entity.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.torque.room.entity.MotoEntity

@Dao
interface MotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(moto: MotoEntity): Long

    @Update
    suspend fun actualizar(moto: MotoEntity)

    @Delete
    suspend fun eliminar(moto: MotoEntity)

    @Query("SELECT * FROM motos ORDER BY creadoEn DESC")
    fun obtenerTodasFlow(): Flow<List<MotoEntity>>

    @Query("SELECT * FROM motos WHERE idMoto = :id")
    suspend fun obtenerPorId(id: Long): MotoEntity?

    @Query("SELECT COUNT(*) FROM motos")
    suspend fun contarMotos(): Int

    @Query("SELECT * FROM motos WHERE esPrincipal = 1 LIMIT 1")
    fun obtenerPrincipalFlow(): Flow<MotoEntity?>

    @Query("UPDATE motos SET esPrincipal = 0")
    suspend fun resetearPrincipales()

    @Query("UPDATE motos SET esPrincipal = 1 WHERE idMoto = :id")
    suspend fun marcarComoPrincipal(id: Long)

    @Query("UPDATE motos SET kilometros = :kilometros WHERE idMoto = :idMoto")
    suspend fun actualizarKilometros(idMoto: Long, kilometros: Int)
}