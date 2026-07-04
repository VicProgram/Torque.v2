package com.example.torque.shared.model

data class FotoMantenimientoData(
    val id: Long = 0,
    val idMantenimiento: Long = 0,
    val ruta: String = "",
    val tipo: String = "foto",
    val creadoEn: String? = null
)
