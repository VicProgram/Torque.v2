package com.example.torque.garage

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.torque.R
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.components.ui.dashboard.DialogoTorque

@Composable
fun DialogoEliminarFoto(
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogoTorque(
        onDismiss = onDismiss,
        title = stringResource(R.string.dialog_delete_photo_title),
        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ColoresTorque.RojoError) },
        confirmText = stringResource(R.string.btn_delete),
        confirmColor = ColoresTorque.RojoError,
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = onConfirmar,
        content = {
            Text(stringResource(R.string.action_irreversible), color = Color.White)
        }
    )
}
