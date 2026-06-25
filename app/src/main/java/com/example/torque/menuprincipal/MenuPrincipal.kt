package com.example.torque.menuprincipal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.torque.ajustes.AjustesRepositorio
import com.example.torque.ajustes.dataStore
import com.example.torque.components.ui.dashboard.TorqueDashboardScreen
import com.example.torque.garage.ListaGaraje
import com.example.torque.maintenance.OpcionesMantenimiento
import com.example.torque.perfil.PerfilActivity
import com.example.torque.ui.theme.TorqueTheme

class MenuPrincipal : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repositorio = remember { AjustesRepositorio(this.dataStore) }
            val modoOscuro by repositorio.modoOscuroActivadoFlow.collectAsState(initial = true)
            TorqueTheme(darkMode = modoOscuro) {
                TorqueNavHost()
            }
        }
    }
}

@Composable
fun TorqueNavHost() {
    val navController = rememberNavController()
    val contexto = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {

        composable("dashboard") {
            TorqueDashboardScreen(
                onIrGaraje = {
                    contexto.startActivity(Intent(contexto, ListaGaraje::class.java))
                },
                onIrMantenimientos = { idMoto ->
                    val intent = Intent(contexto, OpcionesMantenimiento::class.java).apply {
                        if (idMoto != null) putExtra("idMoto", idMoto)
                    }
                    contexto.startActivity(intent)
                },
                onIrPerfil = {
                    contexto.startActivity(Intent(contexto, PerfilActivity::class.java))
                }
            )
        }
    }
}
