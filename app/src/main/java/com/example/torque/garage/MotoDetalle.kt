package com.example.torque.garage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.example.torque.base.BaseActivity
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.maintenance.MantenimientoHistorial
import com.example.torque.maintenance.PlanMantenimiento
import com.example.torque.maintenance.RegistroMantenimiento
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.FotoEntity
import com.example.torque.room.entity.MotoEntity
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.ArchivoUtils
import com.example.torque.utils.DateUtils
import com.example.torque.utils.terminar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MotoDetalle : BaseActivity() {
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
private fun GaleriaMoto(fotos: SnapshotStateList<FotoEntity>, uriFotoInicial: String?, onAddPhoto: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(20.dp))
            .background(ColoresTorque.SurfaceDark)
            .clickable { onAddPhoto() }
    ) {
        val imagenAMostrar = if (fotos.isNotEmpty()) fotos.last().ruta else uriFotoInicial

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
}

@Composable
private fun TarjetaEspecificaciones(m: MotoEntity) {
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
        FilaDetalle(stringResource(R.string.label_brand), m.marca)
        FilaDetalle(stringResource(R.string.label_year), m.anio.toString())
        FilaDetalle(stringResource(R.string.label_plate), m.matricula ?: stringResource(R.string.not_available))
        FilaDetalle(stringResource(R.string.label_displacement), m.cilindrada ?: stringResource(R.string.not_available))
        val caballos = m.caballos
        FilaDetalle(
            stringResource(R.string.label_power),
            if (caballos != null) stringResource(R.string.power_cv_format, caballos) else stringResource(R.string.not_available)
        )
        FilaDetalle(stringResource(R.string.label_kilometers), "${m.kilometros} km", editable = true, onClick = { })
        FilaDetalle(stringResource(R.string.label_itv), m.fechaItv ?: stringResource(R.string.not_available))
        FilaDetalle(stringResource(R.string.label_insurance), m.fechaSeguro ?: stringResource(R.string.not_available))
        FilaDetalle(stringResource(R.string.label_style), m.estilo ?: stringResource(R.string.not_available))
    }
}

@Composable
fun VistaMotoDetalle(idMoto: Long) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }
    val scope = rememberCoroutineScope()
    
    var moto by remember { mutableStateOf<MotoEntity?>(null) }
    val fotos = remember { mutableStateListOf<FotoEntity>() }
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
            val m = requireNotNull(moto)
            Column(modifier = Modifier.fillMaxSize()) {
                CabeceraDetalleMoto(
                    modelo = m.modelo,
                    esPrincipal = m.esPrincipal == 1,
                    onBack = { (contexto as? Activity)?.terminar() },
                    onSetPrimary = {
                        scope.launch {
                            repositorio.establecerMotoPrincipal(idMoto)
                            moto = repositorio.obtenerMotoPorId(idMoto)
                            launch(Dispatchers.Main) {
                                Toast.makeText(contexto, contexto.getString(R.string.toast_primary_updated), Toast.LENGTH_SHORT).show()
                                (contexto as? Activity)?.setResult(Activity.RESULT_OK)
                            }
                        }
                    },
                    onRegisterMaintenance = {
                        contexto.startActivity(Intent(contexto, RegistroMantenimiento::class.java).apply {
                            putExtra("idMoto", idMoto)
                        })
                    },
                    onViewHistory = {
                        contexto.startActivity(Intent(contexto, MantenimientoHistorial::class.java).apply {
                            putExtra("idMoto", idMoto)
                        })
                    },
                    onPlanMaintenance = {
                        contexto.startActivity(Intent(contexto, PlanMantenimiento::class.java).apply {
                            putExtra("idMoto", idMoto)
                        })
                    },
                    onEditBike = {
                        contexto.startActivity(Intent(contexto, AnadirMotoActivity::class.java).apply {
                            putExtra("idMoto", idMoto)
                            putExtra("editando", true)
                        })
                    },
                    onDeleteBike = { mostrarDialogoEliminar = true }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(estadoScroll)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GaleriaMoto(
                        fotos = fotos,
                        uriFotoInicial = moto?.uriFoto,
                        onAddPhoto = { selectorImagen.launch("image/*") }
                    )

                    TarjetaEspecificaciones(m = m)

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

        moto?.let { m2 ->
            if (mostrarDialogoKm) {
                DialogoActualizarKm(
                    kmInicial = m2.kilometros,
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
                            repositorio.eliminarMoto(m2)
                            (contexto as? Activity)?.setResult(Activity.RESULT_OK)
                            (contexto as? Activity)?.terminar()
                        }
                        mostrarDialogoEliminar = false
                    },
                    onDismiss = { mostrarDialogoEliminar = false }
                )
            }
        }

        mostrarDialogoEliminarFoto?.let { foto ->
            DialogoEliminarFoto(
                onConfirmar = {
                    scope.launch {
                        repositorio.eliminarFoto(foto)
                        withContext(Dispatchers.IO) { File(foto.ruta).delete() }
                        Toast.makeText(contexto, contexto.getString(R.string.toast_photo_deleted), Toast.LENGTH_SHORT).show()
                    }
                    mostrarDialogoEliminarFoto = null
                },
                onDismiss = { mostrarDialogoEliminarFoto = null }
            )
        }
    }
}


