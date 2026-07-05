package com.example.torque.ui.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.torque.R
import com.example.torque.room.entity.MotoEntity
import com.example.torque.utils.ResultadoSalud
import com.example.torque.utils.TipoVehiculo

@Composable
fun TarjetaProximoServicio(
    moto: MotoEntity?,
    fotoPortada: String? = null,
    kmRestantes: Int? = null,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (moto?.tipoVehiculo == TipoVehiculo.COCHE) Icons.Default.DirectionsCar else Icons.Default.Motorcycle,
                            contentDescription = null,
                            tint = ColoresTorque.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
