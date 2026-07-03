package com.example.torque.maintenance

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.torque.R
import com.example.torque.ui.theme.AmarilloAdvertencia
import com.example.torque.ui.theme.RojoError
import com.example.torque.utils.TipoVehiculo

data class Tarea(
    val nombre: String,
    val descripcion: String,
    val icono: ImageVector,
    val colorIcono: Color,
    val categoria: String
)

private fun tareasMotorBase() = listOf(
    Tarea("Cambio de Aceite Motor", "Cambiar el aceite del motor", Icons.Default.OilBarrel, Color(0xFF3B82F6), "Motor"),
    Tarea("Filtro de Aceite", "Cambiar el filtro de aceite", Icons.Default.FilterAlt, Color(0xFFF97316), "Motor"),
    Tarea("Bujías", "Revisar y cambiar bujías", Icons.Default.FlashOn, Color(0xFFFACC15), "Motor"),
    Tarea("Filtro de Aire", "Cambiar el filtro de aire del motor", Icons.Default.Air, Color(0xFF60A5FA), "Motor"),
    Tarea("Líquido Refrigerante", "Revisar nivel y cambiar refrigerante", Icons.Default.WaterDrop, Color(0xFF06B6D4), "Motor"),
    Tarea("Reglaje de Válvulas", "Ajustar la holgura de válvulas", Icons.Default.Tune, Color(0xFF8B5CF6), "Motor")
)

@Composable
fun obtenerTareasMotor(tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO): List<Tarea> = when (tipoVehiculo) {
    TipoVehiculo.COCHE -> tareasMotorBase() + listOf(
        Tarea("Correa de Distribución", "Cambiar la correa de distribución", Icons.Default.Settings, Color(0xFF8B5CF6), "Motor"),
    )
    TipoVehiculo.MOTO -> tareasMotorBase()
}

private fun tareasTransmisionBase() = listOf(
    Tarea("Aceite de Cambio", "Cambiar el aceite de la transmisión", Icons.Default.SettingsInputComponent, Color(0xFF6366F1), "Transmisión"),
)

@Composable
fun obtenerTareasTransmision(tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO): List<Tarea> = when (tipoVehiculo) {
    TipoVehiculo.MOTO -> listOf(
        Tarea(stringResource(R.string.task_chain_clean), stringResource(R.string.task_chain_clean_desc), Icons.Default.SettingsInputComponent, Color(0xFF6366F1), "Transmisión"),
        Tarea(stringResource(R.string.task_chain_tension), stringResource(R.string.task_chain_tension_desc), Icons.Default.Link, Color(0xFFA78BFA), "Transmisión"),
        Tarea(stringResource(R.string.task_chain_replace), stringResource(R.string.task_chain_replace_desc), Icons.Default.Cyclone, Color(0xFF818CF8), "Transmisión")
    )
    TipoVehiculo.COCHE -> tareasTransmisionBase()
}

@Composable
fun obtenerTareasFrenos(tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO): List<Tarea> = when (tipoVehiculo) {
    TipoVehiculo.MOTO -> listOf(
        Tarea(stringResource(R.string.task_brake_pads_front), stringResource(R.string.task_brake_pads_front_desc), Icons.Default.CarRepair, RojoError, "Frenos"),
        Tarea(stringResource(R.string.task_brake_pads_rear), stringResource(R.string.task_brake_pads_rear_desc), Icons.Default.CarRepair, Color(0xFFDC2626), "Frenos"),
        Tarea(stringResource(R.string.task_brake_fluid), stringResource(R.string.task_brake_fluid_desc), Icons.Default.Dangerous, Color(0xFFB91C1C), "Frenos"),
        Tarea(stringResource(R.string.task_brake_discs), stringResource(R.string.task_brake_discs_desc), Icons.Default.DiscFull, Color(0xFF991B1B), "Frenos")
    )
    TipoVehiculo.COCHE -> listOf(
        Tarea("Pastillas de Freno", "Cambiar pastillas de freno", Icons.Default.CarRepair, RojoError, "Frenos"),
        Tarea("Discos de Freno", "Cambiar discos de freno", Icons.Default.DiscFull, Color(0xFF991B1B), "Frenos"),
        Tarea("Líquido de Frenos", "Cambiar el líquido de frenos", Icons.Default.Dangerous, Color(0xFFB91C1C), "Frenos"),
    )
}

@Composable
fun obtenerTareasNeumaticos(tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO): List<Tarea> = when (tipoVehiculo) {
    TipoVehiculo.MOTO -> listOf(
        Tarea(stringResource(R.string.task_tire_pressure), stringResource(R.string.task_tire_pressure_desc), Icons.Default.TireRepair, Color(0xFF10B981), "Neumáticos"),
        Tarea(stringResource(R.string.task_tire_change), stringResource(R.string.task_tire_change_desc), Icons.Default.Circle, Color(0xFF34D399), "Neumáticos")
    )
    TipoVehiculo.COCHE -> listOf(
        Tarea("Presión de Neumáticos", "Revisar presión de los neumáticos", Icons.Default.TireRepair, Color(0xFF10B981), "Neumáticos"),
        Tarea("Cambio de Neumáticos", "Cambiar neumáticos", Icons.Default.Circle, Color(0xFF34D399), "Neumáticos"),
        Tarea("Rotación de Neumáticos", "Rotar neumáticos para desgaste uniforme", Icons.Default.Loop, Color(0xFF6EE7B7), "Neumáticos"),
    )
}

