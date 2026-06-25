package com.example.torque.perfil

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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.rememberAsyncImagePainter
import com.example.torque.ajustes.dataStore
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.components.ui.dashboard.CampoTextoTorque
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.utils.ArchivoUtils
import kotlinx.coroutines.launch

class PerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TorqueTheme {
                PerfilScreen(onBack = { finish() })
            }
        }
    }
}

@Composable
fun PerfilScreen(onBack: () -> Unit) {
    val contexto = LocalContext.current
    val repositorio = PerfilRepositorio(contexto.dataStore)
    val scope = rememberCoroutineScope()

    val nombre by repositorio.nombreFlow.collectAsState(initial = "")
    val email by repositorio.emailFlow.collectAsState(initial = "")
    val telefono by repositorio.telefonoFlow.collectAsState(initial = "")
    val fotoPerfil by repositorio.fotoFlow.collectAsState(initial = null)
    val fechaNacimiento by repositorio.fechaNacimientoFlow.collectAsState(initial = "")
    val carnetsGuardados by repositorio.carnetsFlow.collectAsState(initial = emptySet())

    var editando by remember { mutableStateOf(false) }
    var editNombre by remember { mutableStateOf(nombre ?: "") }
    var editEmail by remember { mutableStateOf(email ?: "") }
    var editTelefono by remember { mutableStateOf(telefono ?: "") }
    var editFecha by remember { mutableStateOf(fechaNacimiento ?: "") }
    var editCarnets by remember { mutableStateOf(emptySet<String>()) }

    val carnetsDisponibles = remember { listOf("AM", "A1", "A2", "A") }

    LaunchedEffect(nombre, email, telefono, fechaNacimiento, carnetsGuardados) {
        editNombre = nombre ?: ""
        editEmail = email ?: ""
        editTelefono = telefono ?: ""
        editFecha = fechaNacimiento ?: ""
        editCarnets = carnetsGuardados
    }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val ruta = ArchivoUtils.copiarImagenAInterno(contexto, it)
            if (ruta != null) {
                scope.launch { repositorio.establecerFoto(ruta) }
            }
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
                    text = "Mi Perfil",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    if (editando) {
                        scope.launch {
                            repositorio.establecerNombre(editNombre.trim())
                            repositorio.establecerEmail(editEmail.trim())
                            repositorio.establecerTelefono(editTelefono.trim())
                            repositorio.establecerFechaNacimientoIso(editFecha.trim())
                            repositorio.establecerCarnets(editCarnets)
                        }
                        Toast.makeText(contexto, "Perfil guardado", Toast.LENGTH_SHORT).show()
                    }
                    editando = !editando
                }) {
                    Icon(
                        if (editando) Icons.Default.Save else Icons.Default.Edit,
                        contentDescription = if (editando) "Guardar" else "Editar",
                        tint = ColoresTorque.Primary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(ColoresTorque.SurfaceDark)
                        .border(2.dp, ColoresTorque.Primary, CircleShape)
                        .clickable { if (editando) selectorImagen.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoPerfil != null) {
                        Image(
                            painter = rememberAsyncImagePainter(fotoPerfil),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = ColoresTorque.OnSurfaceVariant
                        )
                    }
                    if (editando) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ColoresTorque.Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (editando) {
                        CampoTextoTorque(value = editNombre, onValueChange = { editNombre = it }, label = "Nombre")
                        CampoTextoTorque(value = editEmail, onValueChange = { editEmail = it }, label = "Correo electrónico")
                        CampoTextoTorque(value = editTelefono, onValueChange = { editTelefono = it }, label = "Teléfono")
                        CampoTextoTorque(value = editFecha, onValueChange = { editFecha = it }, label = "Fecha nacimiento (YYYY-MM-DD)")
                    } else {
                        if (nombre.isNullOrBlank().not()) {
                            Text(text = nombre!!, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (email.isNullOrBlank().not()) {
                            FilaPerfil(Icons.Default.Email, email!!)
                        }
                        if (telefono.isNullOrBlank().not()) {
                            FilaPerfil(Icons.Default.Phone, telefono!!)
                        }
                        if (fechaNacimiento.isNullOrBlank().not()) {
                            FilaPerfil(Icons.Default.CalendarMonth, fechaNacimiento!!)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "MIS CARNETS",
                        color = ColoresTorque.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val carnetsMostrar = if (editando) carnetsDisponibles else carnetsDisponibles.filter { it in carnetsGuardados }
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        carnetsMostrar.forEach { carnet ->
                            val seleccionado = carnet in editCarnets
                            val bgColor = if (editando && seleccionado) ColoresTorque.Primary else ColoresTorque.SurfaceDark
                            val borderColor = if (editando && seleccionado) Color.Transparent else ColoresTorque.BorderDark
                            val textoColor = if (editando && seleccionado) Color.White else Color.White
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable(enabled = editando) {
                                        editCarnets = if (seleccionado) editCarnets - carnet else editCarnets + carnet
                                    }
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = if (editando && seleccionado) Color.White else ColoresTorque.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = carnet, color = textoColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilaPerfil(icono: ImageVector, texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColoresTorque.SurfaceDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icono, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(text = texto, color = Color.White, fontSize = 16.sp)
    }
}
