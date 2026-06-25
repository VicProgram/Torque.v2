package com.example.torque.garage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

import com.example.torque.R
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.components.ui.dashboard.FiltroChip
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.MotoEntity
import com.example.torque.ui.theme.TorqueTheme
import android.content.Intent
import com.example.torque.garage.AnadirMotoActivity

class ListaGaraje : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TorqueTheme {
                VistaListaGaraje()
            }
        }
    }
}

@Composable
fun VistaListaGaraje() {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }
    val motos by repositorio.obtenerMotosFlow().collectAsState(initial = emptyList())

    var textoBusqueda by remember { mutableStateOf("") }
    var filtroActivo by remember { mutableStateOf("Todas") }
    var mostrarBusqueda by remember { mutableStateOf(false) }

    val motosFiltradas = motos.filter { moto ->
        val coincideBusqueda = textoBusqueda.isBlank() ||
            moto.marca.contains(textoBusqueda, ignoreCase = true) ||
            moto.modelo.contains(textoBusqueda, ignoreCase = true)
        val coincideFiltro = when (filtroActivo) {
            "En Uso" -> moto.esPrincipal == 1
            "Históricas" -> moto.esPrincipal == 0
            else -> true
        }
        coincideBusqueda && coincideFiltro
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(ColoresTorque.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CabeceraGaraje(
                onSearchToggle = { mostrarBusqueda = !mostrarBusqueda },
                mostrandoBusqueda = mostrarBusqueda
            )

            if (mostrarBusqueda) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_by_brand_hint), color = ColoresTorque.OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant) },
                    trailingIcon = {
                        if (textoBusqueda.isNotEmpty()) {
                            IconButton(onClick = { textoBusqueda = "" }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.content_desc_clear), tint = ColoresTorque.OnSurfaceVariant)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ColoresTorque.Primary,
                        unfocusedBorderColor = ColoresTorque.BorderDark,
                        cursorColor = ColoresTorque.Primary,
                        focusedContainerColor = ColoresTorque.SurfaceDark,
                        unfocusedContainerColor = ColoresTorque.SurfaceDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    LazyRow(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { FiltroChip("Todas", estaSeleccionado = filtroActivo == "Todas", onClick = { filtroActivo = "Todas" }) }
                        item { FiltroChip("En Uso", estaSeleccionado = filtroActivo == "En Uso", onClick = { filtroActivo = "En Uso" }) }
                        item { FiltroChip("Históricas", estaSeleccionado = filtroActivo == "Históricas", onClick = { filtroActivo = "Históricas" }) }
                    }
                }

                if (motos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Motorcycle,
                                    contentDescription = null,
                                    tint = ColoresTorque.OnSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    stringResource(R.string.no_bikes_yet),
                                    color = ColoresTorque.OnSurfaceVariant,
                                    fontSize = 18.sp
                                )
                                Text(
                                    stringResource(R.string.add_first_bike_hint),
                                    color = ColoresTorque.OnSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else if (motosFiltradas.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = ColoresTorque.OnSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    stringResource(R.string.no_results),
                                    color = ColoresTorque.OnSurfaceVariant,
                                    fontSize = 18.sp
                                )
                                Text(
                                    stringResource(R.string.try_different_filters),
                                    color = ColoresTorque.OnSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                items(motosFiltradas, key = { it.idMoto }) { moto ->
                    TarjetaMoto(
                        moto = moto,
                        onClick = {
                            val intent = Intent(contexto, MotoDetalle::class.java).apply {
                                putExtra("idMoto", moto.idMoto)
                            }
                            contexto.startActivity(intent)
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                contexto.startActivity(Intent(contexto, AnadirMotoActivity::class.java))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp), // Ajustado al nuevo padding
            containerColor = ColoresTorque.Primary,
            contentColor = ColoresTorque.OnPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.content_desc_new_moto),
                modifier = Modifier.size(24.dp)
            )
        }

    }
}

@Composable
fun CabeceraGaraje(onSearchToggle: () -> Unit = {}, mostrandoBusqueda: Boolean = false) {
    val contexto = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(ColoresTorque.Background.copy(alpha = 0.95f))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { (contexto as? android.app.Activity)?.finish() }) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = ColoresTorque.Primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = stringResource(R.string.my_garage_title),
            color = ColoresTorque.OnSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Row {
            IconButton(onClick = onSearchToggle) {
                Icon(
                    if (mostrandoBusqueda) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = stringResource(R.string.content_desc_search),
                    tint = if (mostrandoBusqueda) ColoresTorque.Primary else ColoresTorque.OnSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun TarjetaMoto(moto: MotoEntity, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColoresTorque.SurfaceContainer)
            .border(1.dp, ColoresTorque.Outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(ColoresTorque.SurfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            val foto = moto.uriFoto ?: ""
            if (foto.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(foto),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.9f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Motorcycle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = ColoresTorque.OnSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) { // Reducido de 16.dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = moto.modelo,
                    color = Color.White,
                    fontSize = 22.sp, // Mucho más compacto (era 32sp)
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp,
                    modifier = Modifier.weight(1f)
                )
                if (moto.esPrincipal == 1) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(R.string.content_desc_principal),
                        tint = ColoresTorque.Primary,
                        modifier = Modifier.size(20.dp) // Reducido de 28.dp
                    )
                }
            }
            Text(
                text = "${moto.marca} • ${moto.anio}",
                color = ColoresTorque.OnSurfaceVariant,
                fontSize = 14.sp, // Reducido de 24.sp
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF34D399)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.healthy_status),
                        color = Color(0xFF34D399),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.dp, ColoresTorque.Outline.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_view_details),
                        color = ColoresTorque.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
