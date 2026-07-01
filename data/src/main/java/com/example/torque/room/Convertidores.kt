package com.example.torque.room

import androidx.room.TypeConverter
import com.example.torque.utils.TipoVehiculo

class Convertidores {
    @TypeConverter
    fun deTipoVehiculo(valor: TipoVehiculo): String = valor.name

    @TypeConverter
    fun aTipoVehiculo(valor: String): TipoVehiculo = TipoVehiculo.valueOf(valor)
}
