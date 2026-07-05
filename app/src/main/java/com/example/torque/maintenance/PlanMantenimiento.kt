package com.example.torque.maintenance

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.widget.Toast
import com.example.torque.base.BaseActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.torque.ui.dashboard.ColoresTorque
import com.example.torque.ui.dashboard.EncabezadoDeSeccion
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.room.entity.MotoEntity
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.TipoMantenimiento
import com.example.torque.utils.TiposMantenimiento
import com.example.torque.utils.TipoVehiculo
import com.example.torque.utils.terminar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TareaPlan(
    val tipo: TipoMantenimiento,
    val ultimoRegistro: MantenimientoEntity?,
    val cumplimiento: Int,
    val proximoKms: Int?,
    val estado: EstadoTarea
)

enum class EstadoTarea { OK, PROXIMO, VENCIDO, SIN_DATOS }

class PlanMantenimientoViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = RepositorioApp.obtenerInstancia(application)

    private val _moto = MutableStateFlow<MotoEntity?>(null)
    val moto: StateFlow<MotoEntity?> = _moto

    private val _tareas = MutableStateFlow<List<TareaPlan>>(emptyList())
    val tareas: StateFlow<List<TareaPlan>> = _tareas

    private fun calcularCumplimiento(ultimo: MantenimientoEntity, tipo: TipoMantenimiento, kmActuales: Int, format: SimpleDateFormat): Int {
        if (tipo.esDesgaste) return ultimo.porcentajeVida
        var cumpl = 100
        if (tipo.intervaloKms > 0) {
            val avanzado = kmActuales - ultimo.kms
            cumpl = ((1f - avanzado.toFloat() / tipo.intervaloKms) * 100).toInt().coerceIn(0, 100)
        }
        if (tipo.intervaloDias > 0) {
            try {
                val fechaUltimo = format.parse(ultimo.fecha)
                val dias = fechaUltimo?.let { (Date().time - it.time) / (1000 * 60 * 60 * 24) } ?: 0
                val cumplDias = ((1f - dias.toFloat() / tipo.intervaloDias) * 100).toInt().coerceIn(0, 100)
                cumpl = minOf(cumpl, cumplDias)
            } catch (_: Exception) {}
        }
        return cumpl
    }

    fun cargar(idMoto: Long) {
        viewModelScope.launch {
            _moto.value = repositorio.obtenerMotoPorId(idMoto)
            val mantenimientos = repositorio.obtenerTodosMantenimientosPorMoto(idMoto)
            val kmActuales = _moto.value?.kilometros ?: 0
            val agrupados = mantenimientos
                .filter { it.tipoMantenimientoId > 0L }
                .groupBy { it.tipoMantenimientoId }

            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val tiposFiltrados = TiposMantenimiento.obtenerPorTipo(_moto.value?.tipoVehiculo ?: TipoVehiculo.MOTO)

            _tareas.value = tiposFiltrados.map { tipo ->
                val ultimo = agrupados[tipo.id]?.maxByOrNull { it.kms }
                val cumplimiento = if (ultimo != null) {
                    calcularCumplimiento(ultimo, tipo, kmActuales, format)
                } else 100
                val proximoKms = if (ultimo != null && tipo.intervaloKms > 0) {
                    ultimo.kms + tipo.intervaloKms
                } else null
                val estado = when {
                    ultimo == null -> EstadoTarea.SIN_DATOS
                    cumplimiento <= 0 -> EstadoTarea.VENCIDO
                    cumplimiento <= 20 -> EstadoTarea.PROXIMO
                    else -> EstadoTarea.OK
                }
                TareaPlan(tipo, ultimo, cumplimiento, proximoKms, estado)
            }.sortedBy { it.estado.ordinal }
        }
    }
}

class PlanMantenimiento : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val idMoto = intent.getLongExtra("idMoto", -1L)
        setContent {
            TorqueTheme {
                PantallaPlanMantenimiento(idMoto)
            }
        }
    }
}

@Composable
fun PantallaPlanMantenimiento(idMoto: Long) {
    val contexto = LocalContext.current
    val viewModel: PlanMantenimientoViewModel = viewModel()
    val moto by viewModel.moto.collectAsState()
    val tareas by viewModel.tareas.collectAsState()

    LaunchedEffect(idMoto) { viewModel.cargar(idMoto) }

    val agrupadas = remember(tareas) {
        tareas.groupBy { it.tipo.categoria }
    }

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
                IconButton(onClick = { (contexto as? Activity)?.terminar() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(com.example.torque.R.string.content_desc_back),
                        tint = ColoresTorque.Primary
                    )
                }
                Text(
                    text = stringResource(com.example.torque.R.string.plan_maintenance_title),
                    modifier = Modifier.weight(1f),
                    color = ColoresTorque.OnSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            moto?.let { m ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ColoresTorque.SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Motorcycle, contentDescription = null, tint = ColoresTorque.Primary)
                        Column {
                            Text("${m.marca} ${m.modelo}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${m.kilometros} km", color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                agrupadas.forEach { (categoria, lista) ->
                    item { EncabezadoDeSeccion(categoria) }
                    items(lista) { tarea ->
                        ItemTareaPlan(tarea)
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Leyenda(Color(0xFF4CAF50), stringResource(com.example.torque.R.string.status_ok))
                        Leyenda(ColoresTorque.AmarilloAdvertencia, stringResource(com.example.torque.R.string.status_soon))
                        Leyenda(ColoresTorque.RojoError, stringResource(com.example.torque.R.string.status_expired))
                        Leyenda(ColoresTorque.OnSurfaceVariant, stringResource(com.example.torque.R.string.status_no_data))
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ItemTareaPlan(tarea: TareaPlan) {
    val colorEstado = when (tarea.estado) {
        EstadoTarea.OK -> Color(0xFF4CAF50)
        EstadoTarea.PROXIMO -> ColoresTorque.AmarilloAdvertencia
        EstadoTarea.VENCIDO -> ColoresTorque.RojoError
        EstadoTarea.SIN_DATOS -> ColoresTorque.OnSurfaceVariant
    }

    val textoEstado = when (tarea.estado) {
        EstadoTarea.OK -> stringResource(com.example.torque.R.string.status_ok)
        EstadoTarea.PROXIMO -> stringResource(com.example.torque.R.string.status_soon)
        EstadoTarea.VENCIDO -> stringResource(com.example.torque.R.string.status_expired)
        EstadoTarea.SIN_DATOS -> stringResource(com.example.torque.R.string.pdf_dash)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ColoresTorque.SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(colorEstado)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tarea.tipo.nombre,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                val ultimoStr = if (tarea.ultimoRegistro != null) {
                    stringResource(com.example.torque.R.string.plan_last_record, tarea.ultimoRegistro.kms, tarea.ultimoRegistro.fecha)
                } else {
                    stringResource(com.example.torque.R.string.plan_no_record)
                }
                val intervaloStr = if (tarea.tipo.intervaloKms > 0) {
                    " " + stringResource(com.example.torque.R.string.plan_interval, tarea.tipo.intervaloKms)
                } else ""
                val detalle = ultimoStr + intervaloStr
                Text(detalle, color = ColoresTorque.OnSurfaceVariant, fontSize = 11.sp)

                if (tarea.proximoKms != null) {
                    Text(
                        stringResource(com.example.torque.R.string.plan_next_at, tarea.proximoKms),
                        color = ColoresTorque.Outline,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${tarea.cumplimiento}%",
                    color = colorEstado,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    textoEstado,
                    color = colorEstado,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(texto, color = ColoresTorque.OnSurfaceVariant, fontSize = 10.sp)
    }
}
