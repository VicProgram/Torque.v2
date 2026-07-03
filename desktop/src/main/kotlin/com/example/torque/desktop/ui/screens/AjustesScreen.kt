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
fun AjustesScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ajustes",
            color = ColoresTorque.OnSurface
        )
        Text(
            text = "Proximamente: importar backup, exportar, idioma, modo oscuro",
            color = ColoresTorque.OnSurfaceVariant
        )
    }
}
