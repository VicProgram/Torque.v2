package com.example.torque.maintenance

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.example.torque.base.BaseActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.torque.R
import com.example.torque.components.ui.dashboard.BotonTorque
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.components.ui.dashboard.CampoTextoTorque
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.FotoMantenimientoEntity
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.ArchivoUtils
import com.example.torque.utils.DateUtils
import com.example.torque.utils.TiposMantenimiento
import com.example.torque.utils.terminar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FormularioMantenimiento : BaseActivity() {
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

private fun crearEntidadMantenimiento(
    tareasSeleccionadas: List<String>,
    idMoto: Long,
    kmNuevos: Int,
    intervaloKms: Int,
    precio: String,
    tiempo: String,
    notas: String,
    fechaActual: String,
    contexto: android.content.Context,
    porcentajeVida: Int
): MantenimientoEntity {
    val tipoId = if (tareasSeleccionadas.size == 1)
        TiposMantenimiento.desdeNombre(tareasSeleccionadas[0])?.id ?: 0L
    else 0L
    return MantenimientoEntity(
        idMoto = idMoto,
        nombreMantenimiento = if (tareasSeleccionadas.size == 1) tareasSeleccionadas[0] else contexto.getString(R.string.general_maintenance),
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
}

private suspend fun procesarAdjuntos(adjuntos: List<Adjunto>, idMantenimiento: Long, repositorio: RepositorioApp, contexto: android.content.Context) {
    for (adjunto in adjuntos) {
        val ruta = adjunto.rutaInterna ?: ArchivoUtils.copiarImagenAInterno(contexto, adjunto.uri)
        if (ruta != null) {
            repositorio.insertarFotoMantenimiento(
                FotoMantenimientoEntity(
                    idMantenimiento = idMantenimiento,
                    ruta = ruta,
                    tipo = adjunto.tipo,
                    creadoEn = SimpleDateFormat(DateUtils.FORMATO_FECHA_COMPLETA, Locale.getDefault()).format(Date())
                )
            )
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
    porcentajeVida: Int = 100,
    adjuntos: List<Adjunto> = emptyList(),
    actualizarKmMoto: Boolean = false
) {
    try {
        if (actualizarKmMoto) {
            repositorio.actualizarKilometros(idMoto, kmNuevos)
        }
        val idMantenimiento = repositorio.insertarMantenimiento(
            crearEntidadMantenimiento(tareasSeleccionadas, idMoto, kmNuevos, intervaloKms, precio, tiempo, notas, fechaActual, contexto, porcentajeVida)
        )
        if (idMantenimiento > 0L) {
            procesarAdjuntos(adjuntos, idMantenimiento, repositorio, contexto)
        }
        Toast.makeText(contexto, contexto.getString(R.string.toast_maintenance_saved), Toast.LENGTH_SHORT).show()
        (contexto as? Activity)?.terminar()
    } catch (e: Exception) {
        Toast.makeText(contexto, contexto.getString(R.string.toast_save_error, e.message), Toast.LENGTH_LONG).show()
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
    var adjuntos by remember { mutableStateOf<List<Adjunto>>(emptyList()) }

    val hayCambios = kms.isNotBlank() || intervalo.isNotBlank() || precio.isNotBlank() ||
        tiempo.isNotBlank() || notas.isNotBlank() || adjuntos.isNotEmpty()

    val fechaActual = SimpleDateFormat(DateUtils.FORMATO_FECHA_VISUAL, Locale.getDefault()).format(Date())

    val esDesgaste = remember {
        tareasSeleccionadas.size == 1 &&
            TiposMantenimiento.desdeNombre(tareasSeleccionadas[0])?.esDesgaste == true
    }

    val tipoDesgaste = remember {
        if (esDesgaste) TiposMantenimiento.desdeNombre(tareasSeleccionadas[0]) else null
    }

    val launcherFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { adjuntos = adjuntos + Adjunto(it, "foto") }
    }

    val launcherFactura = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { adjuntos = adjuntos + Adjunto(it, "factura") }
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
                errorKms, esDesgaste, tipoDesgaste, porcentajeVida, adjuntos,
                modifier = Modifier.weight(1f),
                onKmsChange = { kms = it.filter { c -> c.isDigit() }; errorKms = false },
                onIntervaloChange = { intervalo = it.filter { c -> c.isDigit() } },
                onPrecioChange = { precio = it },
                onTiempoChange = { tiempo = it },
                onNotasChange = { notas = it },
                onPorcentajeVidaChange = { porcentajeVida = it },
                onAddFoto = { launcherFoto.launch("image/*") },
                onAddFactura = { launcherFactura.launch("image/*") },
                onRemoveAdjunto = { adjuntos = adjuntos - it }
            )
        }

        DialogoDescartar(mostrarDialogoDescartar, contexto) { mostrarDialogoDescartar = false }
        DialogoActualizarKm(
            mostrar = mostrarDialogoActualizarKm,
            kms = kms,
            kmMotoActual = kmMotoActual,
            onConfirm = { actualizarKm ->
                mostrarDialogoActualizarKm = false
                scope.launch {
                    val kmNuevos = kms.toIntOrNull() ?: 0
                    guardarMantenimiento(tareasSeleccionadas, idMoto, kmNuevos, intervalo.toIntOrNull() ?: 0, precio, tiempo, notas, fechaActual, repositorio, contexto, porcentajeVida, adjuntos, actualizarKmMoto = actualizarKm)
                }
            }
        )
        BotonGuardarFormulario(idMoto, kms, contexto, repositorio) {
            errorKms = it.errorKms
            kmMotoActual = it.kmMotoActual
            mostrarDialogoActualizarKm = it.mostrarActualizarKm
            if (it.guardarDirecto) {
                scope.launch {
                    guardarMantenimiento(tareasSeleccionadas, idMoto, kms.toIntOrNull() ?: 0, intervalo.toIntOrNull() ?: 0, precio, tiempo, notas, fechaActual, repositorio, contexto, porcentajeVida, adjuntos)
                }
            }
        }
    }
}


