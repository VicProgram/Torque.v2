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
fun GarajeScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Garaje",
            color = ColoresTorque.OnSurface
        )
        Text(
            text = "Proximamente: lista de motos, busqueda, filtros",
            color = ColoresTorque.OnSurfaceVariant
        )
    }
}
