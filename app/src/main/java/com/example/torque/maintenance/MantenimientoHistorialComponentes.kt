package com.example.torque.maintenance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.torque.R
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.room.entity.FotoMantenimientoEntity
import com.example.torque.room.entity.MantenimientoEntity
import java.io.File

private val iconosMantenimiento = listOf(
    listOf("aceite") to Icons.Default.OilBarrel,
    listOf("neumático", "rueda") to Icons.Default.TireRepair,
    listOf("cadena", "kit") to Icons.Default.SettingsSuggest,
    listOf("luz", "eléctrico", "batería") to Icons.Default.Bolt,
    listOf("freno") to Icons.Default.BackHand,
    listOf("retenes", "horquilla") to Icons.Default.Construction
)

private fun iconoMantenimiento(nombre: String, tareas: String?): ImageVector {
    for (match in iconosMantenimiento) {
        for (keyword in match.first) {
            if (nombre.contains(keyword, ignoreCase = true) ||
                (tareas?.contains(keyword, ignoreCase = true) ?: false)
            ) {
                return match.second
            }
        }
    }
    return Icons.Default.Build
}

@Composable
fun ItemLogHistorial(item: MantenimientoEntity, onClick: () -> Unit, onEliminar: () -> Unit = {}) {
    val icono = iconoMantenimiento(item.nombreMantenimiento, item.tareas)

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
    fotos: List<FotoMantenimientoEntity> = emptyList(),
    onBack: () -> Unit = {},
    onAddFoto: (Uri) -> Unit = {},
    onAddFactura: (Uri) -> Unit = {},
    onDeleteFoto: (FotoMantenimientoEntity) -> Unit = {}
) {
    val launcherFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onAddFoto(it) }
    }
    val launcherFactura = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onAddFactura(it) }
    }

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

        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.section_attachments), color = ColoresTorque.Outline, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { launcherFoto.launch("image/*") },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColoresTorque.Primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.btn_add_photo), fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { launcherFactura.launch("image/*") },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColoresTorque.Accent)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.btn_add_invoice), fontSize = 12.sp)
            }
        }
        if (fotos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fotos) { foto ->
                    Box(modifier = Modifier.size(80.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(File(foto.ruta)),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        IconButton(
                            onClick = { onDeleteFoto(foto) },
                            modifier = Modifier.align(Alignment.TopEnd).size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.content_desc_delete), tint = ColoresTorque.RojoError, modifier = Modifier.size(14.dp))
                        }
                        if (foto.tipo == "factura") {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .background(ColoresTorque.Accent.copy(alpha = 0.8f), RoundedCornerShape(topEnd = 4.dp, bottomStart = 4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(stringResource(R.string.invoice_badge), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
