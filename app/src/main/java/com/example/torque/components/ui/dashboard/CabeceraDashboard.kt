package com.example.torque.components.ui.dashboard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R
import com.example.torque.ajustes.AjustesActivity
import com.example.torque.garage.AnadirMotoActivity
import com.example.torque.utils.BackupManager
import kotlinx.coroutines.launch

@Composable
fun CabeceraDashboard(
    onIrPerfil: () -> Unit,
    notificacionesPendientes: Int = 0,
    onNotificacionesClick: () -> Unit = {}
) {
    val contexto = LocalContext.current
    var mostrarMenuAjustes by remember { mutableStateOf(false) }
    var mostrarMenuPrincipal by remember { mutableStateOf(false) }
    var mostrarAcercaDe by remember { mutableStateOf(false) }
    var mostrarExportar by remember { mutableStateOf(false) }
    val scopeExportar = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ColoresTorque.ALTURA_CABECERA)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconButton(onClick = { mostrarMenuPrincipal = true }) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = stringResource(R.string.menu),
                        tint = ColoresTorque.OnSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                DropdownMenu(
                    expanded = mostrarMenuPrincipal,
                    onDismissRequest = { mostrarMenuPrincipal = false },
                    modifier = Modifier.background(ColoresTorque.SurfaceDark)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_go_to_garage), color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Motorcycle, contentDescription = null, tint = ColoresTorque.Primary) },
                        onClick = {
                            mostrarMenuPrincipal = false
                            contexto.startActivity(Intent(contexto, com.example.torque.garage.ListaGaraje::class.java))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_add_motorcycle), color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = ColoresTorque.Primary) },
                        onClick = {
                            mostrarMenuPrincipal = false
                            contexto.startActivity(Intent(contexto, AnadirMotoActivity::class.java))
                        }
                    )
                    HorizontalDivider(color = ColoresTorque.Outline.copy(alpha = 0.2f))
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_reminders), color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = ColoresTorque.Primary) },
                        onClick = {
                            mostrarMenuPrincipal = false
                            contexto.startActivity(Intent(contexto, AjustesActivity::class.java))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_export_data), color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null, tint = ColoresTorque.Primary) },
                        onClick = {
                            mostrarMenuPrincipal = false
                            mostrarExportar = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_about), color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = ColoresTorque.Primary) },
                        onClick = {
                            mostrarMenuPrincipal = false
                            mostrarAcercaDe = true
                        }
                    )
                }
            }
            Image(
                painter = painterResource(id = R.drawable.logo15),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.height(32.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(1.dp, ColoresTorque.Outline.copy(alpha = 0.5f), CircleShape)
                .clickable { onNotificacionesClick() }
            ) {
                BadgedBox(
                    badge = {
                        if (notificacionesPendientes > 0) {
                            Badge(
                                containerColor = ColoresTorque.RojoError,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = notificacionesPendientes.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.notifications),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        tint = ColoresTorque.OnSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, ColoresTorque.Outline.copy(alpha = 0.5f), CircleShape)
                        .clickable { mostrarMenuAjustes = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        tint = ColoresTorque.OnSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = mostrarMenuAjustes,
                    onDismissRequest = { mostrarMenuAjustes = false },
                    modifier = Modifier.background(ColoresTorque.SurfaceDark)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.my_profile), color = Color.White) },
                        onClick = {
                            mostrarMenuAjustes = false
                            onIrPerfil()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings_title), color = Color.White) },
                        onClick = {
                            mostrarMenuAjustes = false
                            contexto.startActivity(Intent(contexto, AjustesActivity::class.java))
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }

    if (mostrarAcercaDe) {
        AlertDialog(
            onDismissRequest = { mostrarAcercaDe = false },
            containerColor = ColoresTorque.SurfaceDark,
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = ColoresTorque.Primary) },
            title = { Text(stringResource(R.string.about_dialog_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.about_dialog_description), color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.about_dialog_tech), color = ColoresTorque.OnSurfaceVariant, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarAcercaDe = false }) {
                    Text(stringResource(R.string.btn_close), color = ColoresTorque.Primary)
                }
            }
        )
    }

    if (mostrarExportar) {
        AlertDialog(
            onDismissRequest = { mostrarExportar = false },
            containerColor = ColoresTorque.SurfaceDark,
            icon = { Icon(Icons.Default.Upload, contentDescription = null, tint = ColoresTorque.Primary) },
            title = { Text(stringResource(R.string.menu_export_data)) },
            text = { Text(stringResource(R.string.export_dialog_message), color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarExportar = false
                    scopeExportar.launch {
                        exportarBaseDeDatos(contexto)
                    }
                }) {
                    Text(stringResource(R.string.btn_export), color = ColoresTorque.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarExportar = false }) {
                    Text(stringResource(R.string.btn_cancel), color = ColoresTorque.OnSurfaceVariant)
                }
            }
        )
    }
}

private suspend fun exportarBaseDeDatos(contexto: Context) {
    try {
        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        val zipFile = java.io.File(downloadsDir, "Torque_backup_${System.currentTimeMillis()}.zip")
        val uri = android.net.Uri.fromFile(zipFile)
        val resultado = BackupManager.exportar(contexto, uri)
        if (resultado.isSuccess) {
            Toast.makeText(contexto, contexto.getString(R.string.toast_backup_saved), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(contexto, "Error al exportar: ${resultado.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(contexto, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
