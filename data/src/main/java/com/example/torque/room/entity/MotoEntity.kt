package com.example.torque.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.torque.utils.TipoVehiculo

@Entity(tableName = "motos")
data class MotoEntity(
    @PrimaryKey(autoGenerate = true) val idMoto: Long = 0,
    val marca: String = "",
    val modelo: String = "",
    val anio: Int = 0,
    val matricula: String = "",
    val color: String? = null,
    val cilindrada: String? = null,
    val caballos: String? = null,
    val estilo: String? = null,
    val kilometros: Int = 0,
    val fechaCompra: String? = null,
    val esPrincipal: Int = 0,
    val uriFoto: String? = null,
    val creadoEn: String? = null,
    val actualizadoEn: String? = null,
    val fechaItv: String? = null,
    val fechaSeguro: String? = null,
    val tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO,
    val tipoCarroceria: String? = null,
    val tipoCombustible: String? = null,
    val tipoTransmision: String? = null,
    val numeroPuertas: Int? = null
)
