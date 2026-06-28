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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R
import com.example.torque.components.ui.dashboard.BotonTorque
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.components.ui.dashboard.CampoTextoTorque
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.DateUtils
import com.example.torque.utils.TiposMantenimiento
import com.example.torque.utils.terminar
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
            creadoEn = SimpleDateFormat(DateUtils.FORMATO_FECHA_COMPLETA, Locale.getDefault()).format(Date()),
            tipoMantenimientoId = tipoId,
            porcentajeVida = porcentajeVida
        )
        repositorio.insertarMantenimiento(mantenimiento)
        withContext(Dispatchers.Main) {
            Toast.makeText(contexto, contexto.getString(R.string.toast_maintenance_saved), Toast.LENGTH_SHORT).show()
            (contexto as? Activity)?.terminar()
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
    var porcentajeVida by remember { mutableStateOf(100) }

    val hayCambios = kms.isNotBlank() || intervalo.isNotBlank() || precio.isNotBlank() ||
        tiempo.isNotBlank() || notas.isNotBlank()

    val fechaActual = SimpleDateFormat(DateUtils.FORMATO_FECHA_VISUAL, Locale.getDefault()).format(Date())

    val esDesgaste = remember {
        tareasSeleccionadas.size == 1 &&
            TiposMantenimiento.desdeNombre(tareasSeleccionadas[0])?.esDesgaste == true
    }

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
            CabeceraFormulario(hayCambios, contexto) { mostrarDialogoDescartar = it }

            FormularioCampos(
                tareasSeleccionadas, kms, intervalo, precio, tiempo, notas,
                errorKms, esDesgaste, tipoDesgaste, porcentajeVida,
                modifier = Modifier.weight(1f),
                onKmsChange = { kms = it.filter { c -> c.isDigit() }; errorKms = false },
                onIntervaloChange = { intervalo = it.filter { c -> c.isDigit() } },
                onPrecioChange = { precio = it },
                onTiempoChange = { tiempo = it },
                onNotasChange = { notas = it },
                onPorcentajeVidaChange = { porcentajeVida = it }
            )
        }

        DialogoDescartar(mostrarDialogoDescartar, contexto) { mostrarDialogoDescartar = false }
        DialogoActualizarKm(mostrarDialogoActualizarKm, kms, kmMotoActual) {
            mostrarDialogoActualizarKm = false
            scope.launch {
                val kmNuevos = kms.toIntOrNull() ?: 0
                guardarMantenimiento(tareasSeleccionadas, idMoto, kmNuevos, intervalo.toIntOrNull() ?: 0, precio, tiempo, notas, fechaActual, repositorio, contexto, porcentajeVida)
            }
        }
        BotonGuardarFormulario(idMoto, kms, contexto, repositorio) {
            errorKms = it.errorKms
            kmMotoActual = it.kmMotoActual
            mostrarDialogoActualizarKm = it.mostrarActualizarKm
            if (it.guardarDirecto) {
                scope.launch {
                    guardarMantenimiento(tareasSeleccionadas, idMoto, kms.toIntOrNull() ?: 0, intervalo.toIntOrNull() ?: 0, precio, tiempo, notas, fechaActual, repositorio, contexto, porcentajeVida)
                }
            }
        }
    }
}

