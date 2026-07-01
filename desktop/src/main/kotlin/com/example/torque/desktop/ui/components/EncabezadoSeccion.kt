package com.example.torque.desktop.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.desktop.ui.theme.ColoresTorque

@Composable
fun EncabezadoSeccion(
    titulo: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = titulo.uppercase(),
        modifier = modifier.padding(vertical = 8.dp),
        color = ColoresTorque.TorquePrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}
