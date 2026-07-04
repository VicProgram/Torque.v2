package com.example.torque.shared.model

interface MantenimientoData {
    val id: Long
    val idMoto: Long
    val nombreMantenimiento: String
    val fecha: String
    val kms: Int
    val intervaloKms: Int
    val precio: String?
    val tiempo: String?
    val notas: String?
    val tareas: String
    val tipoMantenimientoId: Long
    val porcentajeVida: Int
    val creadoEn: String?
}
