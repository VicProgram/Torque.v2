package com.example.torque.ajustes

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.ajustes.AjustesRepositorio
import com.example.torque.utils.BackupManager

class AjustesActivity : ComponentActivity() {
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
    val logoSelccionado by repo.logoSelccionadoFlow.collectAsState(initial = 1)

    var mostrarDialogoRestaurar by remember { mutableStateOf(false) }
    var uriRestaurar by remember { mutableStateOf<Uri?>(null) }
    var restaurando by remember { mutableStateOf(false) }

    val crearBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                Toast.makeText(context, "Exportando copia de seguridad…", Toast.LENGTH_SHORT).show()
                val resultado = BackupManager.exportar(context, it)
                if (resultado.isSuccess) {
                    Toast.makeText(context, "Copia de seguridad guardada", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error al exportar: ${resultado.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
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
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = ColoresTorque.Primary)
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

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = com.example.torque.R.string.logo_header),
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
                    Icon(Icons.Default.Palette, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Logo de la app", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = stringResource(id = com.example.torque.R.string.logo_subtitle), color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColoresTorque.SurfaceDark)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 1..6) {
                        val resId = when (i) {
                            1 -> com.example.torque.R.drawable.ic_logo1
                            2 -> com.example.torque.R.drawable.ic_logo2
                            3 -> com.example.torque.R.drawable.ic_logo3
                            4 -> com.example.torque.R.drawable.ic_logo4
                            5 -> com.example.torque.R.drawable.ic_logo5
                            6 -> com.example.torque.R.drawable.ic_logo6
                            else -> com.example.torque.R.drawable.ic_logo1
                        }
                        val borderModifier = if (logoSelccionado == i) {
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColoresTorque.Primary)
                                .padding(2.dp)
                        } else {
                            Modifier
                        }
                        Box(
                            modifier = borderModifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Transparent)
                                .clickable { coroutineScope.launch { repo.establecerLogoSeleccionado(i) } }
                                .padding(4.dp)
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Logo $i",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "COPIA DE SEGURIDAD",
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
                        .clickable { crearBackup.launch("torque_backup.zip") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = ColoresTorque.Primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Exportar copia", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = "BD + fotos + preferencias", color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
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
                        Text(text = if (restaurando) "Restaurando…" else "Restaurar copia", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = "Sustituye todos los datos actuales", color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        }

        if (mostrarDialogoRestaurar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoRestaurar = false },
                containerColor = ColoresTorque.SurfaceDark,
                titleContentColor = Color.White,
                textContentColor = Color.White,
                icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = ColoresTorque.Primary) },
                title = { Text("¿Restaurar copia?") },
                text = { Text("Se sustituirán todos los datos actuales (motos, fotos, mantenimientos y preferencias). La app se reiniciará.") },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarDialogoRestaurar = false
                        restaurando = true
                        coroutineScope.launch {
                            uriRestaurar?.let { uri ->
                                val resultado = BackupManager.restaurar(context, uri)
                                if (resultado.isSuccess) {
                                    Toast.makeText(context, "Datos restaurados. La app se cerrará.", Toast.LENGTH_LONG).show()
                                    (context as? android.app.Activity)?.finishAffinity()
                                } else {
                                    Toast.makeText(context, "Error al restaurar: ${resultado.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                    restaurando = false
                                }
                            }
                        }
                    }) {
                        Text("Restaurar", color = ColoresTorque.Primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoRestaurar = false }) {
                        Text("Cancelar", color = ColoresTorque.OnSurfaceVariant)
                    }
                }
            )
        }
    }
}
