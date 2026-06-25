package com.example.torque.garage

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.torque.R
import com.example.torque.components.ui.dashboard.*
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.MotoEntity
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.ArchivoUtils
import com.example.torque.utils.DateUtils
import com.example.torque.utils.terminar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AnadirMotoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val idMoto = intent.getLongExtra("idMoto", -1L)
        val editando = intent.getBooleanExtra("editando", false)
        setContent {
            TorqueTheme {
                VistaAnadirMoto(idMoto = idMoto, editando = editando)
            }
        }
    }
}

@Composable
fun VistaAnadirMoto(idMoto: Long = -1L, editando: Boolean = false) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var cilindrada by remember { mutableStateOf("") }
    var caballos by remember { mutableStateOf("") }
    var estilo by remember { mutableStateOf("") }
    var kilometros by remember { mutableStateOf("") }
    var uriImagen by remember { mutableStateOf<android.net.Uri?>(null) }
    var fotoActual by remember { mutableStateOf<String?>(null) }
    var esPrincipal by remember { mutableStateOf(false) }
    var errorMarca by remember { mutableStateOf(false) }
    var errorModelo by remember { mutableStateOf(false) }
    var errorMatricula by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(editando) }
    var mostrarDialogoDescartar by remember { mutableStateOf(false) }

    val hayCambios = marca.isNotBlank() || modelo.isNotBlank() || anio.isNotBlank() ||
        matricula.isNotBlank() || cilindrada.isNotBlank() || caballos.isNotBlank() ||
        estilo.isNotBlank() || kilometros.isNotBlank() || uriImagen != null || esPrincipal

    LaunchedEffect(idMoto) {
        if (editando && idMoto != -1L) {
            val moto = repositorio.obtenerMotoPorId(idMoto)
            if (moto != null) {
                marca = moto.marca
                modelo = moto.modelo
                anio = if (moto.anio > 0) moto.anio.toString() else ""
                matricula = moto.matricula
                cilindrada = moto.cilindrada ?: ""
                caballos = moto.caballos ?: ""
                estilo = moto.estilo ?: ""
                kilometros = moto.kilometros.toString()
                fotoActual = moto.uriFoto
                esPrincipal = moto.esPrincipal == 1
            }
            cargando = false
        }
    }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uriImagen = uri
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
                IconButton(onClick = {
                    if (hayCambios && !editando) mostrarDialogoDescartar = true
                    else (contexto as? Activity)?.terminar()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back),
                        tint = ColoresTorque.Primary
                    )
                }
                Text(
                    text = if (editando) stringResource(R.string.title_edit_motorcycle) else stringResource(R.string.title_new_motorcycle),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColoresTorque.Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(ColoresTorque.SurfaceDark)
                        .clickable { selectorImagen.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val imagenAMostrar = uriImagen ?: fotoActual
                    if (imagenAMostrar != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imagenAMostrar),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(48.dp))
                            Text(stringResource(R.string.add_photo), color = ColoresTorque.OnSurfaceVariant)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CampoTextoTorque(marca, { marca = it; errorMarca = false }, stringResource(R.string.field_brand_required))
                    if (errorMarca) {
                        Text(stringResource(R.string.error_brand_required), color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                    CampoTextoTorque(modelo, { modelo = it; errorModelo = false }, stringResource(R.string.field_model_required))
                    if (errorModelo) {
                        Text(stringResource(R.string.error_model_required), color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CampoTextoTorque(anio, { anio = it }, stringResource(R.string.label_year), modifier = Modifier.weight(1f))
                        CampoTextoTorque(matricula, { matricula = it }, stringResource(R.string.label_plate), modifier = Modifier.weight(1f))
                        if (errorMatricula) {
                            Text("La matrícula es obligatoria", color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CampoTextoTorque(kilometros, { kilometros = it }, stringResource(R.string.field_current_kms_short), modifier = Modifier.weight(1f))
                        CampoTextoTorque(cilindrada, { cilindrada = it }, stringResource(R.string.label_displacement), modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.label_set_as_primary), color = Color.White, modifier = Modifier.weight(1f))
                        Switch(
                            checked = esPrincipal,
                            onCheckedChange = { esPrincipal = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ColoresTorque.Primary)
                        )
                    }

                    CampoTextoTorque(
                        value = estilo,
                        onValueChange = { estilo = it },
                        label = stringResource(R.string.field_notes),
                        singleLine = false,
                        modifier = Modifier.height(100.dp)
                    )
                }
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
        }

        if (mostrarDialogoDescartar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoDescartar = false },
                containerColor = ColoresTorque.SurfaceDark,
                title = { Text(stringResource(R.string.dialog_discard_title)) },
                text = { Text(stringResource(R.string.dialog_discard_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarDialogoDescartar = false
                        (contexto as? Activity)?.terminar()
                    }) {
                        Text(stringResource(R.string.btn_discard), color = Color(0xFFEF4444))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoDescartar = false }) {
                        Text(stringResource(R.string.btn_keep_editing), color = ColoresTorque.OnSurfaceVariant)
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            BotonTorque(
                text = if (editando) stringResource(R.string.btn_update_bike) else stringResource(R.string.save_motorcycle),
                icon = Icons.Default.Save,
                onClick = {
                    errorMarca = marca.isBlank()
                    errorModelo = modelo.isBlank()
                    errorMatricula = matricula.isBlank()

                    if (errorMarca || errorModelo || errorMatricula) {
                        Toast.makeText(contexto, contexto.getString(R.string.toast_fill_required), Toast.LENGTH_SHORT).show()
                        return@BotonTorque
                    }

                    scope.launch {
                        val fecha = SimpleDateFormat(DateUtils.FORMATO_FECHA_COMPLETA, Locale.getDefault()).format(Date())
                        val rutaFoto = uriImagen?.let { ArchivoUtils.copiarImagenAInterno(contexto, it) } ?: fotoActual

                        if (editando && idMoto != -1L) {
                            val motoExistente = repositorio.obtenerMotoPorId(idMoto)
                            if (motoExistente != null) {
                                repositorio.actualizarMoto(
                                    motoExistente.copy(
                                        marca = marca.trim(),
                                        modelo = modelo.trim(),
                                        anio = anio.toIntOrNull() ?: 0,
                                        matricula = matricula.trim(),
                                        kilometros = kilometros.toIntOrNull() ?: 0,
                                        cilindrada = cilindrada.trim(),
                                        caballos = caballos.trim().ifEmpty { null },
                                        estilo = estilo.trim(),
                                        uriFoto = rutaFoto,
                                        actualizadoEn = fecha
                                    )
                                )
                                if (esPrincipal && motoExistente.esPrincipal != 1) {
                                    repositorio.establecerMotoPrincipal(idMoto)
                                }
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(contexto, contexto.getString(R.string.toast_bike_updated), Toast.LENGTH_SHORT).show()
                                    (contexto as? Activity)?.terminar()
                                }
                            }
                        } else {
                            val numMotos = repositorio.contarMotos()
                            val marcarPrincipal = esPrincipal || numMotos == 0

                            val nuevaMoto = MotoEntity(
                                marca = marca.trim(),
                                modelo = modelo.trim(),
                                anio = anio.toIntOrNull() ?: 0,
                                matricula = matricula.trim(),
                                kilometros = kilometros.toIntOrNull() ?: 0,
                                cilindrada = cilindrada.trim(),
                                caballos = caballos.trim().ifEmpty { null },
                                estilo = estilo.trim(),
                                esPrincipal = 0,
                                uriFoto = rutaFoto,
                                creadoEn = fecha,
                                actualizadoEn = fecha
                            )

                            val id = repositorio.insertarMoto(nuevaMoto)

                            if (marcarPrincipal && id > 0) {
                                repositorio.establecerMotoPrincipal(id)
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(contexto, contexto.getString(R.string.toast_bike_added), Toast.LENGTH_SHORT).show()
                                (contexto as? Activity)?.terminar()
                            }
                        }
                    }
                }
            )
        }
    }
}
