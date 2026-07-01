package com.example.torque.desktop

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.example.torque.desktop.ui.theme.TorqueThemeDesktop

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Torque - Gestion de Mantenimiento",
        state = rememberWindowState(
            width = 1200.dp,
            height = 800.dp,
            position = WindowPosition(Alignment.Center)
        )
    ) {
        TorqueThemeDesktop {
            App()
        }
    }
}
