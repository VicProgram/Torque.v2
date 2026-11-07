package com.example.torque.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.BuildConfig
import com.example.torque.network.AuthManager

private fun serverUrlError(url: String): String? {
    val trimmed = url.trim()
    return when {
        trimmed.isBlank() -> "Introduce la URL del servidor"
        !trimmed.startsWith("http://") && !trimmed.startsWith("https://") ->
            "La URL debe empezar por http:// o https://"
        !BuildConfig.DEBUG && !trimmed.startsWith("https://") ->
            "La versión de producción requiere HTTPS"
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (pin: String, serverUrl: String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
) {
    var pin by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf(AuthManager.DEFAULT_SERVER_URL) }
    var showServerConfig by remember { mutableStateOf(AuthManager.DEFAULT_SERVER_URL.isBlank()) }
    val focusManager = LocalFocusManager.current
    val urlError = serverUrlError(serverUrl)
    val cleartextWarning = BuildConfig.DEBUG && serverUrl.trim().startsWith("http://")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Torque Taller",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Introduce tu PIN (4-6 dígitos)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { value -> pin = value.filter(Char::isDigit).take(6) },
            label = { Text("PIN") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (pin.length >= 4 && urlError == null) onLogin(pin, serverUrl)
                },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onLogin(pin, serverUrl)
            },
            enabled = pin.length >= 4 && urlError == null && !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Entrar", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(visible = errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = { showServerConfig = !showServerConfig }) {
            Text("Configurar servidor", style = MaterialTheme.typography.bodySmall)
        }

        AnimatedVisibility(visible = showServerConfig) {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("URL del servidor") },
                    singleLine = true,
                    isError = urlError != null && serverUrl.isNotBlank(),
                    supportingText = {
                        when {
                            urlError != null && serverUrl.isNotBlank() -> Text(urlError)
                            cleartextWarning -> Text("Aviso: conexión HTTP sin cifrar (solo desarrollo)")
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
