package com.example.torque.shared.model

data class MantenimientoDto(
    override val id: Long = 0,
    override val idMoto: Long = 0,
    override val nombreMantenimiento: String = "",
    override val fecha: String = "",
    override val kms: Int = 0,
    override val intervaloKms: Int = 0,
    override val precio: String? = null,
    override val tiempo: String? = null,
    override val notas: String? = null,
    override val tareas: String = "",
    override val tipoMantenimientoId: Long = 0,
    override val porcentajeVida: Int = 100,
    override val creadoEn: String? = null
) : MantenimientoData
