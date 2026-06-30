package com.example.torque.garage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R
import com.example.torque.components.ui.dashboard.ColoresTorque

@Composable
fun FilaDetalle(etiqueta: String, valor: String?, editable: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = if (editable) Modifier
            .fillMaxWidth()
            .clickable { onClick() }
        else Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, color = ColoresTorque.OnSurfaceVariant, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = valor ?: stringResource(R.string.not_available),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (editable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Edit, contentDescription = null, tint = ColoresTorque.Primary, modifier = Modifier.size(14.dp))
            }
        }
    }
}
