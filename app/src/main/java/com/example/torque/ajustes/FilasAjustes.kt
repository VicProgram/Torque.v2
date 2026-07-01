package com.example.torque.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.components.ui.dashboard.ColoresTorque

@Composable
fun FilaAjusteConSwitch(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    activado: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColoresTorque.SurfaceDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(text = subtitulo, color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
        }
        Switch(
            checked = activado,
            onCheckedChange = onCambio,
            colors = SwitchDefaults.colors(checkedThumbColor = ColoresTorque.Primary, uncheckedThumbColor = ColoresTorque.OnSurfaceVariant)
        )
    }
}

@Composable
fun FilaAjusteConValor(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    valor: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColoresTorque.SurfaceDark)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(text = subtitulo, color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
        }
        Text(
            text = valor,
            color = ColoresTorque.Primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun FilaAjusteAccion(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    habilitado: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColoresTorque.SurfaceDark)
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = ColoresTorque.Primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(text = subtitulo, color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
        }
    }
}
