package com.example.torque.maintenance

import android.content.Intent
import android.os.Bundle
import com.example.torque.base.BaseActivity
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R
import com.example.torque.components.ui.dashboard.*
import com.example.torque.room.RepositorioApp
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.TipoVehiculo

class RegistroMantenimiento : BaseActivity() {
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

@Composable
fun PantallaRegistroMantenimiento(idMoto: Long) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }
    val itemsSeleccionados = remember { mutableStateMapOf<String, Boolean>() }
    var consultaBusqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todas") }
    var tipoVehiculo by remember { mutableStateOf(TipoVehiculo.MOTO) }

    LaunchedEffect(idMoto) {
        repositorio.obtenerMotoPorId(idMoto)?.let { tipoVehiculo = it.tipoVehiculo }
    }

    val tareasMotor = obtenerTareasMotor(tipoVehiculo)
    val tareasTransmision = obtenerTareasTransmision(tipoVehiculo)
    val tareasFrenos = obtenerTareasFrenos(tipoVehiculo)
    val tareasNeumaticos = obtenerTareasNeumaticos(tipoVehiculo)
    val tareasElectrico = obtenerTareasElectrico(tipoVehiculo)
    val tareasSuspension = obtenerTareasSuspension(tipoVehiculo)
    val tareasClimatizacion = if (tipoVehiculo == TipoVehiculo.COCHE) obtenerTareasClimatizacion() else emptyList()
    val tareasCombustible = if (tipoVehiculo == TipoVehiculo.COCHE) obtenerTareasCombustible() else emptyList()
    val tareasDireccion = if (tipoVehiculo == TipoVehiculo.COCHE) obtenerTareasDireccion() else emptyList()
    val nombresSeccion = obtenerNombresSeccion()

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
                            item { FiltroChip(stringResource(R.string.category_all), estaSeleccionado = filtroSeleccionado == "Todas", onClick = { filtroSeleccionado = "Todas" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                            item { FiltroChip(stringResource(R.string.category_motor), estaSeleccionado = filtroSeleccionado == "Motor", onClick = { filtroSeleccionado = "Motor" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                            item { FiltroChip(stringResource(R.string.category_transmission), estaSeleccionado = filtroSeleccionado == "Transmisión", onClick = { filtroSeleccionado = "Transmisión" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                            item { FiltroChip(stringResource(R.string.category_brakes), estaSeleccionado = filtroSeleccionado == "Frenos", onClick = { filtroSeleccionado = "Frenos" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                            item { FiltroChip(stringResource(R.string.category_wheels), estaSeleccionado = filtroSeleccionado == "Ruedas", onClick = { filtroSeleccionado = "Ruedas" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                            if (tipoVehiculo == TipoVehiculo.COCHE) {
                                item { FiltroChip("Climatización", estaSeleccionado = filtroSeleccionado == "Climatización", onClick = { filtroSeleccionado = "Climatización" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                                item { FiltroChip("Combustible", estaSeleccionado = filtroSeleccionado == "Combustible", onClick = { filtroSeleccionado = "Combustible" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                                item { FiltroChip("Dirección", estaSeleccionado = filtroSeleccionado == "Dirección", onClick = { filtroSeleccionado = "Dirección" }, forma = CircleShape, colorSeleccionado = ColoresTorque.Primary, colorSeleccionadoContenido = Color.White, colorNoSeleccionadoFondo = ColoresTorque.SurfaceDark, colorNoSeleccionadoBorde = ColoresTorque.BorderDark) }
                            }
                        }
                    }
                }

                val todasLasSecciones = mutableListOf(
                    "Motor" to tareasMotor,
                    "Transmisión" to tareasTransmision,
                    "Frenos" to tareasFrenos,
                    "Neumáticos" to tareasNeumaticos,
                    "Eléctrico" to tareasElectrico,
                    "Suspensión" to tareasSuspension
                )
                if (tipoVehiculo == TipoVehiculo.COCHE) {
                    todasLasSecciones.add("Climatización" to tareasClimatizacion)
                    todasLasSecciones.add("Combustible" to tareasCombustible)
                    todasLasSecciones.add("Dirección" to tareasDireccion)
                }
                val secciones = todasLasSecciones.toList()
                item {
                    val todasLasTareas = tareasMotor + tareasTransmision + tareasFrenos +
                        tareasNeumaticos + tareasElectrico + tareasSuspension +
                        tareasClimatizacion + tareasCombustible + tareasDireccion
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
                    item { EncabezadoDeSeccion(nombresSeccion[nombre] ?: nombre) }
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

