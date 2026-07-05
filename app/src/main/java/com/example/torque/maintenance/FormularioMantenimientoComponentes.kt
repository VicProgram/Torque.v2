package com.example.torque.maintenance

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.torque.R
import com.example.torque.ui.dashboard.BotonTorque
import com.example.torque.ui.dashboard.ColoresTorque
import com.example.torque.ui.dashboard.CampoTextoTorque
import com.example.torque.room.RepositorioApp
import com.example.torque.utils.terminar
import kotlinx.coroutines.launch

data class Adjunto(val uri: Uri, val tipo: String = "foto", val rutaInterna: String? = null)

data class EstadoGuardado(val errorKms: Boolean = false, val kmMotoActual: Int = 0, val mostrarActualizarKm: Boolean = false, val guardarDirecto: Boolean = false)

@Composable
fun CabeceraFormulario(hayCambios: Boolean, contexto: android.content.Context, onDescartar: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ColoresTorque.ALTURA_CABECERA)
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
            textAlign = TextAlign.Center,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun FormularioCampos(
    tareasSeleccionadas: List<String>,
    kms: String, intervalo: String, precio: String, tiempo: String, notas: String,
    errorKms: Boolean, esDesgaste: Boolean, tipoDesgaste: com.example.torque.utils.TipoMantenimiento?,
    porcentajeVida: Int,
    adjuntos: List<Adjunto> = emptyList(),
    modifier: Modifier = Modifier,
    onKmsChange: (String) -> Unit, onIntervaloChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit, onTiempoChange: (String) -> Unit,
    onNotasChange: (String) -> Unit, onPorcentajeVidaChange: (Int) -> Unit,
    onAddFoto: () -> Unit = {}, onAddFactura: () -> Unit = {},
    onRemoveAdjunto: (Adjunto) -> Unit = {}
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = ColoresTorque.SurfaceDark),
            shape = RoundedCornerShape(12.dp)
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

        SeccionAdjuntos(adjuntos, onAddFoto, onAddFactura, onRemoveAdjunto)

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SeccionAdjuntos(
    adjuntos: List<Adjunto>,
    onAddFoto: () -> Unit,
    onAddFactura: () -> Unit,
    onRemoveAdjunto: (Adjunto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.section_attachments), color = ColoresTorque.Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onAddFoto,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColoresTorque.Primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.btn_add_photo), fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = onAddFactura,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColoresTorque.Accent)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.btn_add_invoice), fontSize = 13.sp)
            }
        }

        if (adjuntos.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                adjuntos.forEach { adjunto ->
                    Box(modifier = Modifier.size(72.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(adjunto.uri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        IconButton(
                            onClick = { onRemoveAdjunto(adjunto) },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.content_desc_delete), tint = ColoresTorque.RojoError, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionDesgaste(
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
        shape = RoundedCornerShape(12.dp)
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
fun DialogoDescartar(
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
fun DialogoActualizarKm(
    mostrar: Boolean, kms: String, kmMotoActual: Int,
    onConfirm: (actualizarKm: Boolean) -> Unit
) {
    if (mostrar) {
        AlertDialog(
            onDismissRequest = { onConfirm(false) },
            containerColor = ColoresTorque.SurfaceDark,
            title = { Text(stringResource(R.string.dialog_update_km_title)) },
            text = { Text(stringResource(R.string.dialog_update_km_message, kms, kmMotoActual)) },
            confirmButton = {
                TextButton(onClick = { onConfirm(true) }) {
                    Text(stringResource(R.string.btn_yes_update), color = ColoresTorque.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { onConfirm(false) }) {
                    Text(stringResource(R.string.btn_no), color = ColoresTorque.OnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun BotonGuardarFormulario(
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
                    Toast.makeText(contexto, contexto.getString(R.string.toast_moto_not_found), Toast.LENGTH_SHORT).show()
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
