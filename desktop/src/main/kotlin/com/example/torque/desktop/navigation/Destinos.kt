package com.example.torque.desktop.navigation

sealed class Screen {
    data object DASHBOARD : Screen()
    data object GARAJE : Screen()
    data object MOTO_DETALLE : Screen()
    data object AJUSTES : Screen()
}
