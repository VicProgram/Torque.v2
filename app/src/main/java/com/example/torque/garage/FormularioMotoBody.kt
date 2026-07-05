package com.example.torque.garage

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.torque.R
import com.example.torque.ui.dashboard.CampoTextoTorque
import com.example.torque.ui.dashboard.ColoresTorque
import com.example.torque.utils.TipoVehiculo

@Composable
fun FormularioMotoBody(
    modifier: Modifier = Modifier,
    tipoVehiculo: TipoVehiculo = TipoVehiculo.MOTO,
    onTipoVehiculoChange: (TipoVehiculo) -> Unit = {},
    marca: String,
    onMarcaChange: (String) -> Unit,
    onMarcaSeleccionada: (String) -> Unit,
    onMarcaDropdown: () -> Unit,
    errorMarca: Boolean,
    modelo: String,
    onModeloChange: (String) -> Unit,
    errorModelo: Boolean,
    anio: String,
    onAnioChange: (String) -> Unit,
    matricula: String,
    onMatriculaChange: (String) -> Unit,
    errorMatricula: Boolean,
    kilometros: String,
    onKilometrosChange: (String) -> Unit,
    cilindrada: String,
    onCilindradaChange: (String) -> Unit,
    esPrincipal: Boolean,
    onEsPrincipalChange: (Boolean) -> Unit,
    fechaItv: String,
    onFechaItvChange: (String) -> Unit,
    fechaSeguro: String,
    onFechaSeguroChange: (String) -> Unit,
    estilo: String,
    onEstiloChange: (String) -> Unit,
    uriImagen: Uri?,
    fotoActual: String?,
    scrollState: androidx.compose.foundation.ScrollState,
    selectorImagen: androidx.activity.result.ActivityResultLauncher<String>,
    tipoCarroceria: String = "",
    onTipoCarroceriaChange: (String) -> Unit = {},
    tipoCombustible: String = "",
    onTipoCombustibleChange: (String) -> Unit = {},
    tipoTransmision: String = "",
    onTipoTransmisionChange: (String) -> Unit = {},
    numeroPuertas: String = "",
    onNumeroPuertasChange: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = tipoVehiculo == TipoVehiculo.MOTO,
                    onClick = { onTipoVehiculoChange(TipoVehiculo.MOTO) },
                    label = { Text("Moto") },
                    leadingIcon = { Icon(Icons.Default.Motorcycle, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColoresTorque.Primary,
                        selectedLabelColor = Color.Black
                    )
                )
                FilterChip(
                    selected = tipoVehiculo == TipoVehiculo.COCHE,
                    onClick = { onTipoVehiculoChange(TipoVehiculo.COCHE) },
                    label = { Text("Coche") },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColoresTorque.Primary,
                        selectedLabelColor = Color.Black
                    )
                )
            }

            SelectorMarca(
                marca = marca,
                onMarcaChange = { onMarcaChange(it); onMarcaDropdown() },
                onMarcaSeleccionada = onMarcaSeleccionada,
                label = stringResource(R.string.field_brand_required),
                error = errorMarca,
                tipoVehiculo = tipoVehiculo
            )
            if (errorMarca) {
                Text(stringResource(R.string.error_brand_required), color = ColoresTorque.RojoError, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
            }
            SelectorModelo(
                modelo = modelo,
                onModeloChange = { onModeloChange(it) },
                marca = marca,
                label = stringResource(R.string.field_model_required),
                error = errorModelo,
                tipoVehiculo = tipoVehiculo
            )
            if (errorModelo) {
                Text(stringResource(R.string.error_model_required), color = ColoresTorque.RojoError, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoTextoTorque(anio, onAnioChange, stringResource(R.string.label_year), modifier = Modifier.weight(1f))
                Column(modifier = Modifier.weight(1f)) {
                    CampoTextoTorque(
                        value = matricula,
                        onValueChange = { onMatriculaChange(it) },
                        label = stringResource(R.string.label_plate)
                    )
                    if (errorMatricula) {
                        Text(
                            text = stringResource(R.string.error_plate_required),
                            color = ColoresTorque.RojoError,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoTextoTorque(kilometros, onKilometrosChange, stringResource(R.string.field_current_kms_short), modifier = Modifier.weight(1f))
                CampoTextoTorque(cilindrada, onCilindradaChange, stringResource(R.string.label_displacement), modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.label_set_as_primary), color = Color.White, modifier = Modifier.weight(1f))
                Switch(
                    checked = esPrincipal,
                    onCheckedChange = onEsPrincipalChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = ColoresTorque.Primary)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoTextoTorque(fechaItv, onFechaItvChange, "Fecha ITV (dd/mm/aaaa)", modifier = Modifier.weight(1f))
                CampoTextoTorque(fechaSeguro, onFechaSeguroChange, "Fecha Seguro (dd/mm/aaaa)", modifier = Modifier.weight(1f))
            }

            CampoTextoTorque(
                value = estilo,
                onValueChange = onEstiloChange,
                label = stringResource(R.string.field_notes),
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )

            if (tipoVehiculo == TipoVehiculo.COCHE) {
                Text(stringResource(R.string.car_specs_section), color = ColoresTorque.Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CampoTextoTorque(tipoCarroceria, onTipoCarroceriaChange, stringResource(R.string.body_type), modifier = Modifier.weight(1f))
                    CampoTextoTorque(tipoCombustible, onTipoCombustibleChange, stringResource(R.string.fuel_type), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CampoTextoTorque(tipoTransmision, onTipoTransmisionChange, stringResource(R.string.transmission_type), modifier = Modifier.weight(1f))
                    CampoTextoTorque(numeroPuertas, onNumeroPuertasChange, stringResource(R.string.num_doors), modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.height(120.dp))
    }
}
