package com.example.torque.desktop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

private val DarkColorScheme = darkColorScheme(
    primary = ColoresTorque.TorquePrimary,
    secondary = ColoresTorque.TorqueAccent,
    background = ColoresTorque.TorqueBackground,
    surface = ColoresTorque.TorqueSurfaceDark,
    surfaceVariant = ColoresTorque.TorqueSurfaceContainer,
    error = ColoresTorque.RojoError,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = ColoresTorque.OnSurface,
    onSurface = ColoresTorque.OnSurface,
    onSurfaceVariant = ColoresTorque.OnSurfaceVariant,
    outline = ColoresTorque.TorqueBorderDark,
)

private val LightColorScheme = lightColorScheme(
    primary = ColoresTorque.TorquePrimary,
    secondary = ColoresTorque.TorqueAccent,
    error = ColoresTorque.RojoError,
)

private val TorqueTypography = Typography(
    bodyLarge = TextStyle(fontSize = 16.sp),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

private val TorqueShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun TorqueThemeDesktop(
    darkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkMode) DarkColorScheme else LightColorScheme,
        typography = TorqueTypography,
        shapes = TorqueShapes,
        content = content
    )
}
