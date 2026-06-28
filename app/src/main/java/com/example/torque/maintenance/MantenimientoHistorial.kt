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

import com.example.torque.R
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.components.ui.dashboard.FiltroChip
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.ui.theme.TorqueTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MantenimientoHistorialViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = RepositorioApp.obtenerInstancia(application)

    private val _historial = MutableStateFlow<List<MantenimientoEntity>>(emptyList())
    val historial: StateFlow<List<MantenimientoEntity>> = _historial

    fun cargarHistorial(idMoto: Long? = null) {
        viewModelScope.launch {
            if (idMoto != null) {
                repositorio.obtenerMantenimientosPorMoto(idMoto).collect {
                    _historial.value = it
                }
            } else {
                _historial.value = emptyList()
            }
        }
    }

    fun eliminarMantenimiento(mantenimiento: MantenimientoEntity) {
        viewModelScope.launch {
            repositorio.eliminarMantenimiento(mantenimiento)
        }
    }
}

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

    LaunchedEffect(idMoto) { viewModel.cargarHistorial(idMoto) }

    val historialFiltrado = remember(historial, filtroSeleccionado) {
        if (filtroSeleccionado == "Todos") historial
        else historial.filter {
            it.nombreMantenimiento.contains(filtroSeleccionado, ignoreCase = true) ||
                    (it.notas?.contains(filtroSeleccionado, ignoreCase = true) ?: false) ||
                    (it.tareas?.contains(filtroSeleccionado, ignoreCase = true) ?: false)
        }
    }

    if (itemSeleccionado != null) {
        Dialog(onDismissRequest = { itemSeleccionado = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ColoresTorque.SurfaceDark)
            ) {
                // Adaptar DetalleMantenimientoScreen para usar MantenimientoEntity
                DetalleMantenimientoEntityScreen(
                    mantenimiento = itemSeleccionado!!,
                    onBack = { itemSeleccionado = null }
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

        if (itemAEliminar != null) {
            AlertDialog(
                onDismissRequest = { itemAEliminar = null },
                containerColor = ColoresTorque.SurfaceDark,
                title = { Text(stringResource(R.string.dialog_delete_record_title)) },
                text = { Text(stringResource(R.string.action_irreversible)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.eliminarMantenimiento(itemAEliminar!!)
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

@Composable
fun ItemLogHistorial(item: MantenimientoEntity, onClick: () -> Unit, onEliminar: () -> Unit = {}) {
    val icono = when {
        item.nombreMantenimiento.contains("aceite", ignoreCase = true) || 
        (item.tareas?.contains("aceite", ignoreCase = true) ?: false) -> Icons.Default.OilBarrel
        item.nombreMantenimiento.contains("neumático", ignoreCase = true) || 
        item.nombreMantenimiento.contains("rueda", ignoreCase = true) ||
        (item.tareas?.contains("neumático", ignoreCase = true) ?: false) -> Icons.Default.TireRepair
        item.nombreMantenimiento.contains("cadena", ignoreCase = true) || 
        item.nombreMantenimiento.contains("kit", ignoreCase = true) ||
        (item.tareas?.contains("cadena", ignoreCase = true) ?: false) -> Icons.Default.SettingsSuggest
        item.nombreMantenimiento.contains("luz", ignoreCase = true) ||
        item.nombreMantenimiento.contains("eléctrico", ignoreCase = true) ||
        item.nombreMantenimiento.contains("batería", ignoreCase = true) ||
        (item.tareas?.contains("eléctrico", ignoreCase = true) ?: false) -> Icons.Default.Bolt
        item.nombreMantenimiento.contains("freno", ignoreCase = true) ||
        (item.tareas?.contains("freno", ignoreCase = true) ?: false) -> Icons.Default.BackHand
        item.nombreMantenimiento.contains("retenes", ignoreCase = true) ||
        item.nombreMantenimiento.contains("horquilla", ignoreCase = true) ||
        (item.tareas?.contains("suspensión", ignoreCase = true) ?: false) -> Icons.Default.Construction
        else -> Icons.Default.Build
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(ColoresTorque.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(item.nombreMantenimiento, color = ColoresTorque.OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text("${item.fecha} · ${item.precio ?: stringResource(R.string.no_price)}", color = ColoresTorque.OnSurfaceVariant, fontSize = 13.sp)
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(20.dp))
            Text("${item.kms} km", color = ColoresTorque.Outline, fontSize = 11.sp)
        }
        IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.content_desc_delete), tint = ColoresTorque.RojoError.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun DetalleMantenimientoEntityScreen(
    mantenimiento: MantenimientoEntity,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.maintenance_detail_title),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.content_desc_close), tint = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = mantenimiento.nombreMantenimiento, color = ColoresTorque.Primary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = "${mantenimiento.fecha} · ${mantenimiento.kms} kms", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        
        val notas = mantenimiento.notas
        if (!notas.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.section_notes), color = ColoresTorque.Outline, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = notas, color = Color.White, fontSize = 16.sp)
        }

        val tareas = mantenimiento.tareas
        if (!tareas.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.section_tasks_done), color = ColoresTorque.Outline, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = tareas, color = Color.White, fontSize = 16.sp)
        }

        val precio = mantenimiento.precio
        if (!precio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.section_cost), color = ColoresTorque.Outline, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = precio, color = Color(0xFF81C784), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
