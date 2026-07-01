package com.example.torque.desktop.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.torque.desktop.ui.theme.ColoresTorque

@Composable
fun CampoTextoTorque(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { androidx.compose.material3.Text(label) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ColoresTorque.TorquePrimary,
            unfocusedBorderColor = ColoresTorque.TorqueBorderDark,
            cursorColor = ColoresTorque.TorquePrimary,
            focusedLabelColor = ColoresTorque.TorquePrimary,
            unfocusedLabelColor = ColoresTorque.OnSurfaceVariant
        )
    )
}
