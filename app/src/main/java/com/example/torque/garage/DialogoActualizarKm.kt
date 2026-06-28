package com.example.torque.garage

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.torque.R
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.components.ui.dashboard.DialogoTorque

@Composable
fun DialogoActualizarKm(
    kmInicial: Int,
    onGuardar: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(kmInicial.toString()) }

    DialogoTorque(
        onDismiss = onDismiss,
        title = stringResource(R.string.dialog_update_mileage_title),
        confirmText = stringResource(R.string.save),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = {
            val km = text.toIntOrNull()
            if (km != null && km >= 0) {
                onGuardar(km)
            }
            onDismiss()
        },
        content = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.field_kilometers)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ColoresTorque.Primary,
                    unfocusedBorderColor = ColoresTorque.BorderDark,
                    focusedLabelColor = ColoresTorque.Primary,
                    unfocusedLabelColor = ColoresTorque.OnSurfaceVariant
                )
            )
        }
    )
}
