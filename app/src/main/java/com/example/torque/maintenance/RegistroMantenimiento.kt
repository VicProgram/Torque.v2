package com.example.torque.maintenance

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.torque.ui.theme.AmarilloAdvertencia
import com.example.torque.ui.theme.RojoError
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R
import com.example.torque.components.ui.dashboard.*
import com.example.torque.ui.theme.TorqueTheme

class RegistroMantenimiento : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val idMoto = intent.getLongExtra("idMoto", -1L)
        setContent {
            TorqueTheme {
                PantallaRegistroMantenimiento(idMoto)
            }
        }
    }
}

data class Tarea(
    val nombre: String,
    val descripcion: String,
    val icono: ImageVector,
    val colorIcono: Color,
    val categoria: String
)

@Composable
fun PantallaRegistroMantenimiento(idMoto: Long) {
    val contexto = LocalContext.current
    val itemsSeleccionados = remember { mutableStateMapOf<String, Boolean>() }
    var consultaBusqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todas") }

    val tareasMotor = listOf(
        Tarea("Cambio de Aceite Motor", "Recomendado cada 5,000 km", Icons.Default.OilBarrel, Color(0xFF3B82F6), "Motor"),
        Tarea("Filtro de Aceite", "Incluido en servicio estándar", Icons.Default.FilterAlt, Color(0xFFF97316), "Motor"),
        Tarea("Bujías", "Reemplazo cada 10,000 km", Icons.Default.FlashOn, Color(0xFFFACC15), "Motor"),
        Tarea("Filtro de Aire", "Limpieza o cambio cada 10,000 km", Icons.Default.Air, Color(0xFF60A5FA), "Motor"),
        Tarea("Líquido Refrigerante", "Cambio cada 20,000 km", Icons.Default.WaterDrop, Color(0xFF06B6D4), "Motor"),
        Tarea("Reglaje de Válvulas", "Cada 12,000 km", Icons.Default.Tune, Color(0xFF8B5CF6), "Motor")
    )

    val tareasTransmision = listOf(
        Tarea("Limpieza y Engrase de Cadena", "Mantenimiento regular cada 500 km", Icons.Default.SettingsInputComponent, Color(0xFF6366F1), "Transmisión"),
        Tarea("Tensado de Cadena", "Revisar cada 1,000 km", Icons.Default.Link, Color(0xFFA78BFA), "Transmisión"),
        Tarea("Cambio de Cadena y Coronas", "Cada 20,000 km", Icons.Default.Cyclone, Color(0xFF818CF8), "Transmisión")
    )

    val tareasFrenos = listOf(
        Tarea("Pastillas de Freno Delantero", "Revisar cada 5,000 km", Icons.Default.CarRepair, RojoError, "Frenos"),
        Tarea("Pastillas de Freno Trasero", "Revisar cada 5,000 km", Icons.Default.CarRepair, Color(0xFFDC2626), "Frenos"),
        Tarea("Líquido de Frenos", "Cambio cada 20,000 km", Icons.Default.Dangerous, Color(0xFFB91C1C), "Frenos"),
        Tarea("Discos de Freno", "Inspeccionar desgaste cada 10,000 km", Icons.Default.DiscFull, Color(0xFF991B1B), "Frenos")
    )

    val tareasNeumaticos = listOf(
        Tarea("Presión de Neumáticos", "Revisión semanal", Icons.Default.TireRepair, Color(0xFF10B981), "Neumáticos"),
        Tarea("Cambio de Neumáticos", "Cada 10,000 km o según desgaste", Icons.Default.Circle, Color(0xFF34D399), "Neumáticos")
    )

    val tareasElectrico = listOf(
        Tarea("Batería", "Revisar cada 6 meses", Icons.Default.BatteryStd, Color(0xFF84CC16), "Eléctrico"),
        Tarea("Luces y Señalización", "Revisión mensual", Icons.Default.Lightbulb, AmarilloAdvertencia, "Eléctrico"),
        Tarea("Centralita / ECU", "Diagnóstico anual", Icons.Default.Memory, Color(0xFFA3E635), "Eléctrico")
    )

    val tareasSuspension = listOf(
        Tarea("Suspensión Delantera", "Revisión cada 15,000 km", Icons.Default.ArrowDownward, Color(0xFFE87930), "Suspensión"),
        Tarea("Suspensión Trasera", "Revisión cada 15,000 km", Icons.Default.ArrowUpward, Color(0xFFE87930), "Suspensión"),
        Tarea("Revisión de Retenes Horquilla", "Revisión cada 5,000 km", Icons.Default.Visibility, Color(0xFFE87930), "Suspensión"),
        Tarea("Cambio de Retenes Horquilla", "Cambio cada 10,000 km", Icons.Default.Build, Color(0xFFE87930), "Suspensión")
    )



    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(ColoresTorque.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ColoresTorque.ALTURA_CABECERA)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { (contexto as? android.app.Activity)?.finish() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back),
                        tint = ColoresTorque.Primary
                    )
                }
                Text(
                    stringResource(R.string.register_maintenance_title),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 160.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextField(
                            value = consultaBusqueda,
                            onValueChange = { consultaBusqueda = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            placeholder = {
                                Text(
                                    stringResource(R.string.search_tasks_hint),
                                    color = ColoresTorque.Slate500
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = ColoresTorque.Slate500
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ColoresTorque.SurfaceDark,
                                unfocusedContainerColor = ColoresTorque.SurfaceDark,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = ColoresTorque.Primary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item { FiltroChip("Todas", estaSeleccionado = filtroSeleccionado == "Todas", onClick = { filtroSeleccionado = "Todas" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                            item { FiltroChip("Motor", estaSeleccionado = filtroSeleccionado == "Motor", onClick = { filtroSeleccionado = "Motor" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                            item { FiltroChip("Transmisión", estaSeleccionado = filtroSeleccionado == "Transmisión", onClick = { filtroSeleccionado = "Transmisión" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                            item { FiltroChip("Frenos", estaSeleccionado = filtroSeleccionado == "Frenos", onClick = { filtroSeleccionado = "Frenos" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                            item { FiltroChip("Ruedas", estaSeleccionado = filtroSeleccionado == "Ruedas", onClick = { filtroSeleccionado = "Ruedas" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                        }
                    }
                }

                val secciones = listOf(
                    "Motor" to tareasMotor,
                    "Transmisión" to tareasTransmision,
                    "Frenos" to tareasFrenos,
                    "Neumáticos" to tareasNeumaticos,
                    "Eléctrico" to tareasElectrico,
                    "Suspensión" to tareasSuspension
                )
                item {
                    val todasLasTareas = tareasMotor + tareasTransmision + tareasFrenos +
                        tareasNeumaticos + tareasElectrico + tareasSuspension
                    val tareasFiltradas = if (consultaBusqueda.isBlank()) todasLasTareas
                        else todasLasTareas.filter { it.nombre.contains(consultaBusqueda, ignoreCase = true) }
                    if (tareasFiltradas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.no_tasks_for, consultaBusqueda),
                                color = ColoresTorque.OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        return@item
                    }
                }
                secciones.forEach { (nombre, lista) ->
                    val listaFiltrada = if (consultaBusqueda.isBlank()) lista
                        else lista.filter { it.nombre.contains(consultaBusqueda, ignoreCase = true) }
                    if (listaFiltrada.isEmpty()) return@forEach
                    item { EncabezadoDeSeccion(nombre) }
                    items(listaFiltrada) { tarea ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            ElementoCheckMotoKeep(
                                title = tarea.nombre,
                                description = tarea.descripcion,
                                icon = tarea.icono,
                                iconColor = tarea.colorIcono,
                                isChecked = itemsSeleccionados[tarea.nombre] == true,
                                onCheckedChange = { itemsSeleccionados[tarea.nombre] = it }
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ColoresTorque.Background.copy(alpha = 0.95f),
                            ColoresTorque.Background
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColoresTorque.SurfaceDark)
                    .border(1.dp, ColoresTorque.BorderDark, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val cantidadSeleccionada = itemsSeleccionados.values.count { it }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.tasks_selected, cantidadSeleccionada),
                        color = ColoresTorque.Slate500,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        stringResource(R.string.estimated_45_min),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                BotonTorque(
                    text = stringResource(R.string.btn_continue),
                    color = ColoresTorque.Accent,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = {
                        val listaSeleccionados = itemsSeleccionados.filterValues { it }.keys.toList()
                        if (listaSeleccionados.isNotEmpty()) {
                            val intent = Intent(contexto, FormularioMantenimiento::class.java).apply {
                                putStringArrayListExtra("seleccionados", ArrayList(listaSeleccionados))
                                putExtra("idMoto", idMoto)
                            }
                            contexto.startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}

