package com.example.torque.components.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R
import com.example.torque.room.RepositorioApp
import com.example.torque.utils.TiposMantenimiento
import kotlin.math.roundToInt

@Composable
fun DialogoAjusteDesgaste(
    idMoto: Long,
    onDismiss: () -> Unit,
    onGuardar: (Long, Map<Long, Int>) -> Unit = { _, _ -> }
) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }

    val wearTipos = TiposMantenimiento.WEAR_TIPOS
    var porcentajes by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(idMoto) {
        val tipoIds = wearTipos.map { it.id }
        val ultimos = repositorio.obtenerUltimosMantenimientosPorTipos(idMoto, tipoIds)
        porcentajes = wearTipos.associate { it.id to (ultimos[it.id]?.porcentajeVida ?: 100) }
        cargando = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColoresTorque.SurfaceDark,
        title = { Text(stringResource(R.string.wear_status_title), color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            if (cargando) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColoresTorque.Primary)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    wearTipos.forEach { tipo ->
                        val pct = porcentajes[tipo.id] ?: 100
                        val colorBarra = when {
                            pct >= 70 -> ColoresTorque.VerdeExito
                            pct >= 40 -> ColoresTorque.AmarilloAdvertencia
                            else -> ColoresTorque.RojoError
                        }
                        Column {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tipo.nombre, color = Color.White, fontSize = 13.sp)
                                Text("$pct%", color = colorBarra, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = (porcentajes[tipo.id] ?: 100).toFloat(),
                                onValueChange = { nuevo ->
                                    val redondeado = (nuevo / 5f).roundToInt() * 5
                                    porcentajes = porcentajes + (tipo.id to redondeado.coerceIn(0, 100))
                                },
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGuardar(idMoto, porcentajes)
            }) {
                Text(stringResource(R.string.save), color = ColoresTorque.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = ColoresTorque.OnSurfaceVariant)
            }
        }
    )
}
