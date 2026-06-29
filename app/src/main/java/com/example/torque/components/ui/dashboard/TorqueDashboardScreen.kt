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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.torque.R
import com.example.torque.ajustes.AjustesActivity
import com.example.torque.garage.AnadirMotoActivity
import com.example.torque.room.entity.MotoEntity
import com.example.torque.utils.BackupManager
import com.example.torque.utils.NotificacionSalud
import com.example.torque.utils.ResultadoSalud
import kotlinx.coroutines.launch

@Composable
fun TorqueDashboardScreen(
    onIrGaraje: () -> Unit = {},
    onIrMantenimientos: (Long?) -> Unit = {},
    onIrPerfil: () -> Unit = {}
) {
    val contexto = LocalContext.current
    val viewModel: TorqueDashboardViewModel = viewModel(factory = TorqueDashboardViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()

    val motoActual = uiState.motos.getOrNull(uiState.selectedIndex)

    val pagerState = rememberPagerState(
        initialPage = uiState.selectedIndex,
        pageCount = { maxOf(uiState.motos.size, 1) }
    )

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != uiState.selectedIndex) {
            viewModel.selectMoto(pagerState.currentPage)
        }
    }

    LaunchedEffect(uiState.selectedIndex) {
        if (pagerState.currentPage != uiState.selectedIndex) {
            pagerState.animateScrollToPage(uiState.selectedIndex)
        }
    }

    var mostrarDialogoDesgaste by remember { mutableStateOf(false) }
    var mostrarDialogoKm by remember { mutableStateOf(false) }
    var mostrarDialogoNotificaciones by remember { mutableStateOf(false) }

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

    LaunchedEffect(uiState.resultadoSalud) {
        val salud = uiState.resultadoSalud
        val moto = motoActual
        if (salud != null && moto != null) {
            val nombreMoto = moto.modelo.ifBlank { "Tu moto" }
            NotificacionSalud.verificarYNotificar(contexto, salud, moto.kilometros, nombreMoto)
        }
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
                notificacionesPendientes = uiState.notificacionesPendientes,
                onNotificacionesClick = { mostrarDialogoNotificaciones = true }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
                            text = uiState.nombrePerfil ?: stringResource(id = R.string.welcome_name),
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
                                    if (motoActual != null) {
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
                                    text = motoActual?.kilometros?.toString() ?: "—",
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
                    if (uiState.motos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(ColoresTorque.SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Motorcycle, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(stringResource(R.string.no_bike), color = ColoresTorque.OnSurfaceVariant)
                            }
                        }
                    } else {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            val moto = uiState.motos[page]
                            val fotoPortada = if (uiState.fotos.isNotEmpty() && page == uiState.selectedIndex) {
                                uiState.fotos.last().ruta
                            } else {
                                moto.uriFoto
                            }
                            TarjetaProximoServicio(
                                moto = moto,
                                fotoPortada = fotoPortada,
                                kmRestantes = if (page == uiState.selectedIndex) uiState.kmRestantes else null,
                                progreso = if (page == uiState.selectedIndex) uiState.progreso else 0f,
                                resultadoSalud = if (page == uiState.selectedIndex) uiState.resultadoSalud else null,
                                onClick = { onIrMantenimientos(moto.idMoto) }
                            )
                        }

                        if (uiState.motos.size > 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(uiState.motos.size) { i ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .size(if (i == pagerState.currentPage) 10.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (i == pagerState.currentPage) ColoresTorque.Primary
                                                else ColoresTorque.OnSurfaceVariant.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = stringResource(id = R.string.quick_actions_title),
                            color = ColoresTorque.OnSurfaceVariant,
                            fontSize = 10.sp,
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
                .padding(end = 16.dp, bottom = 80.dp),
            containerColor = ColoresTorque.PrimaryContainer,
            contentColor = ColoresTorque.OnPrimaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add))
        }

        if (mostrarDialogoDesgaste && motoActual != null) {
            DialogoAjusteDesgaste(
                initialPorcentajes = uiState.porcentajesDesgaste,
                onDismiss = { mostrarDialogoDesgaste = false },
                onGuardar = { porcentajes ->
                    viewModel.guardarAjustesDesgaste(motoActual.idMoto, porcentajes) {
                        mostrarDialogoDesgaste = false
                    }
                }
            )
        }

        if (mostrarDialogoKm && motoActual != null) {
            DialogoKm(
                initialKm = motoActual.kilometros,
                onSave = { km -> viewModel.actualizarKilometros(km) },
                onDismiss = { mostrarDialogoKm = false }
            )
        }

        if (mostrarDialogoNotificaciones) {
            DialogoNotificaciones(
                resultadoSalud = uiState.resultadoSalud,
                onDismiss = { mostrarDialogoNotificaciones = false }
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

@Composable
fun TarjetaProximoServicio(
    moto: MotoEntity?,
    fotoPortada: String? = null,
    kmRestantes: Int? = null,
    progreso: Float = 0f,
    resultadoSalud: ResultadoSalud? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ColoresTorque.SurfaceContainerHigh)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(140.dp)
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
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(ColoresTorque.Primary)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.next_service_badge),
                        color = ColoresTorque.OnPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = moto?.modelo ?: stringResource(R.string.no_bike),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (moto != null) stringResource(R.string.your_main_bike) else stringResource(R.string.add_a_bike),
                            color = ColoresTorque.OnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (kmRestantes != null) {
                            Text(
                                text = if (kmRestantes < 0) stringResource(R.string.overdue) else kmRestantes.toString(),
                                color = if (kmRestantes < 0) ColoresTorque.RojoError else ColoresTorque.Primary,
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
                    salud >= 70 -> ColoresTorque.VerdeExito
                    salud >= 40 -> ColoresTorque.AmarilloAdvertencia
                    else -> ColoresTorque.RojoError
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
                    val progresoSalud = (salud.toFloat() / 100f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = progresoSalud)
                                .background(colorSalud)
                        )
                    }
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
