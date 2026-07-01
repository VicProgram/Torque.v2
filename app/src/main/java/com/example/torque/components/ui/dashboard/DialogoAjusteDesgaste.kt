package com.example.torque.components.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R
import com.example.torque.utils.TiposMantenimiento
import com.example.torque.utils.TipoVehiculo
import kotlin.math.roundToInt

@Composable
fun DialogoAjusteDesgaste(
    initialPorcentajes: Map<Long, Int>,
    onDismiss: () -> Unit,
    onGuardar: (Map<Long, Int>) -> Unit = {},
    tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO
) {
    val wearTipos = TiposMantenimiento.WEAR_TIPOS.filter { it.tipoVehiculo == tipoVehiculo }
    var porcentajes by remember { mutableStateOf(initialPorcentajes) }

    DialogoTorque(
        onDismiss = onDismiss,
        title = stringResource(R.string.wear_status_title),
        confirmText = stringResource(R.string.save),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = { onGuardar(porcentajes) },
        content = {
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
    )
}
