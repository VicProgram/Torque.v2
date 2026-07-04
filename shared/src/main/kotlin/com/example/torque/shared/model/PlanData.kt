package com.example.torque.shared.model

data class PlanData(
    val id: Long = 0,
    val idMoto: Long = 0,
    val nombreTarea: String = "",
    val categoria: String = "",
    val intervaloKms: Int = 0,
    val intervaloDias: Int = 0,
    val ultimoCumplimientoKm: Int = 0,
    val realizado: Boolean = false
)
