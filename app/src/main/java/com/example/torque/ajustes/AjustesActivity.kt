package com.example.torque.ajustes

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.torque.R
import com.example.torque.base.BaseActivity
import com.example.torque.network.ApiClient
import com.example.torque.network.AuthManager
import com.example.torque.ui.dashboard.ColoresTorque
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.ajustes.AjustesRepositorio
import com.example.torque.utils.BackupManager
import com.example.torque.utils.AyudaIdioma

class AjustesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TorqueTheme {
                AjustesScreen(onBack = { finish() })
            }
        }
    }
}

@Composable
fun AjustesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = AjustesRepositorio(context.dataStore)
    val coroutineScope = rememberCoroutineScope()

    val authManager = remember { AuthManager.getInstance(context) }
    val notificacionesActivadas by repo.notificacionesActivadasFlow.collectAsState(initial = true)
    val modoOscuroActivado by repo.modoOscuroActivadoFlow.collectAsState(initial = true)
    val idiomaActual by repo.idiomaFlow.collectAsState(initial = "es")
    val serverUrl by authManager.serverUrlFlow.collectAsState(initial = AuthManager.DEFAULT_SERVER_URL)
    var mostrarDialogoIdioma by remember { mutableStateOf(false) }
    var mostrarDialogoRestaurar by remember { mutableStateOf(false) }
    var mostrarDialogoServer by remember { mutableStateOf(false) }
    var uriRestaurar by remember { mutableStateOf<Uri?>(null) }
    var restaurando by remember { mutableStateOf(false) }

    val crearBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                Toast.makeText(context, context.getString(com.example.torque.R.string.toast_exporting_backup), Toast.LENGTH_SHORT).show()
                val resultado = BackupManager.exportar(context, it)
                if (resultado.isSuccess) {
                    Toast.makeText(context, context.getString(com.example.torque.R.string.toast_backup_saved_title), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, context.getString(com.example.torque.R.string.toast_backup_error, resultado.exceptionOrNull()?.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val seleccionarBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            uriRestaurar = it
            mostrarDialogoRestaurar = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(ColoresTorque.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ColoresTorque.ALTURA_CABECERA)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.example.torque.R.string.content_desc_back), tint = ColoresTorque.Primary)
                }
                Text(
                    text = stringResource(id = com.example.torque.R.string.settings_title),
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = com.example.torque.R.string.preferences_header),
                    color = ColoresTorque.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )

                FilaAjusteConSwitch(
                    icono = Icons.Default.Notifications,
                    titulo = stringResource(R.string.notifications),
                    subtitulo = stringResource(R.string.notifications_subtitle),
                    activado = notificacionesActivadas,
                    onCambio = { activado -> coroutineScope.launch { repo.establecerNotificacionesActivadas(activado) } }
                )

                FilaAjusteConSwitch(
                    icono = Icons.Default.Palette,
                    titulo = stringResource(R.string.dark_mode),
                    subtitulo = stringResource(R.string.dark_mode_subtitle),
                    activado = modoOscuroActivado,
                    onCambio = { activado -> coroutineScope.launch { repo.establecerModoOscuroActivado(activado) } }
                )

                FilaAjusteConValor(
                    icono = Icons.Default.Language,
                    titulo = stringResource(R.string.language),
                    subtitulo = stringResource(R.string.language_subtitle),
                    valor = if (idiomaActual == "es") stringResource(R.string.language_spanish) else stringResource(R.string.language_english),
                    onClick = { mostrarDialogoIdioma = true }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.backup_section),
                    color = ColoresTorque.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )

                FilaAjusteAccion(
                    icono = Icons.Default.Backup,
                    titulo = stringResource(R.string.export_backup),
                    subtitulo = stringResource(R.string.export_backup_subtitle),
                    onClick = { crearBackup.launch(context.getString(R.string.backup_default_name)) }
                )

                FilaAjusteAccion(
                    icono = Icons.Default.Restore,
                    titulo = if (restaurando) stringResource(R.string.restoring) else stringResource(R.string.restore_backup),
                    subtitulo = stringResource(R.string.restore_backup_subtitle),
                    onClick = { seleccionarBackup.launch(arrayOf("application/zip")) },
                    habilitado = !restaurando
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.server_section),
                    color = ColoresTorque.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )

                FilaAjusteConValor(
                    icono = Icons.Default.Computer,
                    titulo = stringResource(R.string.server_url),
                    subtitulo = stringResource(R.string.server_url_subtitle),
                    valor = serverUrl.removePrefix("http://").removePrefix("https://").take(25),
                    onClick = { mostrarDialogoServer = true }
                )

                FilaAjusteAccion(
                    icono = Icons.Default.CheckCircle,
                    titulo = stringResource(R.string.server_test_connection),
                    subtitulo = stringResource(R.string.server_test_connection_subtitle),
                    onClick = {
                        coroutineScope.launch {
                            val normalised = serverUrl.trimEnd('/') + "/"
                            val api = ApiClient.create(normalised, authManager)
                            try {
                                val response = api.getMe()
                                if (response.isSuccessful) {
                                    Toast.makeText(context, context.getString(R.string.toast_server_test_success), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.toast_server_test_error, "HTTP ${response.code()}"), Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, context.getString(R.string.toast_server_test_error, e.localizedMessage ?: "Error"), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }
        }

        if (mostrarDialogoIdioma) {
            DialogoSeleccionIdioma(
                idiomaActual = idiomaActual,
                onDismiss = { mostrarDialogoIdioma = false },
                onIdiomaChange = { codigo ->
                    mostrarDialogoIdioma = false
                    coroutineScope.launch {
                        repo.establecerIdioma(codigo)
                        AyudaIdioma.setLanguage(context, codigo)
                        Toast.makeText(context, context.getString(if (codigo == "es") R.string.language_spanish else R.string.language_english), Toast.LENGTH_SHORT).show()
                        (context as? android.app.Activity)?.recreate()
                    }
                }
            )
        }

        if (mostrarDialogoRestaurar) {
            DialogoConfirmarRestauracion(
                onDismiss = { mostrarDialogoRestaurar = false },
                onConfirmar = {
                    mostrarDialogoRestaurar = false
                    restaurando = true
                    coroutineScope.launch {
                        uriRestaurar?.let { uri ->
                            val resultado = BackupManager.restaurar(context, uri)
                            if (resultado.isSuccess) {
                                Toast.makeText(context, context.getString(R.string.toast_restore_success), Toast.LENGTH_LONG).show()
                                (context as? android.app.Activity)?.finishAffinity()
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_restore_error, resultado.exceptionOrNull()?.message), Toast.LENGTH_LONG).show()
                                restaurando = false
                            }
                        }
                    }
                }
            )
        }

        if (mostrarDialogoServer) {
            DialogoConfigurarServidor(
                urlActual = serverUrl,
                onDismiss = { mostrarDialogoServer = false },
                onConfirmar = { nuevaUrl ->
                    mostrarDialogoServer = false
                    coroutineScope.launch {
                        authManager.saveServerUrl(nuevaUrl.trimEnd('/'))
                    }
                }
            )
        }
    }
}