@Composable
fun obtenerTareasElectrico(tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO): List<Tarea> = when (tipoVehiculo) {
    TipoVehiculo.MOTO -> listOf(
        Tarea(stringResource(R.string.task_battery), stringResource(R.string.task_battery_desc), Icons.Default.BatteryStd, Color(0xFF84CC16), "Eléctrico"),
        Tarea(stringResource(R.string.task_lights), stringResource(R.string.task_lights_desc), Icons.Default.Lightbulb, AmarilloAdvertencia, "Eléctrico"),
        Tarea(stringResource(R.string.task_ecu), stringResource(R.string.task_ecu_desc), Icons.Default.Memory, Color(0xFFA3E635), "Eléctrico")
    )
    TipoVehiculo.COCHE -> listOf(
        Tarea("Batería", "Revisar y cambiar batería", Icons.Default.BatteryStd, Color(0xFF84CC16), "Eléctrico"),
        Tarea("Luces", "Revisar luces y señalización", Icons.Default.Lightbulb, AmarilloAdvertencia, "Eléctrico"),
        Tarea("Centralita / ECU", "Revisar centralita electrónica", Icons.Default.Memory, Color(0xFFA3E635), "Eléctrico"),
    )
}

@Composable
fun obtenerTareasSuspension(tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO): List<Tarea> = when (tipoVehiculo) {
    TipoVehiculo.MOTO -> listOf(
        Tarea(stringResource(R.string.task_suspension_front), stringResource(R.string.task_suspension_front_desc), Icons.Default.ArrowDownward, Color(0xFFE87930), "Suspensión"),
        Tarea(stringResource(R.string.task_suspension_rear), stringResource(R.string.task_suspension_rear_desc), Icons.Default.ArrowUpward, Color(0xFFE87930), "Suspensión"),
        Tarea(stringResource(R.string.task_fork_seals_inspect), stringResource(R.string.task_fork_seals_inspect_desc), Icons.Default.Visibility, Color(0xFFE87930), "Suspensión"),
        Tarea(stringResource(R.string.task_fork_seals_replace), stringResource(R.string.task_fork_seals_replace_desc), Icons.Default.Build, Color(0xFFE87930), "Suspensión")
    )
    TipoVehiculo.COCHE -> emptyList()
}

@Composable
fun obtenerTareasClimatizacion(): List<Tarea> = listOf(
    Tarea("Filtro de Habitáculo", "Cambiar el filtro del habitáculo", Icons.Default.Air, Color(0xFF60A5FA), "Climatización"),
    Tarea("Gas Aire Acondicionado", "Recargar gas del aire acondicionado", Icons.Default.AcUnit, Color(0xFF06B6D4), "Climatización"),
)

@Composable
fun obtenerTareasCombustible(): List<Tarea> = listOf(
    Tarea("Filtro de Gasóleo", "Cambiar el filtro de gasóleo", Icons.Default.FilterAlt, Color(0xFFF97316), "Combustible"),
)

@Composable
fun obtenerTareasDireccion(): List<Tarea> = listOf(
    Tarea("Líquido de Dirección", "Revisar y cambiar líquido de dirección", Icons.Default.Settings, Color(0xFF8B5CF6), "Dirección"),
)

@Composable
fun obtenerNombresSeccion(): Map<String, String> = mapOf(
    "Motor" to stringResource(R.string.category_motor),
    "Transmisión" to stringResource(R.string.category_transmission),
    "Frenos" to stringResource(R.string.category_brakes),
    "Neumáticos" to stringResource(R.string.category_tires),
    "Eléctrico" to stringResource(R.string.category_electrical),
    "Suspensión" to stringResource(R.string.category_suspension),
    "Climatización" to stringResource(R.string.category_climate),
    "Combustible" to stringResource(R.string.category_fuel),
    "Dirección" to stringResource(R.string.category_steering)
)

@Composable
fun obtenerSecciones(
    tareasMotor: List<Tarea>,
    tareasTransmision: List<Tarea>,
    tareasFrenos: List<Tarea>,
    tareasNeumaticos: List<Tarea>,
    tareasElectrico: List<Tarea>,
    tareasSuspension: List<Tarea>
): List<Pair<String, List<Tarea>>> = listOf(
    "Motor" to tareasMotor,
    "Transmisión" to tareasTransmision,
    "Frenos" to tareasFrenos,
    "Neumáticos" to tareasNeumaticos,
    "Eléctrico" to tareasElectrico,
    "Suspensión" to tareasSuspension
)

@Composable
fun obtenerTodasLasTareas(
    tareasMotor: List<Tarea>,
    tareasTransmision: List<Tarea>,
    tareasFrenos: List<Tarea>,
    tareasNeumaticos: List<Tarea>,
    tareasElectrico: List<Tarea>,
    tareasSuspension: List<Tarea>
): List<Tarea> = tareasMotor + tareasTransmision + tareasFrenos +
    tareasNeumaticos + tareasElectrico + tareasSuspension
