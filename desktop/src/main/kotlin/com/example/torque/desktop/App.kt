package com.example.torque.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.torque.desktop.navigation.Screen
import com.example.torque.desktop.ui.screens.AjustesScreen
import com.example.torque.desktop.ui.screens.DashboardScreen
import com.example.torque.desktop.ui.screens.GarajeScreen

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.DASHBOARD) }

    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(modifier = Modifier.fillMaxHeight()) {
            NavigationRailItem(
                selected = currentScreen == Screen.DASHBOARD,
                onClick = { currentScreen = Screen.DASHBOARD },
                icon = { Icon(Icons.Default.Home, contentDescription = "Panel") },
                label = { Text("Panel") }
            )
            NavigationRailItem(
                selected = currentScreen == Screen.GARAJE,
                onClick = { currentScreen = Screen.GARAJE },
                icon = { Icon(Icons.Default.DirectionsBike, contentDescription = "Garaje") },
                label = { Text("Garaje") }
            )
            NavigationRailItem(
                selected = currentScreen == Screen.AJUSTES,
                onClick = { currentScreen = Screen.AJUSTES },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                label = { Text("Ajustes") }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> DashboardScreen()
                Screen.GARAJE -> GarajeScreen()
                Screen.AJUSTES -> AjustesScreen()
            }
        }
    }
}
