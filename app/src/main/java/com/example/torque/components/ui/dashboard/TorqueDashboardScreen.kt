package com.example.torque.components.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.torque.R
import com.example.torque.ajustes.AjustesActivity
import com.example.torque.ajustes.dataStore
import com.example.torque.garage.AnadirMotoActivity
import com.example.torque.perfil.PerfilRepositorio
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.FotoEntity
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.room.entity.MotoEntity
import com.example.torque.utils.NotificacionSalud
import com.example.torque.utils.ResultadoSalud
import com.example.torque.utils.SaludMotoCalculator
import com.example.torque.utils.TiposMantenimiento
import com.example.torque.utils.DateUtils
import com.example.torque.utils.TipoMantenimiento
import kotlinx.coroutines.launch

@Composable
fun TorqueDashboardScreen(
    onIrGaraje: () -> Unit = {},
    onIrMantenimientos: (Long?) -> Unit = {},
    onIrPerfil: () -> Unit = {}
) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }
    val motoPrincipal by repositorio.obtenerMotoPrincipalFlow().collectAsState(initial = null)
    var fotosPrincipales by remember { mutableStateOf<List<FotoEntity>>(emptyList()) }
    var ultimoMantenimiento by remember { mutableStateOf<MantenimientoEntity?>(null) }
    var resultadoSalud by remember { mutableStateOf<ResultadoSalud?>(null) }
    var mostrarDialogoDesgaste by remember { mutableStateOf(false) }
    var mostrarDialogoKm by remember { mutableStateOf(false) }
    var kmEditText by remember { mutableStateOf("") }
    var mostrarDialogoNotificaciones by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val perfilRepositorio = remember { PerfilRepositorio(contexto.dataStore) }
    val nombrePerfil by perfilRepositorio.nombreFlow.collectAsState(initial = null)

    val launcherPermisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> Unit }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                launcherPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(motoPrincipal?.idMoto) {
        if (motoPrincipal != null) {
            ultimoMantenimiento = repositorio.obtenerUltimoMantenimiento(motoPrincipal!!.idMoto)
            repositorio.obtenerFotosPorMoto(motoPrincipal!!.idMoto).collect { fotosPrincipales = it }
        } else {
            ultimoMantenimiento = null
            fotosPrincipales = emptyList()
            resultadoSalud = null
        }
    }

    LaunchedEffect(motoPrincipal?.idMoto) {
        if (motoPrincipal != null) {
            repositorio.obtenerMantenimientosPorMoto(motoPrincipal!!.idMoto).collect { mantenimientos ->
                val salud = SaludMotoCalculator.calcular(mantenimientos, motoPrincipal!!.kilometros)
                resultadoSalud = salud
                val nombreMoto = motoPrincipal?.modelo ?: "Tu moto"
                NotificacionSalud.verificarYNotificar(contexto, salud, motoPrincipal!!.kilometros, nombreMoto)
            }
        } else {
            resultadoSalud = null
        }
    }

    val kmRestantes = remember(motoPrincipal, ultimoMantenimiento) {
        if (motoPrincipal != null && ultimoMantenimiento != null && ultimoMantenimiento!!.intervaloKms > 0) {
            val proxServicio = ultimoMantenimiento!!.kms + ultimoMantenimiento!!.intervaloKms
            val restante = proxServicio - motoPrincipal!!.kilometros
            if (restante < 0) -1 else restante
        } else null
    }

    val progreso = remember(motoPrincipal, ultimoMantenimiento, kmRestantes) {
        if (motoPrincipal != null && ultimoMantenimiento != null && ultimoMantenimiento!!.intervaloKms > 0 && kmRestantes != null) {
            val transcurrido = motoPrincipal!!.kilometros - ultimoMantenimiento!!.kms
            val total = ultimoMantenimiento!!.intervaloKms
            if (total > 0) (transcurrido.toFloat() / total).coerceIn(0f, 1f) else 0f
        } else 0f
    }

    val notificacionesPendientes = remember(resultadoSalud) {
        resultadoSalud?.desglose?.count { it.cumplimiento <= 20 && !it.tipo.esDesgaste } ?: 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(ColoresTorque.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
        ) {
            CabeceraDashboard(
                onIrPerfil = onIrPerfil,
                notificacionesPendientes = notificacionesPendientes,
                onNotificacionesClick = { mostrarDialogoNotificaciones = true }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), // Reducido el espacio entre tarjetas
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = stringResource(id = R.string.welcome_prefix),
                            color = ColoresTorque.OnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (nombrePerfil.isNullOrBlank()) stringResource(id = R.string.welcome_name) else nombrePerfil!!,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColoresTorque.SurfaceContainer)
                                .clickable {
                                    if (motoPrincipal != null) {
                                        kmEditText = motoPrincipal!!.kilometros.toString()
                                        mostrarDialogoKm = true
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.km_current_label),
                                    color = ColoresTorque.Tertiary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = if (motoPrincipal != null) "${motoPrincipal!!.kilometros}" else "—",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.km_tap_to_edit),
                                    color = ColoresTorque.OnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = ColoresTorque.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    val fotoPortada = if (fotosPrincipales.isNotEmpty()) fotosPrincipales.last().ruta else motoPrincipal?.uriFoto
                    TarjetaProximoServicio(moto = motoPrincipal, fotoPortada = fotoPortada, kmRestantes = kmRestantes, progreso = progreso, resultadoSalud = resultadoSalud, onClick = { onIrMantenimientos(motoPrincipal?.idMoto) })
                }

                item {
                    Column {
                        Text(
                            text = stringResource(id = R.string.quick_actions_title),
                            color = ColoresTorque.OnSurfaceVariant,
                            fontSize = 10.sp, // Reducido
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TarjetaAccionRapida(
                                titulo = stringResource(id = R.string.garage_title),
                                subtitulo = stringResource(id = R.string.garage_subtitle),
                                icono = Icons.Default.Motorcycle,
                                colorContenedor = ColoresTorque.SecondaryContainer,
                                colorContenido = ColoresTorque.OnSecondaryContainer,
                                modifier = Modifier.weight(1f),
                                onClick = onIrGaraje
                            )
                            TarjetaAccionRapida(
                                titulo = stringResource(R.string.menu_wear),
                                subtitulo = stringResource(R.string.menu_wear_subtitle),
                                icono = Icons.Default.Build,
                                colorContenedor = ColoresTorque.Secondary,
                                colorContenido = ColoresTorque.OnSecondary,
                                modifier = Modifier.weight(1f),
                                onClick = { mostrarDialogoDesgaste = true }
                            )
                        }
                    }
                }

            }
        }

        FloatingActionButton(
            onClick = {
                contexto.startActivity(Intent(contexto, AnadirMotoActivity::class.java))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp), // Ajustada la posición del FAB
            containerColor = ColoresTorque.PrimaryContainer,
            contentColor = ColoresTorque.OnPrimaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add))
        }

        if (mostrarDialogoDesgaste && motoPrincipal != null) {
            DialogoAjusteDesgaste(
                idMoto = motoPrincipal!!.idMoto,
                onDismiss = { mostrarDialogoDesgaste = false }
            )
        }

        if (mostrarDialogoKm && motoPrincipal != null) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoKm = false },
                containerColor = ColoresTorque.SurfaceDark,
                title = { Text(stringResource(R.string.km_edit_title), color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = kmEditText,
                        onValueChange = { kmEditText = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.field_kilometers), color = ColoresTorque.OnSurfaceVariant) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ColoresTorque.Primary,
                            unfocusedBorderColor = ColoresTorque.Outline,
                            cursorColor = ColoresTorque.Primary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = ColoresTorque.Primary,
                            unfocusedLabelColor = ColoresTorque.OnSurfaceVariant
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val km = kmEditText.toIntOrNull()
                        if (km != null && km >= 0) {
                            scope.launch {
                                repositorio.actualizarKilometros(motoPrincipal!!.idMoto, km)
                            }
                        }
                        mostrarDialogoKm = false
                    }) {
                        Text(stringResource(R.string.save), color = ColoresTorque.Primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoKm = false }) {
                        Text(stringResource(R.string.btn_cancel), color = ColoresTorque.OnSurfaceVariant)
                    }
                }
            )
        }

        if (mostrarDialogoNotificaciones) {
            val saludActual = resultadoSalud
            val itemsNotificacion = remember(saludActual) {
                saludActual?.desglose
                    ?.filter { it.cumplimiento <= 20 && !it.tipo.esDesgaste }
                    ?.sortedBy { it.cumplimiento }
                    ?: emptyList()
            }

            AlertDialog(
                onDismissRequest = { mostrarDialogoNotificaciones = false },
                containerColor = ColoresTorque.SurfaceDark,
                icon = {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = ColoresTorque.Primary)
                },
                title = {
                    Text(stringResource(R.string.notifications_title), color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    if (itemsNotificacion.isEmpty()) {
                        Text(
                            stringResource(R.string.no_notifications_pending),
                            color = ColoresTorque.OnSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsNotificacion.forEach { detalle ->
                                val vencido = detalle.cumplimiento == 0
                                val colorEstado = if (vencido) Color(0xFFEF4444) else Color(0xFFEAB308)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ColoresTorque.SurfaceContainer)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (vencido) Icons.Default.Warning else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = colorEstado,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = detalle.tipo.nombre,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (vencido) stringResource(R.string.notification_expired)
                                                   else stringResource(R.string.notification_soon),
                                            color = colorEstado,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Text(
                                        text = "${detalle.cumplimiento}%",
                                        color = colorEstado,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogoNotificaciones = false }) {
                        Text(stringResource(R.string.btn_close), color = ColoresTorque.Primary)
                    }
                }
            )
        }

    }
}

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
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
            Text(
                text = stringResource(R.string.app_name),
                color = ColoresTorque.OnSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
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
                                containerColor = Color(0xFFEF4444),
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
                    exportarBaseDeDatos(contexto)
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

private fun exportarBaseDeDatos(contexto: Context) {
    try {
        val dbPath = contexto.getDatabasePath("torque_room.db")
        if (dbPath.exists()) {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val backupFile = java.io.File(downloadsDir, "Torque_backup.db")
            dbPath.inputStream().use { input ->
                backupFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(contexto, contexto.getString(R.string.toast_backup_saved), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(contexto, "No se encontró la base de datos", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(contexto, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun TarjetaProximoServicio(moto: MotoEntity?, fotoPortada: String? = null, kmRestantes: Int? = null, progreso: Float = 0f, resultadoSalud: ResultadoSalud? = null, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)) // Reducido de 28.dp
            .background(ColoresTorque.SurfaceContainerHigh)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(140.dp) // Reducido de 160.dp
                    .background(ColoresTorque.SurfaceVariant)
            ) {
                if (fotoPortada != null) {
                    Image(
                        painter = rememberAsyncImagePainter(fotoPortada),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ColoresTorque.SurfaceContainerHigh
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .padding(12.dp) // Reducido de 16.dp
                        .clip(CircleShape)
                        .background(ColoresTorque.Primary)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.next_service_badge),
                        color = ColoresTorque.OnPrimary,
                        fontSize = 10.sp, // Reducido de 11.sp
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            Column(modifier = Modifier.padding(20.dp)) { // Reducido de 24.dp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = moto?.modelo ?: stringResource(R.string.no_bike),
                            color = Color.White,
                            fontSize = 20.sp, // Reducido de 24.sp
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (moto != null) stringResource(R.string.your_main_bike) else stringResource(R.string.add_a_bike),
                            color = ColoresTorque.OnSurfaceVariant,
                            fontSize = 12.sp, // Reducido de 14.sp
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (kmRestantes != null) {
                            Text(
                                text = if (kmRestantes < 0) stringResource(R.string.overdue) else kmRestantes.toString(),
                                color = if (kmRestantes < 0) Color(0xFFEF4444) else ColoresTorque.Primary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (kmRestantes < 0) stringResource(R.string.service_km) else stringResource(R.string.km_remaining),
                                color = ColoresTorque.Outline,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        } else {
                            Text(
                                text = "—",
                                color = ColoresTorque.OnSurfaceVariant,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = stringResource(R.string.no_data),
                                color = ColoresTorque.Outline,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
                val salud = resultadoSalud?.saludGlobal ?: 0
                val colorSalud = when {
                    salud >= 70 -> Color(0xFF22C55E)
                    salud >= 40 -> Color(0xFFEAB308)
                    else -> Color(0xFFEF4444)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.bike_health),
                            color = ColoresTorque.OnSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${salud}%",
                            color = colorSalud,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { salud / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = colorSalud,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                }
                Spacer(modifier = Modifier.height(16.dp))
                BotonTorque(
                    text = stringResource(R.string.btn_view_maintenance),
                    onClick = onClick,
                    modifier = Modifier.height(48.dp)
                )
            }
        }
    }
}

@Composable
fun TarjetaAccionRapida(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorContenedor: Color,
    colorContenido: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ColoresTorque.SurfaceContainer)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .heightIn(min = 100.dp)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorContenedor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icono,
                contentDescription = null,
                tint = colorContenido,
                modifier = Modifier.size(24.dp)
            )
        }
        Column {
            Text(text = titulo, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                text = subtitulo,
                color = ColoresTorque.OnSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TarjetaEstadisticas(moto: MotoEntity?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColoresTorque.SurfaceContainer)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.total_mileage),
                color = ColoresTorque.Tertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ColoresTorque.Outline)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = if (moto != null) moto.kilometros.toString() else "—",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.kilometers_label),
                color = ColoresTorque.OnSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun DialogoAjusteDesgaste(idMoto: Long, onDismiss: () -> Unit) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }
    val scope = rememberCoroutineScope()

    val wearTipos = remember { TiposMantenimiento.WEAR_TIPOS }
    var porcentajes by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(idMoto) {
        val mapa = mutableMapOf<Long, Int>()
        for (tipo in wearTipos) {
            val ultimo = repositorio.obtenerUltimoMantenimientoPorTipo(idMoto, tipo.id)
            mapa[tipo.id] = ultimo?.porcentajeVida ?: 100
        }
        porcentajes = mapa
        cargando = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColoresTorque.SurfaceDark,
        title = { Text(stringResource(R.string.wear_status_title), color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            if (cargando) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColoresTorque.Primary)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    wearTipos.forEach { tipo ->
                        val pct = porcentajes[tipo.id] ?: 100
                        val colorBarra = when {
                            pct >= 70 -> Color(0xFF22C55E)
                            pct >= 40 -> Color(0xFFEAB308)
                            else -> Color(0xFFEF4444)
                        }
                        Column {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tipo.nombre, color = Color.White, fontSize = 13.sp)
                                Text("$pct%", color = colorBarra, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = (porcentajes[tipo.id] ?: 100).toFloat(),
                                onValueChange = { nuevo ->
                                    porcentajes = porcentajes + (tipo.id to nuevo.toInt())
                                },
                                valueRange = 0f..100f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = colorBarra,
                                    activeTrackColor = colorBarra,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    for (tipo in wearTipos) {
                        val pct = porcentajes[tipo.id] ?: 100
                        val ultimo = repositorio.obtenerUltimoMantenimientoPorTipo(idMoto, tipo.id)
                        if (ultimo != null) {
                            repositorio.actualizarPorcentajeVida(ultimo.id, pct)
                        } else {
                            val nuevo = MantenimientoEntity(
                                idMoto = idMoto,
                                nombreMantenimiento = tipo.nombre,
                                fecha = java.text.SimpleDateFormat(DateUtils.FORMATO_FECHA_VISUAL, java.util.Locale.getDefault()).format(java.util.Date()),
                                kms = 0,
                                intervaloKms = 0,
                                precio = "",
                                tiempo = "",
                                notas = "Ajuste de desgaste",
                                tareas = tipo.nombre,
                                creadoEn = java.text.SimpleDateFormat(DateUtils.FORMATO_FECHA_COMPLETA, java.util.Locale.getDefault()).format(java.util.Date()),
                                tipoMantenimientoId = tipo.id,
                                porcentajeVida = pct
                            )
                            repositorio.insertarMantenimiento(nuevo)
                        }
                    }
                    onDismiss()
                }
            }) {
                Text(stringResource(R.string.save), color = ColoresTorque.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = ColoresTorque.OnSurfaceVariant)
            }
        }
    )
}

