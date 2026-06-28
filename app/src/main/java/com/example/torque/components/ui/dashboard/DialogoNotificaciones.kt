package com.example.torque.components.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R
import com.example.torque.utils.ResultadoSalud

@Composable
fun DialogoNotificaciones(
    resultadoSalud: ResultadoSalud?,
    onDismiss: () -> Unit
) {
    val itemsNotificacion = remember(resultadoSalud) {
        resultadoSalud?.desglose
            ?.filter { it.cumplimiento <= 20 && !it.tipo.esDesgaste }
            ?.sortedBy { it.cumplimiento }
            ?: emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColoresTorque.SurfaceDark,
        icon = {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = ColoresTorque.Primary)
        },
        title = {
            Text(stringResource(R.string.notifications_title), color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            if (itemsNotificacion.isEmpty()) {
                Text(
                    stringResource(R.string.no_notifications_pending),
                    color = ColoresTorque.OnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsNotificacion.forEach { detalle ->
                        val vencido = detalle.cumplimiento == 0
                        val colorEstado = if (vencido) Color(0xFFEF4444) else Color(0xFFEAB308)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ColoresTorque.SurfaceContainer)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (vencido) Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = null,
                                tint = colorEstado,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = detalle.tipo.nombre,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (vencido) stringResource(R.string.notification_expired)
                                           else stringResource(R.string.notification_soon),
                                    color = colorEstado,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "${detalle.cumplimiento}%",
                                color = colorEstado,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close), color = ColoresTorque.Primary)
            }
        }
    )
}
