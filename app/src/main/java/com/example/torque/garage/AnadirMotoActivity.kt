package com.example.torque.garage

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.example.torque.base.BaseActivity
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
import com.example.torque.ui.dashboard.BotonTorque
import com.example.torque.ui.dashboard.ColoresTorque
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.MotoEntity
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.ArchivoUtils
import com.example.torque.utils.DateUtils
import com.example.torque.utils.TipoVehiculo
import com.example.torque.utils.terminar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AnadirMotoActivity : BaseActivity() {
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

internal suspend fun guardarMoto(
    editando: Boolean, idMoto: Long, contexto: android.content.Context,
    repositorio: RepositorioApp, marca: String, modelo: String, anio: String,
    matricula: String, kilometros: String, cilindrada: String, caballos: String,
    estilo: String, uriImagen: android.net.Uri?, fotoActual: String?,
    esPrincipal: Boolean, fechaItv: String, fechaSeguro: String,
    tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO,
    tipoCarroceria: String = "", tipoCombustible: String = "",
    tipoTransmision: String = "", numeroPuertas: String = ""
) {
    val fecha = SimpleDateFormat(DateUtils.FORMATO_FECHA_COMPLETA, Locale.getDefault()).format(Date())
    val rutaFoto = uriImagen?.let { ArchivoUtils.copiarImagenAInterno(contexto, it) } ?: fotoActual

    if (editando && idMoto != -1L) {
        val motoExistente = repositorio.obtenerMotoPorId(idMoto) ?: return
        repositorio.actualizarMoto(
            motoExistente.copy(
                marca = marca.trim(), modelo = modelo.trim(), anio = anio.toIntOrNull() ?: 0,
                matricula = matricula.trim(), kilometros = kilometros.toIntOrNull() ?: 0,
                cilindrada = cilindrada.trim(), caballos = caballos.trim().ifEmpty { null },
                estilo = estilo.trim(), uriFoto = rutaFoto,
                fechaItv = fechaItv.trim().ifEmpty { null }, fechaSeguro = fechaSeguro.trim().ifEmpty { null },
                tipoVehiculo = tipoVehiculo,
                tipoCarroceria = tipoCarroceria.trim().ifEmpty { null },
                tipoCombustible = tipoCombustible.trim().ifEmpty { null },
                tipoTransmision = tipoTransmision.trim().ifEmpty { null },
                numeroPuertas = numeroPuertas.toIntOrNull(),
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
    } else {
        val numMotos = repositorio.contarMotos()
        val marcarPrincipal = esPrincipal || numMotos == 0
        val nuevaMoto = MotoEntity(
            marca = marca.trim(), modelo = modelo.trim(), anio = anio.toIntOrNull() ?: 0,
            matricula = matricula.trim(), kilometros = kilometros.toIntOrNull() ?: 0,
            cilindrada = cilindrada.trim(), caballos = caballos.trim().ifEmpty { null },
            estilo = estilo.trim(), esPrincipal = 0, uriFoto = rutaFoto,
            fechaItv = fechaItv.trim().ifEmpty { null }, fechaSeguro = fechaSeguro.trim().ifEmpty { null },
            tipoVehiculo = tipoVehiculo,
            tipoCarroceria = tipoCarroceria.trim().ifEmpty { null },
            tipoCombustible = tipoCombustible.trim().ifEmpty { null },
            tipoTransmision = tipoTransmision.trim().ifEmpty { null },
            numeroPuertas = numeroPuertas.toIntOrNull(),
            creadoEn = fecha, actualizadoEn = fecha
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

@Composable
private fun CabeceraAnadirMoto(editando: Boolean, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ColoresTorque.ALTURA_CABECERA)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
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
}

@Composable
private fun DialogoDescartarMoto(mostrar: Boolean, onDescartar: () -> Unit, onCancelar: () -> Unit) {
    if (mostrar) {
        AlertDialog(
            onDismissRequest = onCancelar,
            containerColor = ColoresTorque.SurfaceDark,
            title = { Text(stringResource(R.string.dialog_discard_title)) },
            text = { Text(stringResource(R.string.dialog_discard_message)) },
            confirmButton = {
                TextButton(onClick = onDescartar) {
                    Text(stringResource(R.string.btn_discard), color = ColoresTorque.RojoError)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelar) {
                    Text(stringResource(R.string.btn_keep_editing), color = ColoresTorque.OnSurfaceVariant)
                }
            }
        )
    }
}

private fun hayCambiosEnFormulario(
    marca: String, modelo: String, anio: String, matricula: String,
    cilindrada: String, caballos: String, estilo: String, kilometros: String,
    uriImagen: android.net.Uri?, esPrincipal: Boolean, fechaItv: String, fechaSeguro: String,
    tipoCarroceria: String = "", tipoCombustible: String = "",
    tipoTransmision: String = "", numeroPuertas: String = ""
): Boolean {
    return marca.isNotBlank() || modelo.isNotBlank() || anio.isNotBlank() ||
        matricula.isNotBlank() || cilindrada.isNotBlank() || caballos.isNotBlank() ||
        estilo.isNotBlank() || kilometros.isNotBlank() || uriImagen != null || esPrincipal ||
        fechaItv.isNotBlank() || fechaSeguro.isNotBlank() ||
        tipoCarroceria.isNotBlank() || tipoCombustible.isNotBlank() ||
        tipoTransmision.isNotBlank() || numeroPuertas.isNotBlank()
}

private data class DatosMotoCarga(
    val marca: String, val modelo: String, val anio: String, val matricula: String,
    val cilindrada: String, val caballos: String, val estilo: String, val kilometros: String,
    val fotoActual: String?, val esPrincipal: Boolean, val fechaItv: String, val fechaSeguro: String,
    val tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO,
    val tipoCarroceria: String = "", val tipoCombustible: String = "",
    val tipoTransmision: String = "", val numeroPuertas: String = ""
)

private suspend fun cargarDatosMoto(repositorio: RepositorioApp, idMoto: Long, onDatos: (DatosMotoCarga) -> Unit) {
    val moto = repositorio.obtenerMotoPorId(idMoto)
    if (moto != null) {
        onDatos(DatosMotoCarga(
            marca = moto.marca, modelo = moto.modelo,
            anio = if (moto.anio > 0) moto.anio.toString() else "",
            matricula = moto.matricula, cilindrada = moto.cilindrada ?: "",
            caballos = moto.caballos ?: "", estilo = moto.estilo ?: "",
            kilometros = moto.kilometros.toString(), fotoActual = moto.uriFoto,
            esPrincipal = moto.esPrincipal == 1, fechaItv = moto.fechaItv ?: "",
            fechaSeguro = moto.fechaSeguro ?: "",
            tipoVehiculo = moto.tipoVehiculo,
            tipoCarroceria = moto.tipoCarroceria ?: "",
            tipoCombustible = moto.tipoCombustible ?: "",
            tipoTransmision = moto.tipoTransmision ?: "",
            numeroPuertas = moto.numeroPuertas?.toString() ?: ""
        ))
    }
}

@Composable
fun VistaAnadirMoto(idMoto: Long = -1L, editando: Boolean = false) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioApp.obtenerInstancia(contexto) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var tipoVehiculo by remember { mutableStateOf(TipoVehiculo.MOTO) }
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
    var fechaItv by remember { mutableStateOf("") }
    var fechaSeguro by remember { mutableStateOf("") }
    var tipoCarroceria by remember { mutableStateOf("") }
    var tipoCombustible by remember { mutableStateOf("") }
    var tipoTransmision by remember { mutableStateOf("") }
    var numeroPuertas by remember { mutableStateOf("") }

    val hayCambios = hayCambiosEnFormulario(
        marca, modelo, anio, matricula, cilindrada, caballos, estilo,
        kilometros, uriImagen, esPrincipal, fechaItv, fechaSeguro,
        tipoCarroceria, tipoCombustible, tipoTransmision, numeroPuertas
    )

    LaunchedEffect(idMoto) {
        if (editando && idMoto != -1L) {
            cargarDatosMoto(repositorio, idMoto) { datos ->
                tipoVehiculo = datos.tipoVehiculo
                marca = datos.marca
                modelo = datos.modelo
                anio = datos.anio
                matricula = datos.matricula
                cilindrada = datos.cilindrada
                caballos = datos.caballos
                estilo = datos.estilo
                kilometros = datos.kilometros
                fotoActual = datos.fotoActual
                esPrincipal = datos.esPrincipal
                fechaItv = datos.fechaItv
                fechaSeguro = datos.fechaSeguro
                tipoCarroceria = datos.tipoCarroceria
                tipoCombustible = datos.tipoCombustible
                tipoTransmision = datos.tipoTransmision
                numeroPuertas = datos.numeroPuertas
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
            CabeceraAnadirMoto(
                editando = editando,
                onBack = {
                    if (hayCambios && !editando) mostrarDialogoDescartar = true
                    else (contexto as? Activity)?.terminar()
                }
            )
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColoresTorque.Primary)
            }
        } else {
            FormularioMotoBody(
                modifier = Modifier.weight(1f),
                tipoVehiculo = tipoVehiculo,
                onTipoVehiculoChange = { tipoVehiculo = it; marca = ""; modelo = "" },
                marca = marca,
                onMarcaChange = { marca = it; errorMarca = false },
                onMarcaSeleccionada = { modelo = "" },
                onMarcaDropdown = { errorMarca = false },
                errorMarca = errorMarca,
                modelo = modelo,
                onModeloChange = { modelo = it; errorModelo = false },
                errorModelo = errorModelo,
                anio = anio,
                onAnioChange = { anio = it },
                matricula = matricula,
                onMatriculaChange = { matricula = it; errorMatricula = false },
                errorMatricula = errorMatricula,
                kilometros = kilometros,
                onKilometrosChange = { kilometros = it },
                cilindrada = cilindrada,
                onCilindradaChange = { cilindrada = it },
                esPrincipal = esPrincipal,
                onEsPrincipalChange = { esPrincipal = it },
                fechaItv = fechaItv,
                onFechaItvChange = { fechaItv = it },
                fechaSeguro = fechaSeguro,
                onFechaSeguroChange = { fechaSeguro = it },
                estilo = estilo,
                onEstiloChange = { estilo = it },
                uriImagen = uriImagen,
                fotoActual = fotoActual,
                scrollState = scrollState,
                selectorImagen = selectorImagen,
                tipoCarroceria = tipoCarroceria,
                onTipoCarroceriaChange = { tipoCarroceria = it },
                tipoCombustible = tipoCombustible,
                onTipoCombustibleChange = { tipoCombustible = it },
                tipoTransmision = tipoTransmision,
                onTipoTransmisionChange = { tipoTransmision = it },
                numeroPuertas = numeroPuertas,
                onNumeroPuertasChange = { numeroPuertas = it }
            )
        }
        }

        DialogoDescartarMoto(
            mostrar = mostrarDialogoDescartar,
            onDescartar = {
                mostrarDialogoDescartar = false
                (contexto as? Activity)?.terminar()
            },
            onCancelar = { mostrarDialogoDescartar = false }
        )

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
                        guardarMoto(
                            editando, idMoto, contexto, repositorio,
                            marca, modelo, anio, matricula, kilometros,
                            cilindrada, caballos, estilo, uriImagen,
                            fotoActual, esPrincipal, fechaItv, fechaSeguro,
                            tipoVehiculo, tipoCarroceria, tipoCombustible,
                            tipoTransmision, numeroPuertas
                        )
                    }
                }
            )
        }
    }
}
