package com.example.torque.garage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R
import com.example.torque.components.ui.dashboard.ColoresTorque

@Composable
fun CabeceraDetalleMoto(
    modelo: String,
    esPrincipal: Boolean,
    onBack: () -> Unit,
    onSetPrimary: () -> Unit,
    onRegisterMaintenance: () -> Unit,
    onViewHistory: () -> Unit,
    onPlanMaintenance: () -> Unit,
    onEditBike: () -> Unit,
    onDeleteBike: () -> Unit
) {
    var mostrarMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ColoresTorque.ALTURA_CABECERA)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = ColoresTorque.Primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = modelo,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (esPrincipal) {
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
                        onSetPrimary()
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
                        onRegisterMaintenance()
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
                        onViewHistory()
                        mostrarMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_plan_maintenance), color = Color.White) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = ColoresTorque.Primary
                        )
                    },
                    onClick = {
                        onPlanMaintenance()
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
                        onEditBike()
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
                        onDeleteBike()
                        mostrarMenu = false
                    }
                )
            }
        }
    }
}
