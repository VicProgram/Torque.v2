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
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.ajustes.AjustesRepositorio
import com.example.torque.utils.BackupManager
import com.example.torque.utils.LocaleHelper

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

    val notificacionesActivadas by repo.notificacionesActivadasFlow.collectAsState(initial = true)
    val modoOscuroActivado by repo.modoOscuroActivadoFlow.collectAsState(initial = true)
    val idiomaActual by repo.idiomaFlow.collectAsState(initial = "es")
    var mostrarDialogoIdioma by remember { mutableStateOf(false) }
    var mostrarDialogoRestaurar by remember { mutableStateOf(false) }
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColoresTorque.SurfaceDark)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = com.example.torque.R.string.notifications), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = stringResource(id = com.example.torque.R.string.notifications_subtitle), color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(
                        checked = notificacionesActivadas,
                        onCheckedChange = { activado -> coroutineScope.launch { repo.establecerNotificacionesActivadas(activado) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = ColoresTorque.Primary, uncheckedThumbColor = ColoresTorque.OnSurfaceVariant)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColoresTorque.SurfaceDark)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = com.example.torque.R.string.dark_mode), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = stringResource(id = com.example.torque.R.string.dark_mode_subtitle), color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(
                        checked = modoOscuroActivado,
                        onCheckedChange = { activado -> coroutineScope.launch { repo.establecerModoOscuroActivado(activado) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = ColoresTorque.Primary, uncheckedThumbColor = ColoresTorque.OnSurfaceVariant)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColoresTorque.SurfaceDark)
                        .clickable { mostrarDialogoIdioma = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = R.string.language), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = stringResource(id = R.string.language_subtitle), color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                    }
                    Text(
                        text = if (idiomaActual == "es") stringResource(id = R.string.language_spanish) else stringResource(id = R.string.language_english),
                        color = ColoresTorque.Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = com.example.torque.R.string.backup_section),
                    color = ColoresTorque.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColoresTorque.SurfaceDark)
                        .clickable { crearBackup.launch(context.getString(com.example.torque.R.string.backup_default_name)) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = ColoresTorque.Primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(com.example.torque.R.string.export_backup), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = stringResource(com.example.torque.R.string.export_backup_subtitle), color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColoresTorque.SurfaceDark)
                        .clickable(enabled = !restaurando) { seleccionarBackup.launch(arrayOf("application/zip")) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = ColoresTorque.Primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = if (restaurando) stringResource(com.example.torque.R.string.restoring) else stringResource(com.example.torque.R.string.restore_backup), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = stringResource(com.example.torque.R.string.restore_backup_subtitle), color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        }

        if (mostrarDialogoIdioma) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoIdioma = false },
                containerColor = ColoresTorque.SurfaceDark,
                titleContentColor = Color.White,
                textContentColor = Color.White,
                icon = { Icon(Icons.Default.Language, contentDescription = null, tint = ColoresTorque.Primary) },
                title = { Text(stringResource(R.string.language)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                mostrarDialogoIdioma = false
                                coroutineScope.launch {
                                    repo.establecerIdioma("es")
                                    LocaleHelper.setLanguage(context, "es")
                                    Toast.makeText(context, context.getString(R.string.language_spanish), Toast.LENGTH_SHORT).show()
                                    (context as? android.app.Activity)?.recreate()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stringResource(R.string.language_spanish),
                                color = if (idiomaActual == "es") ColoresTorque.Primary else Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                        TextButton(
                            onClick = {
                                mostrarDialogoIdioma = false
                                coroutineScope.launch {
                                    repo.establecerIdioma("en")
                                    LocaleHelper.setLanguage(context, "en")
                                    Toast.makeText(context, context.getString(R.string.language_english), Toast.LENGTH_SHORT).show()
                                    (context as? android.app.Activity)?.recreate()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stringResource(R.string.language_english),
                                color = if (idiomaActual == "en") ColoresTorque.Primary else Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogoIdioma = false }) {
                        Text(stringResource(R.string.btn_cancel), color = ColoresTorque.OnSurfaceVariant)
                    }
                }
            )
        }

        if (mostrarDialogoRestaurar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoRestaurar = false },
                containerColor = ColoresTorque.SurfaceDark,
                titleContentColor = Color.White,
                textContentColor = Color.White,
                icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = ColoresTorque.Primary) },
                title = { Text(stringResource(com.example.torque.R.string.dialog_restore_title)) },
                text = { Text(stringResource(com.example.torque.R.string.dialog_restore_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarDialogoRestaurar = false
                        restaurando = true
                        coroutineScope.launch {
                            uriRestaurar?.let { uri ->
                                val resultado = BackupManager.restaurar(context, uri)
                                if (resultado.isSuccess) {
                                    Toast.makeText(context, context.getString(com.example.torque.R.string.toast_restore_success), Toast.LENGTH_LONG).show()
                                    (context as? android.app.Activity)?.finishAffinity()
                                } else {
                                    Toast.makeText(context, context.getString(com.example.torque.R.string.toast_restore_error, resultado.exceptionOrNull()?.message), Toast.LENGTH_LONG).show()
                                    restaurando = false
                                }
                            }
                        }
                    }) {
                        Text(stringResource(com.example.torque.R.string.btn_restore), color = ColoresTorque.Primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoRestaurar = false }) {
                        Text(stringResource(com.example.torque.R.string.btn_cancel), color = ColoresTorque.OnSurfaceVariant)
                    }
                }
            )
        }
    }
}