@Composable
private fun CabeceraFormulario(hayCambios: Boolean, contexto: android.content.Context, onDescartar: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            if (hayCambios) onDescartar(true)
            else (contexto as? Activity)?.terminar()
        }) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = ColoresTorque.Primary
            )
        }
        Text(
            text = stringResource(R.string.maintenance_details_title),
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun FormularioCampos(
    tareasSeleccionadas: List<String>,
    kms: String, intervalo: String, precio: String, tiempo: String, notas: String,
    errorKms: Boolean, esDesgaste: Boolean, tipoDesgaste: com.example.torque.utils.TipoMantenimiento?,
    porcentajeVida: Int,
    modifier: Modifier = Modifier,
    onKmsChange: (String) -> Unit, onIntervaloChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit, onTiempoChange: (String) -> Unit,
    onNotasChange: (String) -> Unit, onPorcentajeVidaChange: (Int) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = ColoresTorque.SurfaceDark),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.label_tasks), color = ColoresTorque.Slate500, fontSize = 12.sp)
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
                CampoTextoTorque(kms, { onKmsChange(it) }, stringResource(R.string.field_current_kms), modifier = Modifier.weight(1f))
                CampoTextoTorque(intervalo, { onIntervaloChange(it) }, stringResource(R.string.field_every_x_kms), modifier = Modifier.weight(1f))
            }
            if (errorKms) {
                Text(stringResource(R.string.error_kms_required), color = ColoresTorque.RojoError, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CampoTextoTorque(precio, { onPrecioChange(it) }, stringResource(R.string.field_price), modifier = Modifier.weight(1f))
            CampoTextoTorque(tiempo, { onTiempoChange(it) }, stringResource(R.string.field_time), modifier = Modifier.weight(1f))
        }

        CampoTextoTorque(
            value = notas,
            onValueChange = { onNotasChange(it) },
            label = stringResource(R.string.field_notes),
            singleLine = false,
            modifier = Modifier.height(120.dp)
        )

        if (esDesgaste && tipoDesgaste != null) {
            SeccionDesgaste(tipoDesgaste, porcentajeVida, onPorcentajeVidaChange)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SeccionDesgaste(
    tipoDesgaste: com.example.torque.utils.TipoMantenimiento,
    porcentajeVida: Int,
    onPorcentajeVidaChange: (Int) -> Unit
) {
    val colorBarra = when {
        porcentajeVida >= 70 -> ColoresTorque.VerdeExito
        porcentajeVida >= 40 -> ColoresTorque.AmarilloAdvertencia
        else -> ColoresTorque.RojoError
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = ColoresTorque.SurfaceDark),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(stringResource(R.string.label_remaining_life), color = ColoresTorque.Slate500, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(tipoDesgaste.nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("$porcentajeVida%", color = colorBarra, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = porcentajeVida.toFloat(),
                onValueChange = { onPorcentajeVidaChange(it.toInt()) },
                valueRange = 0f..100f, steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = colorBarra, activeTrackColor = colorBarra,
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
private fun DialogoDescartar(
    mostrar: Boolean, contexto: android.content.Context, onDismiss: () -> Unit
) {
    if (mostrar) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = ColoresTorque.SurfaceDark,
            title = { Text(stringResource(R.string.dialog_discard_title)) },
            text = { Text(stringResource(R.string.dialog_discard_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onDismiss()
                    (contexto as? Activity)?.terminar()
                }) {
                    Text(stringResource(R.string.btn_discard), color = ColoresTorque.RojoError)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_keep_editing), color = ColoresTorque.OnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun DialogoActualizarKm(
    mostrar: Boolean, kms: String, kmMotoActual: Int,
    onConfirm: () -> Unit
) {
    if (mostrar) {
        AlertDialog(
            onDismissRequest = onConfirm,
            containerColor = ColoresTorque.SurfaceDark,
            title = { Text(stringResource(R.string.dialog_update_km_title)) },
            text = { Text(stringResource(R.string.dialog_update_km_message, kms, kmMotoActual)) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.btn_yes_update), color = ColoresTorque.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.btn_no), color = ColoresTorque.OnSurfaceVariant)
                }
            }
        )
    }
}

private data class EstadoGuardado(val errorKms: Boolean = false, val kmMotoActual: Int = 0, val mostrarActualizarKm: Boolean = false, val guardarDirecto: Boolean = false)

@Composable
private fun BotonGuardarFormulario(
    idMoto: Long, kms: String,
    contexto: android.content.Context,
    repositorio: RepositorioApp,
    onResult: (EstadoGuardado) -> Unit
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColoresTorque.Background)
            .padding(24.dp)
    ) {
        BotonTorque(
            text = stringResource(R.string.btn_save_maintenance),
            icon = Icons.Default.Save,
            onClick = {
                if (idMoto == -1L) {
                    Toast.makeText(contexto, "Error: No se identificó la moto", Toast.LENGTH_SHORT).show()
                    return@BotonTorque
                }
                if (kms.isBlank()) {
                    onResult(EstadoGuardado(errorKms = true))
                    Toast.makeText(contexto, contexto.getString(R.string.toast_fill_required), Toast.LENGTH_SHORT).show()
                    return@BotonTorque
                }
                scope.launch {
                    val kmNuevos = kms.toIntOrNull() ?: 0
                    val moto = repositorio.obtenerMotoPorId(idMoto)
                    if (moto != null && kmNuevos > moto.kilometros) {
                        onResult(EstadoGuardado(kmMotoActual = moto.kilometros, mostrarActualizarKm = true))
                        return@launch
                    }
                    onResult(EstadoGuardado(guardarDirecto = true))
                }
            }
        )
    }
}
