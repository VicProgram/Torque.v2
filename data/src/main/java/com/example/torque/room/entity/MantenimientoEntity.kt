package com.example.torque.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mantenimientos")
data class MantenimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idMoto: Long,
    val nombreMantenimiento: String,
    val fecha: String,
    val kms: Int = 0,
    val intervaloKms: Int = 0,
    val precio: String? = null,
    val tiempo: String? = null,
    val notas: String? = null,
    val tareas: String? = null,
    val creadoEn: String? = null,
    val tipoMantenimientoId: Long = 0,
    val porcentajeVida: Int = 100
)