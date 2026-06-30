package com.example.torque.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fotos_mantenimiento")
data class FotoMantenimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idMantenimiento: Long,
    val ruta: String,
    val tipo: String = "foto",
    val creadoEn: String? = null
)
