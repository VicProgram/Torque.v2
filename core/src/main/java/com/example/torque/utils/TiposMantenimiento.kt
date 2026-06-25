package com.example.torque.utils

data class TipoMantenimiento(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val intervaloKms: Int,
    val intervaloDias: Int = 0,
    val peso: Int,
    val esDesgaste: Boolean = false
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
    )

    val WEAR_TIPOS = listOf(
        TipoMantenimiento(21, "Neumático Delantero", "Neumáticos", 0, peso = 8, esDesgaste = true),
        TipoMantenimiento(22, "Neumático Trasero", "Neumáticos", 0, peso = 8, esDesgaste = true),
        TipoMantenimiento(10, "Pastillas de Freno Delantero", "Frenos", 5000, peso = 10, esDesgaste = true),
        TipoMantenimiento(11, "Pastillas de Freno Trasero", "Frenos", 5000, peso = 10, esDesgaste = true),
        TipoMantenimiento(13, "Discos de Freno Delanteros", "Frenos", 10000, peso = 8, esDesgaste = true),
        TipoMantenimiento(23, "Discos de Freno Traseros", "Frenos", 10000, peso = 8, esDesgaste = true),
    )

    fun desdeNombre(nombre: String): TipoMantenimiento? =
        TODOS.find { it.nombre.equals(nombre.trim(), ignoreCase = true) }

    fun desdeId(id: Long): TipoMantenimiento? =
        TODOS.find { it.id == id }
}
