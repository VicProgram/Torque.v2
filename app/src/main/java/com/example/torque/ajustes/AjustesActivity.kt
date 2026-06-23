package com.example.torque.ajustes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.torque.components.ui.dashboard.ColoresTorque
import com.example.torque.ui.theme.TorqueTheme
import com.example.torque.ajustes.AjustesRepositorio

class AjustesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TorqueTheme {
                AjustesScreen(onBack = { finish() })
            }
        }
    }
}

@Composable
fun AjustesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = AjustesRepositorio(context.dataStore)
    val coroutineScope = rememberCoroutineScope()

    val notificacionesActivadas by repo.notificacionesActivadasFlow.collectAsState(initial = true)
    val modoOscuroActivado by repo.modoOscuroActivadoFlow.collectAsState(initial = true)

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
                    text = stringResource(id = com.example.torque.R.string.settings_title),
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = com.example.torque.R.string.preferences_header),
                    color = ColoresTorque.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )

                // Notifications toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColoresTorque.SurfaceDark)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = com.example.torque.R.string.notifications), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = stringResource(id = com.example.torque.R.string.notifications_subtitle), color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(
                        checked = notificacionesActivadas,
                        onCheckedChange = { activado -> coroutineScope.launch { repo.establecerNotificacionesActivadas(activado) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = ColoresTorque.Primary, uncheckedThumbColor = ColoresTorque.OnSurfaceVariant)
                    )
                }

                // Dark mode toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColoresTorque.SurfaceDark)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = ColoresTorque.OnSurfaceVariant, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(id = com.example.torque.R.string.dark_mode), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = stringResource(id = com.example.torque.R.string.dark_mode_subtitle), color = ColoresTorque.OnSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(
                        checked = modoOscuroActivado,
                        onCheckedChange = { activado -> coroutineScope.launch { repo.establecerModoOscuroActivado(activado) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = ColoresTorque.Primary, uncheckedThumbColor = ColoresTorque.OnSurfaceVariant)
                    )
                }
            }
        }
    }
}