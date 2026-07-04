package com.example.torque.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.torque.menuprincipal.MenuPrincipal
import com.example.torque.network.ApiClient
import com.example.torque.network.AuthManager
import com.example.torque.network.TorqueApiService
import com.example.torque.network.models.PinLoginRequest
import com.example.torque.ui.theme.TorqueTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager.getInstance(this)

        enableEdgeToEdge()

        setContent {
            TorqueTheme {
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()

                LoginScreen(
                    onLogin = { pin, serverUrl ->
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                authManager.saveServerUrl(serverUrl)
                                val normalised = serverUrl.trimEnd('/') + "/"
                                val api = ApiClient.create(normalised, authManager)
                                val response = api.loginPin(PinLoginRequest(pin))
                                if (response.isSuccessful) {
                                    val data = response.body()!!
                                    authManager.saveLogin(
                                        token = data.accessToken,
                                        idUsuario = data.idUsuario,
                                        idTaller = data.idTaller,
                                        nombre = data.nombre,
                                        rol = data.rol,
                                    )
                                    startActivity(Intent(this@LoginActivity, MenuPrincipal::class.java))
                                    finish()
                                } else {
                                    errorMessage = when (response.code()) {
                                        401 -> "PIN incorrecto"
                                        404 -> "Servidor no encontrado"
                                        else -> "Error: ${response.code()} ${response.message()}"
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "No se pudo conectar: ${e.localizedMessage ?: "Error desconocido"}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                )
            }
        }
    }
}
