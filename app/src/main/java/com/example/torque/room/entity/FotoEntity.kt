package com.example.torque.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fotos_moto")
data class FotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idMoto: Long,
    val ruta: String, // ruta interna o cadena URI
    val creadoEn: String? = null
)