package com.example.torque.desktop.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.torque.desktop.ui.theme.ColoresTorque

@Composable
fun DialogoTorque(
    titulo: String,
    mensaje: String,
    textoConfirmar: String = "Aceptar",
    textoCancelar: String = "Cancelar",
    colorConfirmar: Color = ColoresTorque.TorquePrimary,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(textoConfirmar, color = colorConfirmar)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(textoCancelar)
            }
        }
    )
}
