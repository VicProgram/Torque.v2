package com.example.torque.desktop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.torque.desktop.ui.theme.ColoresTorque

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Panel de Control",
            color = ColoresTorque.OnSurface
        )
        Text(
            text = "Proximamente: carrusel de motos, salud, recordatorios",
            color = ColoresTorque.OnSurfaceVariant
        )
    }
}
