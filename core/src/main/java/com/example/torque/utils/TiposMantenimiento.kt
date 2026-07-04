package com.example.torque.utils

enum class TipoVehiculo { MOTO, COCHE }

data class TipoMantenimiento(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val intervaloKms: Int,
    val intervaloDias: Int = 0,
    val peso: Int,
    val esDesgaste: Boolean = false,
    val tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO
)

object TiposMantenimiento {
    val TODOS = listOf(
        TipoMantenimiento(1, "Cambio de Aceite Motor", "Motor", 5000, peso = 10),
        TipoMantenimiento(2, "Filtro de Aceite", "Motor", 5000, peso = 5),
        TipoMantenimiento(3, "Bujías", "Motor", 10000, peso = 6),
        TipoMantenimiento(4, "Filtro de Aire", "Motor", 10000, peso = 5),
        TipoMantenimiento(5, "Líquido Refrigerante", "Motor", 20000, peso = 5),
        TipoMantenimiento(6, "Reglaje de Válvulas", "Motor", 12000, peso = 6),
        TipoMantenimiento(7, "Limpieza y Engrase de Cadena", "Transmisión", 500, peso = 7),
        TipoMantenimiento(8, "Tensado de Cadena", "Transmisión", 1000, peso = 6),
        TipoMantenimiento(9, "Cambio de Cadena y Coronas", "Transmisión", 20000, peso = 7),
        TipoMantenimiento(10, "Pastillas de Freno Delantero", "Frenos", 5000, peso = 10, esDesgaste = true),
        TipoMantenimiento(11, "Pastillas de Freno Trasero", "Frenos", 5000, peso = 10, esDesgaste = true),
        TipoMantenimiento(12, "Líquido de Frenos", "Frenos", 20000, peso = 8),
        TipoMantenimiento(13, "Discos de Freno Delanteros", "Frenos", 10000, peso = 8, esDesgaste = true),
        TipoMantenimiento(14, "Presión de Neumáticos", "Neumáticos", 0, peso = 4),
        TipoMantenimiento(15, "Cambio de Neumáticos", "Neumáticos", 10000, peso = 8),
        TipoMantenimiento(16, "Batería", "Eléctrico", 0, 180, peso = 4),
        TipoMantenimiento(17, "Luces y Señalización", "Eléctrico", 0, 30, peso = 3),
        TipoMantenimiento(18, "Centralita / ECU", "Eléctrico", 0, 365, peso = 4),
        TipoMantenimiento(19, "Suspensión Delantera", "Suspensión", 15000, peso = 5),
        TipoMantenimiento(20, "Suspensión Trasera", "Suspensión", 15000, peso = 5),
        TipoMantenimiento(21, "Neumático Delantero", "Neumáticos", 0, peso = 8, esDesgaste = true),
        TipoMantenimiento(22, "Neumático Trasero", "Neumáticos", 0, peso = 8, esDesgaste = true),
        TipoMantenimiento(23, "Discos de Freno Traseros", "Frenos", 10000, peso = 8, esDesgaste = true),
        TipoMantenimiento(24, "Revisión de Retenes Horquilla", "Suspensión", 5000, peso = 5),
        TipoMantenimiento(25, "Cambio de Retenes Horquilla", "Suspensión", 10000, peso = 6),
        // Coche
        TipoMantenimiento(26, "Cambio de Aceite Motor", "Motor", 8000, peso = 10, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(27, "Filtro de Aceite", "Motor", 8000, peso = 5, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(28, "Bujías", "Motor", 20000, peso = 6, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(29, "Filtro de Aire", "Motor", 15000, peso = 5, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(30, "Correa de Distribución", "Motor", 60000, peso = 10, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(31, "Líquido Refrigerante", "Motor", 40000, peso = 5, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(32, "Aceite de Cambio", "Transmisión", 60000, peso = 6, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(33, "Pastillas de Freno", "Frenos", 30000, peso = 10, esDesgaste = true, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(34, "Discos de Freno", "Frenos", 60000, peso = 8, esDesgaste = true, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(35, "Líquido de Frenos", "Frenos", 40000, peso = 8, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(36, "Presión de Neumáticos", "Neumáticos", 0, peso = 4, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(37, "Cambio de Neumáticos", "Neumáticos", 40000, peso = 8, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(38, "Rotación de Neumáticos", "Neumáticos", 10000, peso = 5, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(39, "Batería", "Eléctrico", 0, 180, peso = 4, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(40, "Luces y Señalización", "Eléctrico", 0, 30, peso = 3, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(41, "Centralita / ECU", "Eléctrico", 0, 365, peso = 4, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(42, "Filtro de Habitáculo", "Climatización", 15000, peso = 5, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(43, "Gas Aire Acondicionado", "Climatización", 0, 730, peso = 5, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(44, "Líquido de Dirección", "Dirección", 60000, peso = 6, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(45, "Filtro de Gasóleo", "Combustible", 30000, peso = 5, tipoVehiculo = TipoVehiculo.COCHE),
    )

    val WEAR_TIPOS = listOf(
        TipoMantenimiento(21, "Neumático Delantero", "Neumáticos", 0, peso = 8, esDesgaste = true),
        TipoMantenimiento(22, "Neumático Trasero", "Neumáticos", 0, peso = 8, esDesgaste = true),
        TipoMantenimiento(10, "Pastillas de Freno Delantero", "Frenos", 5000, peso = 10, esDesgaste = true),
        TipoMantenimiento(11, "Pastillas de Freno Trasero", "Frenos", 5000, peso = 10, esDesgaste = true),
        TipoMantenimiento(13, "Discos de Freno Delanteros", "Frenos", 10000, peso = 8, esDesgaste = true),
        TipoMantenimiento(23, "Discos de Freno Traseros", "Frenos", 10000, peso = 8, esDesgaste = true),
        TipoMantenimiento(33, "Pastillas de Freno", "Frenos", 30000, peso = 10, esDesgaste = true, tipoVehiculo = TipoVehiculo.COCHE),
        TipoMantenimiento(34, "Discos de Freno", "Frenos", 60000, peso = 8, esDesgaste = true, tipoVehiculo = TipoVehiculo.COCHE),
    )

    fun obtenerPorTipo(tipoVehiculo: TipoVehiculo): List<TipoMantenimiento> =
        TODOS.filter { it.tipoVehiculo == tipoVehiculo }

    fun desdeNombre(nombre: String): TipoMantenimiento? =
        TODOS.find { it.nombre.equals(nombre.trim(), ignoreCase = true) }

    fun desdeNombre(nombre: String, tipoVehiculo: TipoVehiculo): TipoMantenimiento? =
        TODOS.find { it.nombre.equals(nombre.trim(), ignoreCase = true) && it.tipoVehiculo == tipoVehiculo }

    fun desdeId(id: Long): TipoMantenimiento? =
        TODOS.find { it.id == id }
}
