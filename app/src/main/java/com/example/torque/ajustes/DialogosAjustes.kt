package com.example.torque.ajustes

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R
import com.example.torque.ui.dashboard.ColoresTorque
import com.example.torque.utils.AyudaIdioma
import com.example.torque.utils.BackupManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DialogoSeleccionIdioma(
    idiomaActual: String,
    onDismiss: () -> Unit,
    onIdiomaChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColoresTorque.SurfaceDark,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        icon = { Icon(Icons.Default.Language, contentDescription = null, tint = ColoresTorque.Primary) },
        title = { Text(stringResource(R.string.language)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onIdiomaChange("es") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.language_spanish),
                        color = if (idiomaActual == "es") ColoresTorque.Primary else Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
                TextButton(
                    onClick = { onIdiomaChange("en") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.language_english),
                        color = if (idiomaActual == "en") ColoresTorque.Primary else Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = ColoresTorque.OnSurfaceVariant)
            }
        }
    )
}

@Composable
fun DialogoConfirmarRestauracion(
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColoresTorque.SurfaceDark,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = ColoresTorque.Primary) },
        title = { Text(stringResource(R.string.dialog_restore_title)) },
        text = { Text(stringResource(R.string.dialog_restore_message)) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(stringResource(R.string.btn_restore), color = ColoresTorque.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = ColoresTorque.OnSurfaceVariant)
            }
        }
    )
}

@Composable
fun DialogoConfigurarServidor(
    urlActual: String,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    var url by remember { mutableStateOf(urlActual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColoresTorque.SurfaceDark,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        icon = { Icon(Icons.Default.Computer, contentDescription = null, tint = ColoresTorque.Primary) },
        title = { Text(stringResource(R.string.dialog_server_title)) },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(stringResource(R.string.dialog_server_url_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ColoresTorque.Primary,
                    unfocusedBorderColor = ColoresTorque.Outline,
                    cursorColor = ColoresTorque.Primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(url) }) {
                Text(stringResource(R.string.btn_save), color = ColoresTorque.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = ColoresTorque.OnSurfaceVariant)
            }
        }
    )
}
