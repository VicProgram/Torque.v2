package com.example.torque.shared.model

data class ItvData(
    val idMoto: Long = 0,
    val fechaItv: String? = null,
    val fechaRecordatorio: String? = null
)

data class SeguroData(
    val idMoto: Long = 0,
    val fechaSeguro: String? = null,
    val fechaRecordatorio: String? = null
)
