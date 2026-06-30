package com.example.torque.maintenance

import android.content.Intent
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.torque.utils.terminar
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.example.torque.R
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.components.ui.dashboard.FiltroChip
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.FotoMantenimientoEntity
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.ArchivoUtils
import com.example.torque.utils.DateUtils
import com.example.torque.utils.PdfExporter
import com.example.torque.utils.SaludMotoCalculator
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MantenimientoHistorial : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val idMoto = intent.getLongExtra("idMoto", -1L).takeIf { it != -1L }
        setContent {
            TorqueTheme {
                PantallaHistorialMantenimiento(idMoto)
            }
        }
    }
}

@Composable
fun PantallaHistorialMantenimiento(idMoto: Long? = null) {
    val contexto = LocalContext.current
    val viewModel: MantenimientoHistorialViewModel = viewModel()
    val historial by viewModel.historial.collectAsState()
    var filtroSeleccionado by remember { mutableStateOf("Todos") }
    var itemSeleccionado by remember { mutableStateOf<MantenimientoEntity?>(null) }
    var itemAEliminar by remember { mutableStateOf<MantenimientoEntity?>(null) }
    var fotosItemSeleccionado by remember { mutableStateOf<List<FotoMantenimientoEntity>>(emptyList()) }
    var exportandoPdf by remember { mutableStateOf(false) }

    val launcherPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let {
            viewModel.viewModelScope.launch {
                exportandoPdf = true
                val resultado = idMoto?.let { id -> viewModel.exportarPdf(contexto, it, id) }
                if (resultado?.isFailure == true) {
                    android.widget.Toast.makeText(contexto, "Error al exportar PDF: ${resultado.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_LONG).show()
                }
                exportandoPdf = false
            }
        }
    }

    LaunchedEffect(idMoto) { viewModel.cargarHistorial(idMoto) }

    val fotosPorMantenimiento by viewModel.fotosPorMantenimiento.collectAsState()

    val historialFiltrado = remember(historial, filtroSeleccionado) {
        if (filtroSeleccionado == "Todos") historial
        else historial.filter {
            it.nombreMantenimiento.contains(filtroSeleccionado, ignoreCase = true) ||
                    (it.notas?.contains(filtroSeleccionado, ignoreCase = true) ?: false) ||
                    (it.tareas?.contains(filtroSeleccionado, ignoreCase = true) ?: false)
        }
    }

    itemSeleccionado?.let { sel ->
        val fotos = fotosPorMantenimiento[sel.id] ?: emptyList()
        Dialog(onDismissRequest = { itemSeleccionado = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ColoresTorque.SurfaceDark)
            ) {
                DetalleMantenimientoEntityScreen(
                    mantenimiento = sel,
                    fotos = fotos,
                    onBack = { itemSeleccionado = null },
                    onAddFoto = { uri ->
                        viewModel.viewModelScope.launch {
                            viewModel.agregarFoto(sel.id, uri)
                        }
                    },
                    onAddFactura = { uri ->
                        viewModel.viewModelScope.launch {
                            viewModel.agregarFoto(sel.id, uri, "factura")
                        }
                    },
                    onDeleteFoto = { foto ->
                        viewModel.viewModelScope.launch {
                            viewModel.eliminarFoto(foto)
                        }
                    }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding().background(ColoresTorque.Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth().height(ColoresTorque.ALTURA_CABECERA).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { (contexto as? Activity)?.terminar() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back),
                        tint = ColoresTorque.Primary
                    )
                }
                Text(
                    text = stringResource(R.string.maintenance_history_title),
                    modifier = Modifier.weight(1f),
                    color = ColoresTorque.OnSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        if (!exportandoPdf && idMoto != null) {
                            launcherPdf.launch("historial_mantenimiento_${idMoto}.pdf")
                        }
                    },
                    enabled = !exportandoPdf
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = "Exportar PDF",
                        tint = if (exportandoPdf) ColoresTorque.OnSurfaceVariant else ColoresTorque.Primary
                    )
                }
            }

            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 140.dp)) {
                // Filtros
                item {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filtros = listOf("Todos", "Aceite", "Filtro", "Neumático", "Cadena", "Frenos", "Suspensión")
                        items(filtros) { filtro ->
                            FiltroChip(
                                texto = filtro,
                                estaSeleccionado = filtroSeleccionado == filtro,
                                onClick = { filtroSeleccionado = filtro },
                                colorSeleccionado = ColoresTorque.SecondaryContainer,
                                colorSeleccionadoContenido = ColoresTorque.OnSecondaryContainer,
                                colorNoSeleccionadoBorde = ColoresTorque.Outline.copy(alpha = 0.5f),
                                mostrarCheck = true
                            )
                        }
                    }
                }

                item {
                    Text(
                        stringResource(R.string.recent_activity),
                        color = ColoresTorque.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (historialFiltrado.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (historial.isEmpty()) stringResource(R.string.no_maintenance_records) else stringResource(R.string.no_results_for, filtroSeleccionado),
                                color = ColoresTorque.OnSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(historialFiltrado, key = { it.id }) { item ->
                        ItemLogHistorial(
                            item = item,
                            onClick = { itemSeleccionado = item },
                            onEliminar = { itemAEliminar = item }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = ColoresTorque.SurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val intent = Intent(contexto, RegistroMantenimiento::class.java).apply {
                    putExtra("idMoto", idMoto ?: -1L)
                }
                contexto.startActivity(intent)
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 110.dp),
            containerColor = ColoresTorque.PrimaryContainer,
            contentColor = ColoresTorque.OnPrimaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
        }

        itemAEliminar?.let { elim ->
            AlertDialog(
                onDismissRequest = { itemAEliminar = null },
                containerColor = ColoresTorque.SurfaceDark,
                title = { Text(stringResource(R.string.dialog_delete_record_title)) },
                text = { Text(stringResource(R.string.action_irreversible)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.eliminarMantenimiento(elim)
                        itemAEliminar = null
                    }) {
                        Text(stringResource(R.string.btn_delete), color = ColoresTorque.RojoError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemAEliminar = null }) {
                        Text(stringResource(R.string.btn_cancel), color = ColoresTorque.OnSurfaceVariant)
                    }
                }
            )
        }
    }
}


