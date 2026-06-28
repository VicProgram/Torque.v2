package com.example.torque.components.ui.dashboard

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.torque.R

@Composable
fun DialogoKm(
    initialKm: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialKm.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColoresTorque.SurfaceDark,
        title = { Text(stringResource(R.string.km_edit_title), color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.field_kilometers), color = ColoresTorque.OnSurfaceVariant) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ColoresTorque.Primary,
                    unfocusedBorderColor = ColoresTorque.Outline,
                    cursorColor = ColoresTorque.Primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedLabelColor = ColoresTorque.Primary,
                    unfocusedLabelColor = ColoresTorque.OnSurfaceVariant
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val km = text.toIntOrNull()
                if (km != null && km >= 0) {
                    onSave(km)
                }
                onDismiss()
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
