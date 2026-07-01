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

data class Tarea(
    val nombre: String,
    val descripcion: String,
    val icono: ImageVector,
    val colorIcono: Color,
    val categoria: String
)

@Composable
fun obtenerTareasMotor(): List<Tarea> = listOf(
    Tarea(stringResource(R.string.task_oil_change), stringResource(R.string.task_oil_change_desc), Icons.Default.OilBarrel, Color(0xFF3B82F6), "Motor"),
    Tarea(stringResource(R.string.task_oil_filter), stringResource(R.string.task_oil_filter_desc), Icons.Default.FilterAlt, Color(0xFFF97316), "Motor"),
    Tarea(stringResource(R.string.task_spark_plugs), stringResource(R.string.task_spark_plugs_desc), Icons.Default.FlashOn, Color(0xFFFACC15), "Motor"),
    Tarea(stringResource(R.string.task_air_filter), stringResource(R.string.task_air_filter_desc), Icons.Default.Air, Color(0xFF60A5FA), "Motor"),
    Tarea(stringResource(R.string.task_coolant), stringResource(R.string.task_coolant_desc), Icons.Default.WaterDrop, Color(0xFF06B6D4), "Motor"),
    Tarea(stringResource(R.string.task_valves), stringResource(R.string.task_valves_desc), Icons.Default.Tune, Color(0xFF8B5CF6), "Motor")
)

@Composable
fun obtenerTareasTransmision(): List<Tarea> = listOf(
    Tarea(stringResource(R.string.task_chain_clean), stringResource(R.string.task_chain_clean_desc), Icons.Default.SettingsInputComponent, Color(0xFF6366F1), "Transmisión"),
    Tarea(stringResource(R.string.task_chain_tension), stringResource(R.string.task_chain_tension_desc), Icons.Default.Link, Color(0xFFA78BFA), "Transmisión"),
    Tarea(stringResource(R.string.task_chain_replace), stringResource(R.string.task_chain_replace_desc), Icons.Default.Cyclone, Color(0xFF818CF8), "Transmisión")
)

@Composable
fun obtenerTareasFrenos(): List<Tarea> = listOf(
    Tarea(stringResource(R.string.task_brake_pads_front), stringResource(R.string.task_brake_pads_front_desc), Icons.Default.CarRepair, RojoError, "Frenos"),
    Tarea(stringResource(R.string.task_brake_pads_rear), stringResource(R.string.task_brake_pads_rear_desc), Icons.Default.CarRepair, Color(0xFFDC2626), "Frenos"),
    Tarea(stringResource(R.string.task_brake_fluid), stringResource(R.string.task_brake_fluid_desc), Icons.Default.Dangerous, Color(0xFFB91C1C), "Frenos"),
    Tarea(stringResource(R.string.task_brake_discs), stringResource(R.string.task_brake_discs_desc), Icons.Default.DiscFull, Color(0xFF991B1B), "Frenos")
)

@Composable
fun obtenerTareasNeumaticos(): List<Tarea> = listOf(
    Tarea(stringResource(R.string.task_tire_pressure), stringResource(R.string.task_tire_pressure_desc), Icons.Default.TireRepair, Color(0xFF10B981), "Neumáticos"),
    Tarea(stringResource(R.string.task_tire_change), stringResource(R.string.task_tire_change_desc), Icons.Default.Circle, Color(0xFF34D399), "Neumáticos")
)

@Composable
fun obtenerTareasElectrico(): List<Tarea> = listOf(
    Tarea(stringResource(R.string.task_battery), stringResource(R.string.task_battery_desc), Icons.Default.BatteryStd, Color(0xFF84CC16), "Eléctrico"),
    Tarea(stringResource(R.string.task_lights), stringResource(R.string.task_lights_desc), Icons.Default.Lightbulb, AmarilloAdvertencia, "Eléctrico"),
    Tarea(stringResource(R.string.task_ecu), stringResource(R.string.task_ecu_desc), Icons.Default.Memory, Color(0xFFA3E635), "Eléctrico")
)

@Composable
fun obtenerTareasSuspension(): List<Tarea> = listOf(
    Tarea(stringResource(R.string.task_suspension_front), stringResource(R.string.task_suspension_front_desc), Icons.Default.ArrowDownward, Color(0xFFE87930), "Suspensión"),
    Tarea(stringResource(R.string.task_suspension_rear), stringResource(R.string.task_suspension_rear_desc), Icons.Default.ArrowUpward, Color(0xFFE87930), "Suspensión"),
    Tarea(stringResource(R.string.task_fork_seals_inspect), stringResource(R.string.task_fork_seals_inspect_desc), Icons.Default.Visibility, Color(0xFFE87930), "Suspensión"),
    Tarea(stringResource(R.string.task_fork_seals_replace), stringResource(R.string.task_fork_seals_replace_desc), Icons.Default.Build, Color(0xFFE87930), "Suspensión")
)

@Composable
fun obtenerNombresSeccion(): Map<String, String> = mapOf(
    "Motor" to stringResource(R.string.category_motor),
    "Transmisión" to stringResource(R.string.category_transmission),
    "Frenos" to stringResource(R.string.category_brakes),
    "Neumáticos" to stringResource(R.string.category_tires),
    "Eléctrico" to stringResource(R.string.category_electrical),
    "Suspensión" to stringResource(R.string.category_suspension)
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
