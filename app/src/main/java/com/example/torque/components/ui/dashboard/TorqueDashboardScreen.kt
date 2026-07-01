package com.example.torque.components.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.torque.R
import com.example.torque.garage.AnadirMotoActivity
import com.example.torque.utils.NotificacionSalud

@OptIn(ExperimentalFoundationApi::class)
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
            val nombreMoto = moto.modelo.ifBlank { contexto.getString(R.string.your_main_bike) }
            NotificacionSalud.verificarYNotificar(contexto, salud, nombreMoto)
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
                    SeccionBienvenida(nombrePerfil = uiState.nombrePerfil, motoActual = motoActual, onEditarKm = { mostrarDialogoKm = true })
                }

                item {
                    SeccionMotoPager(
                        motos = uiState.motos,
                        fotos = uiState.fotos,
                        selectedIndex = uiState.selectedIndex,
                        kmRestantes = uiState.kmRestantes,
                        resultadoSalud = uiState.resultadoSalud,
                        pagerState = pagerState,
                        onIrMantenimientos = onIrMantenimientos
                    )
                }

                item {
                    SeccionAccionesRapidas(onIrGaraje = onIrGaraje, onMostrarDesgaste = { mostrarDialogoDesgaste = true })
                }

                if (motoActual != null && (uiState.itvProxima || uiState.seguroProximo)) {
                    item {
                        SeccionRecordatorios(itvProxima = uiState.itvProxima, seguroProximo = uiState.seguroProximo, fechaItv = uiState.fechaItv, fechaSeguro = uiState.fechaSeguro)
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
                    viewModel.guardarAjustesDesgaste(motoActual.idMoto, porcentajes, contexto.getString(R.string.wear_adjustment_note)) {
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
private fun SeccionBienvenida(nombrePerfil: String?, motoActual: com.example.torque.room.entity.MotoEntity?, onEditarKm: () -> Unit) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = stringResource(id = R.string.welcome_prefix),
            color = ColoresTorque.OnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = nombrePerfil ?: stringResource(id = R.string.welcome_name),
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
                    if (motoActual != null) onEditarKm()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SeccionMotoPager(
    motos: List<com.example.torque.room.entity.MotoEntity>,
    fotos: List<com.example.torque.room.entity.FotoEntity>,
    selectedIndex: Int,
    kmRestantes: Int?,
    resultadoSalud: com.example.torque.utils.ResultadoSalud?,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onIrMantenimientos: (Long?) -> Unit
) {
    if (motos.isEmpty()) {
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
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val moto = motos[page]
            val fotoPortada = if (fotos.isNotEmpty() && page == selectedIndex) {
                fotos.last().ruta
            } else {
                moto.uriFoto
            }
            TarjetaProximoServicio(
                moto = moto, fotoPortada = fotoPortada,
                kmRestantes = if (page == selectedIndex) kmRestantes else null,
                resultadoSalud = if (page == selectedIndex) resultadoSalud else null,
                onClick = { onIrMantenimientos(moto.idMoto) }
            )
        }
        if (motos.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(motos.size) { i ->
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

@Composable
private fun SeccionAccionesRapidas(onIrGaraje: () -> Unit, onMostrarDesgaste: () -> Unit) {
    Column {
        Text(
            text = stringResource(id = R.string.quick_actions_title),
            color = ColoresTorque.OnSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaAccionRapida(
                titulo = stringResource(id = R.string.garage_title), subtitulo = stringResource(id = R.string.garage_subtitle),
                icono = Icons.Default.Motorcycle, colorContenedor = ColoresTorque.SecondaryContainer,
                colorContenido = ColoresTorque.OnSecondaryContainer, modifier = Modifier.weight(1f), onClick = onIrGaraje
            )
            TarjetaAccionRapida(
                titulo = stringResource(R.string.menu_wear), subtitulo = stringResource(R.string.menu_wear_subtitle),
                icono = Icons.Default.Build, colorContenedor = ColoresTorque.Secondary,
                colorContenido = ColoresTorque.OnSecondary, modifier = Modifier.weight(1f), onClick = onMostrarDesgaste
            )
        }
    }
}

@Composable
private fun SeccionRecordatorios(itvProxima: Boolean, seguroProximo: Boolean, fechaItv: String?, fechaSeguro: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.reminders_section),
            color = ColoresTorque.AmarilloAdvertencia,
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
        if (itvProxima) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(ColoresTorque.AmarilloAdvertencia.copy(alpha = 0.15f)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = ColoresTorque.AmarilloAdvertencia, modifier = Modifier.size(24.dp))
                Column {
                    Text(stringResource(R.string.itv_soon), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.itv_expires, fechaItv ?: "-"), color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
        if (seguroProximo) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(ColoresTorque.AmarilloAdvertencia.copy(alpha = 0.15f)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = ColoresTorque.AmarilloAdvertencia, modifier = Modifier.size(24.dp))
                Column {
                    Text(stringResource(R.string.insurance_soon), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.insurance_expires, fechaSeguro ?: "-"), color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}
