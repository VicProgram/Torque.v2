package com.example.torque.maintenance

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.components.ui.dashboard.BotonTorque
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.components.ui.dashboard.CampoTextoTorque
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.TiposMantenimiento
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FormularioMantenimiento : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val tareasSeleccionadas = intent.getStringArrayListExtra("seleccionados") ?: arrayListOf()
        val idMoto = intent.getLongExtra("idMoto", -1L)
        setContent {
            TorqueTheme {
                VistaFormularioMantenimiento(tareasSeleccionadas, idMoto)
            }
        }
    }
}

private suspend fun guardarMantenimiento(
    tareasSeleccionadas: List<String>,
    idMoto: Long,
    kmNuevos: Int,
    intervaloKms: Int,
    precio: String,
    tiempo: String,
    notas: String,
    fechaActual: String,
    repositorio: RepositorioApp,
    contexto: android.content.Context,
    porcentajeVida: Int = 100
) {
    try {
        val tipoId = if (tareasSeleccionadas.size == 1)
            TiposMantenimiento.desdeNombre(tareasSeleccionadas[0])?.id ?: 0L
        else 0L

        val mantenimiento = MantenimientoEntity(
            idMoto = idMoto,
            nombreMantenimiento = if (tareasSeleccionadas.size == 1) tareasSeleccionadas[0] else "Mantenimiento General",
            fecha = fechaActual,
            kms = kmNuevos,
            intervaloKms = intervaloKms,
            precio = precio,
            tiempo = tiempo,
            notas = notas,
            tareas = tareasSeleccionadas.joinToString(", "),
            creadoEn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            tipoMantenimientoId = tipoId,
            porcentajeVida = porcentajeVida
        )
        repositorio.insertarMantenimiento(mantenimiento)
        withContext(Dispatchers.Main) {
            Toast.makeText(contexto, "¡Mantenimiento guardado!", Toast.LENGTH_SHORT).show()
            (contexto as? Activity)?.finish()
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(contexto, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun VistaFormularioMantenimiento(tareasSeleccionadas: List<String>, idMoto: Long) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }
    val scope = rememberCoroutineScope()

    var kms by remember { mutableStateOf("") }
    var intervalo by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var tiempo by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var errorKms by remember { mutableStateOf(false) }
    var mostrarDialogoDescartar by remember { mutableStateOf(false) }
    var mostrarDialogoActualizarKm by remember { mutableStateOf(false) }
    var kmMotoActual by remember { mutableStateOf(0) }

    val hayCambios = kms.isNotBlank() || intervalo.isNotBlank() || precio.isNotBlank() ||
        tiempo.isNotBlank() || notas.isNotBlank()

    val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    val esDesgaste = remember {
        tareasSeleccionadas.size == 1 &&
            TiposMantenimiento.desdeNombre(tareasSeleccionadas[0])?.esDesgaste == true
    }
    var porcentajeVida by remember { mutableStateOf(100) }

    val tipoDesgaste = remember {
        if (esDesgaste) TiposMantenimiento.desdeNombre(tareasSeleccionadas[0]) else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(ColoresTorque.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Cabecera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (hayCambios) mostrarDialogoDescartar = true
                    else (contexto as? Activity)?.finish()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = ColoresTorque.Primary
                    )
                }
                Text(
                    text = "Detalles del Mantenimiento",
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Tareas seleccionadas
                Card(
                    colors = CardDefaults.cardColors(containerColor = ColoresTorque.SurfaceDark),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Tareas", color = ColoresTorque.Slate500, fontSize = 12.sp)
                        Text(
                            tareasSeleccionadas.joinToString(" · "),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CampoTextoTorque(kms, { kms = it.filter { c -> c.isDigit() }; errorKms = false }, "Kms actuales *", modifier = Modifier.weight(1f))
                        CampoTextoTorque(intervalo, { intervalo = it.filter { c -> c.isDigit() } }, "Cada X kms", modifier = Modifier.weight(1f))
                    }
                    if (errorKms) {
                        Text("Los kilómetros son obligatorios", color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CampoTextoTorque(precio, { precio = it }, "Precio (€)", modifier = Modifier.weight(1f))
                    CampoTextoTorque(tiempo, { tiempo = it }, "Tiempo", modifier = Modifier.weight(1f))
                }

                CampoTextoTorque(
                    value = notas,
                    onValueChange = { notas = it },
                    label = "Notas",
                    singleLine = false,
                    modifier = Modifier.height(120.dp)
                )

                if (esDesgaste && tipoDesgaste != null) {
                    val colorBarra = when {
                        porcentajeVida >= 70 -> Color(0xFF22C55E)
                        porcentajeVida >= 40 -> Color(0xFFEAB308)
                        else -> Color(0xFFEF4444)
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ColoresTorque.SurfaceDark),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Vida útil restante",
                                color = ColoresTorque.Slate500,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    tipoDesgaste.nombre,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "$porcentajeVida%",
                                    color = colorBarra,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = porcentajeVida.toFloat(),
                                onValueChange = { porcentajeVida = it.toInt() },
                                valueRange = 0f..100f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = colorBarra,
                                    activeTrackColor = colorBarra,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        if (mostrarDialogoDescartar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoDescartar = false },
                containerColor = ColoresTorque.SurfaceDark,
                title = { Text("¿Descartar cambios?") },
                text = { Text("Los datos introducidos se perderán.") },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarDialogoDescartar = false
                        (contexto as? Activity)?.finish()
                    }) {
                        Text("Descartar", color = Color(0xFFEF4444))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoDescartar = false }) {
                        Text("Seguir editando", color = ColoresTorque.OnSurfaceVariant)
                    }
                }
            )
        }

        if (mostrarDialogoActualizarKm) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoActualizarKm = false },
                containerColor = ColoresTorque.SurfaceDark,
                title = { Text("¿Actualizar kilometraje?") },
                text = {
                    Text("El mantenimiento registra ${kms} km, pero la moto tiene $kmMotoActual km. ¿Actualizar el kilometraje de la moto?")
                },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarDialogoActualizarKm = false
                        scope.launch {
                            val kmNuevos = kms.toIntOrNull() ?: 0
                            repositorio.actualizarKilometros(idMoto, kmNuevos)
                            guardarMantenimiento(tareasSeleccionadas, idMoto, kmNuevos, intervalo.toIntOrNull() ?: 0, precio, tiempo, notas, fechaActual, repositorio, contexto, porcentajeVida)
                        }
                    }) {
                        Text("Sí, actualizar", color = ColoresTorque.Primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        mostrarDialogoActualizarKm = false
                        scope.launch {
                            val kmNuevos = kms.toIntOrNull() ?: 0
                            guardarMantenimiento(tareasSeleccionadas, idMoto, kmNuevos, intervalo.toIntOrNull() ?: 0, precio, tiempo, notas, fechaActual, repositorio, contexto, porcentajeVida)
                        }
                    }) {
                        Text("No", color = ColoresTorque.OnSurfaceVariant)
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(ColoresTorque.Background)
                .padding(24.dp)
        ) {
            BotonTorque(
                text = "Guardar Mantenimiento",
                icon = Icons.Default.Save,
                onClick = {
                    if (idMoto == -1L) {
                        Toast.makeText(contexto, "Error: No se identificó la moto", Toast.LENGTH_SHORT).show()
                        return@BotonTorque
                    }

                    errorKms = kms.isBlank()
                    if (errorKms) {
                        Toast.makeText(contexto, "Rellena los campos obligatorios", Toast.LENGTH_SHORT).show()
                        return@BotonTorque
                    }

                    scope.launch {
                        val kmNuevos = kms.toIntOrNull() ?: 0
                        val moto = repositorio.obtenerMotoPorId(idMoto)
                        if (moto != null && kmNuevos > moto.kilometros) {
                            kmMotoActual = moto.kilometros
                            mostrarDialogoActualizarKm = true
                            return@launch
                        }

                        guardarMantenimiento(tareasSeleccionadas, idMoto, kmNuevos, intervalo.toIntOrNull() ?: 0, precio, tiempo, notas, fechaActual, repositorio, contexto, porcentajeVida)
                    }
                }
            )
        }
    }
}