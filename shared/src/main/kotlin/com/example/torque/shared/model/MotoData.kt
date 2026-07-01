package com.example.torque.shared.model

data class MotoData(
    val id: Long = 0,
    val marca: String = "",
    val modelo: String = "",
    val anio: Int = 0,
    val matricula: String = "",
    val kilometros: Int = 0,
    val cilindrada: String? = null,
    val caballos: String? = null,
    val estilo: String? = null,
    val uriFoto: String? = null,
    val esPrincipal: Boolean = false,
    val fechaItv: String? = null,
    val fechaSeguro: String? = null,
    val creadoEn: String? = null,
    val actualizadoEn: String? = null
)
