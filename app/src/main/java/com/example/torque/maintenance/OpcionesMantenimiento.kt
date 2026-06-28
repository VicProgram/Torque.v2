package com.example.torque.maintenance

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.MotoEntity
import com.example.torque.ui.theme.TorqueTheme
import kotlinx.coroutines.launch

class OpcionesMantenimiento : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val motoIdIntent = intent.getLongExtra("idMoto", -1L)
        setContent {
            TorqueTheme {
                PantallaOpcionesMantenimiento(
                    motoIdInicial = motoIdIntent,
                    onNuevoMantenimiento = { id ->
                        val intent = Intent(this, RegistroMantenimiento::class.java).apply {
                            putExtra("idMoto", id)
                        }
                        startActivity(intent)
                    },
                    onVerHistorial = { id ->
                        val intent = Intent(this, MantenimientoHistorial::class.java).apply {
                            putExtra("idMoto", id)
                        }
                        startActivity(intent)
                    },
                    onAtras = { finish() }
                )
            }
        }
    }
}

@Composable
fun PantallaOpcionesMantenimiento(
    motoIdInicial: Long,
    onNuevoMantenimiento: (Long) -> Unit,
    onVerHistorial: (Long) -> Unit,
    onAtras: () -> Unit
) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }
    
    var motoSeleccionada by remember { mutableStateOf<MotoEntity?>(null) }

    LaunchedEffect(motoIdInicial) {
        if (motoIdInicial != -1L) {
            motoSeleccionada = repositorio.obtenerMotoPorId(motoIdInicial)
        } else {
            // Si no hay ID, intentamos pillar la principal
            repositorio.obtenerMotoPrincipalFlow().collect { principal ->
                motoSeleccionada = principal
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(ColoresTorque.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Header (Compacto) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ColoresTorque.ALTURA_CABECERA)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAtras) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = ColoresTorque.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Mantenimiento",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top), // Cambiado a Top y menos espacio
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Info de la moto seleccionada
                motoSeleccionada?.let { moto ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ColoresTorque.SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Motorcycle, contentDescription = null, tint = ColoresTorque.Primary)
                            Column {
                                Text("${moto.marca} ${moto.modelo}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${moto.kilometros} km", color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }

                    TarjetaOpcionMantenimiento(
                        titulo = "Nuevo Registro",
                        subtitulo = "Registrar tareas realizadas",
                        icono = Icons.Default.Build,
                        color = ColoresTorque.Primary,
                        onClick = { onNuevoMantenimiento(moto.idMoto) }
                    )

                    TarjetaOpcionMantenimiento(
                        titulo = "Ver Historial",
                        subtitulo = "Consulta registros pasados",
                        icono = Icons.Default.History,
                        color = ColoresTorque.Accent,
                        onClick = { onVerHistorial(moto.idMoto) }
                    )
                } ?: run {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay ninguna moto seleccionada", color = Color.White)
                    }
                }
            }
        }

    }
}

@Composable
fun TarjetaOpcionMantenimiento(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ColoresTorque.SurfaceDark)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp), // Reducido de 24.dp
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp) // Reducido de 56.dp
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(24.dp)) // Reducido de 32.dp
        }
        Column {
            Text(titulo, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) // Reducido de 18.sp
            Text(subtitulo, color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp) // Reducido de 14.sp
        }
    }
}
