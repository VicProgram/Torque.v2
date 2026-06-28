package com.example.torque.garage

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.FotoEntity
import com.example.torque.room.entity.MotoEntity
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.ArchivoUtils
import com.example.torque.utils.DateUtils
import com.example.torque.utils.terminar
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import androidx.compose.foundation.clickable
import com.example.torque.maintenance.MantenimientoHistorial
import com.example.torque.maintenance.RegistroMantenimiento

class MotoDetalle : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val idMoto = intent.getLongExtra("idMoto", -1L)
        setContent {
            TorqueTheme {
                VistaMotoDetalle(idMoto)
            }
        }
    }
}

@Composable
fun VistaMotoDetalle(idMoto: Long) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }
    val scope = rememberCoroutineScope()
    
    var moto by remember { mutableStateOf<MotoEntity?>(null) }
    val fotos = remember { mutableStateListOf<FotoEntity>() }
    var mostrarMenu by remember { mutableStateOf(false) }
    var mostrarDialogoKm by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoEliminarFoto by remember { mutableStateOf<FotoEntity?>(null) }
    val estadoScroll = rememberScrollState()

    LaunchedEffect(idMoto) {
        if (idMoto != -1L) {
            moto = repositorio.obtenerMotoPorId(idMoto)
            repositorio.obtenerFotosPorMoto(idMoto).collect { listaFotos ->
                fotos.clear()
                fotos.addAll(listaFotos)
            }
        }
    }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val ruta = ArchivoUtils.copiarImagenAInterno(contexto, it)
            if (ruta != null && idMoto != -1L) {
                scope.launch {
                    val nuevaFoto = FotoEntity(
                        idMoto = idMoto,
                        ruta = ruta,
                        creadoEn = SimpleDateFormat(DateUtils.FORMATO_FECHA_COMPLETA, Locale.getDefault()).format(Date())
                    )
                    repositorio.insertarFoto(nuevaFoto)
                    Toast.makeText(contexto, contexto.getString(R.string.toast_photo_added), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(ColoresTorque.Background)
    ) {
        if (moto == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColoresTorque.Primary)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- Cabecera (Compacta) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ColoresTorque.ALTURA_CABECERA)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { (contexto as? Activity)?.terminar() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back),
                        tint = ColoresTorque.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = moto!!.modelo,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (moto!!.esPrincipal == 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = stringResource(R.string.content_desc_principal),
                            tint = ColoresTorque.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { mostrarMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.menu),
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = mostrarMenu,
                            onDismissRequest = { mostrarMenu = false },
                            modifier = Modifier.background(ColoresTorque.SurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_make_primary), color = Color.White) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = ColoresTorque.Primary
                                    )
                                },
                                onClick = {
                                    scope.launch {
                                        repositorio.establecerMotoPrincipal(idMoto)
                                        moto = repositorio.obtenerMotoPorId(idMoto)
                                        launch(kotlinx.coroutines.Dispatchers.Main) {
                                            Toast.makeText(contexto, contexto.getString(R.string.toast_primary_updated), Toast.LENGTH_SHORT).show()
                                            (contexto as? Activity)?.setResult(Activity.RESULT_OK)
                                        }
                                    }
                                    mostrarMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_register_maintenance), color = Color.White) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Build,
                                        contentDescription = null,
                                        tint = ColoresTorque.Primary
                                    )
                                },
                                onClick = {
                                    val intent = Intent(contexto, RegistroMantenimiento::class.java).apply {
                                        putExtra("idMoto", idMoto)
                                    }
                                    contexto.startActivity(intent)
                                    mostrarMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_view_history), color = Color.White) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = ColoresTorque.Primary
                                    )
                                },
                                onClick = {
                                    val intent = Intent(contexto, MantenimientoHistorial::class.java).apply {
                                        putExtra("idMoto", idMoto)
                                    }
                                    contexto.startActivity(intent)
                                    mostrarMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_edit_bike), color = Color.White) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = ColoresTorque.Primary
                                    )
                                },
                                onClick = {
                                    val intent = Intent(contexto, AnadirMotoActivity::class.java).apply {
                                        putExtra("idMoto", idMoto)
                                        putExtra("editando", true)
                                    }
                                    contexto.startActivity(intent)
                                    mostrarMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_delete_bike), color = Color.Red) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                },
                                onClick = {
                                    mostrarDialogoEliminar = true
                                    mostrarMenu = false
                                }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(estadoScroll)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- Galería de Imágenes ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f) // Un poco más horizontal
                            .clip(RoundedCornerShape(20.dp))
                            .background(ColoresTorque.SurfaceDark)
                            .clickable { selectorImagen.launch("image/*") }
                    ) {
                        val imagenAMostrar = if (fotos.isNotEmpty()) fotos.last().ruta else moto?.uriFoto
                        
                        if (!imagenAMostrar.isNullOrEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(imagenAMostrar),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = ColoresTorque.OnSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(stringResource(R.string.no_photos_yet), color = ColoresTorque.OnSurfaceVariant)
                            }
                        }
                    }

                    // --- Tarjeta de Información Básica ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ColoresTorque.SurfaceDark)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.section_specifications),
                            color = ColoresTorque.Primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        FilaDetalle(stringResource(R.string.label_brand), moto!!.marca)
                        FilaDetalle(stringResource(R.string.label_year), moto!!.anio.toString())
                        FilaDetalle(stringResource(R.string.label_plate), moto!!.matricula ?: stringResource(R.string.not_available))
                        FilaDetalle(stringResource(R.string.label_displacement), moto!!.cilindrada ?: stringResource(R.string.not_available))
                        FilaDetalle(stringResource(R.string.label_power), if (moto!!.caballos != null) stringResource(R.string.power_cv_format, moto!!.caballos!!) else stringResource(R.string.not_available))
                        FilaDetalle(stringResource(R.string.label_kilometers), "${moto!!.kilometros} km", editable = true, onClick = {
                            mostrarDialogoKm = true
                        })
                        FilaDetalle(stringResource(R.string.label_style), moto!!.estilo ?: stringResource(R.string.not_available))
                    }

                    // --- Fila de Fotos ---
                    if (fotos.size > 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                stringResource(R.string.section_gallery),
                                color = ColoresTorque.OnSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(fotos) { foto ->
                                    Image(
                                        painter = rememberAsyncImagePainter(foto.ruta),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                mostrarDialogoEliminarFoto = foto
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (mostrarDialogoKm) {
            DialogoActualizarKm(
                kmInicial = moto!!.kilometros,
                onGuardar = { km ->
                    scope.launch {
                        repositorio.actualizarKilometros(idMoto, km)
                        moto = repositorio.obtenerMotoPorId(idMoto)
                    }
                },
                onDismiss = { mostrarDialogoKm = false }
            )
        }

        if (mostrarDialogoEliminar) {
            DialogoEliminarMoto(
                onConfirmar = {
                    scope.launch {
                        repositorio.eliminarMoto(moto!!)
                        (contexto as? Activity)?.setResult(Activity.RESULT_OK)
                        (contexto as? Activity)?.terminar()
                    }
                    mostrarDialogoEliminar = false
                },
                onDismiss = { mostrarDialogoEliminar = false }
            )
        }

        mostrarDialogoEliminarFoto?.let { foto ->
            DialogoEliminarFoto(
                onConfirmar = {
                    scope.launch {
                        repositorio.eliminarFoto(foto)
                        File(foto.ruta).delete()
                        Toast.makeText(contexto, contexto.getString(R.string.toast_photo_deleted), Toast.LENGTH_SHORT).show()
                    }
                    mostrarDialogoEliminarFoto = null
                },
                onDismiss = { mostrarDialogoEliminarFoto = null }
            )
        }
    }
}

@Composable
fun FilaDetalle(etiqueta: String, valor: String?, editable: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = if (editable) Modifier
            .fillMaxWidth()
            .clickable { onClick() }
        else Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, color = ColoresTorque.OnSurfaceVariant, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = valor ?: stringResource(R.string.not_available),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (editable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Edit, contentDescription = null, tint = ColoresTorque.Primary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

