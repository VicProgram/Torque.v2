package com.example.torque.shared.model

data class DetalleSalud(
    val tipo: TipoMantenimiento,
    val ultimoKms: Int?,
    val cumplimiento: Int,
    val relevancia: Int
)

data class ResultadoSalud(
    val saludGlobal: Int,
    val desglose: List<DetalleSalud>,
    val proximosVencidos: List<DetalleSalud>
)
