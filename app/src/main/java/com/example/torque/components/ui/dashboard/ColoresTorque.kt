package com.example.torque.components.ui.dashboard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.ui.theme.TorqueAccent
import com.example.torque.ui.theme.TorqueBackground
import com.example.torque.ui.theme.TorqueBorderDark
import com.example.torque.ui.theme.TorqueOnPrimary
import com.example.torque.ui.theme.TorqueOnPrimaryContainer
import com.example.torque.ui.theme.TorqueOnSecondary
import com.example.torque.ui.theme.TorqueOnSecondaryContainer
import com.example.torque.ui.theme.TorqueOnSurface
import com.example.torque.ui.theme.TorqueOnSurfaceVariant
import com.example.torque.ui.theme.TorqueOnTertiary
import com.example.torque.ui.theme.TorqueOutline
import com.example.torque.ui.theme.TorquePrimary
import com.example.torque.ui.theme.TorquePrimaryContainer
import com.example.torque.ui.theme.TorqueSecondary
import com.example.torque.ui.theme.TorqueSecondaryContainer
import com.example.torque.ui.theme.TorqueSlate500
import com.example.torque.ui.theme.TorqueSlate900
import com.example.torque.ui.theme.TorqueSurface
import com.example.torque.ui.theme.TorqueSurfaceContainer
import com.example.torque.ui.theme.TorqueSurfaceContainerHigh
import com.example.torque.ui.theme.TorqueSurfaceContainerLow
import com.example.torque.ui.theme.TorqueSurfaceDark
import com.example.torque.ui.theme.TorqueSurfaceVariant
import com.example.torque.ui.theme.TorqueTertiary

// Objeto para acceder a los colores desde otros archivos
object ColoresTorque {
    val Primary = TorquePrimary
    val Accent = TorqueAccent
    val OnPrimary = TorqueOnPrimary
    val PrimaryContainer = TorquePrimaryContainer
    val OnPrimaryContainer = TorqueOnPrimaryContainer
    val Secondary = TorqueSecondary
    val OnSecondary = TorqueOnSecondary
    val SecondaryContainer = TorqueSecondaryContainer
    val OnSecondaryContainer = TorqueOnSecondaryContainer
    val Surface = TorqueSurface
    val OnSurface = TorqueOnSurface
    val SurfaceVariant = TorqueSurfaceVariant
    val OnSurfaceVariant = TorqueOnSurfaceVariant
    val Outline = TorqueOutline
    val Background = TorqueBackground
    val SurfaceDark = TorqueSurfaceDark
    val BorderDark = TorqueBorderDark
    val Tertiary = TorqueTertiary
    val OnTertiary = TorqueOnTertiary
    val SurfaceContainerHigh = TorqueSurfaceContainerHigh
    val SurfaceContainer = TorqueSurfaceContainer
    val SurfaceContainerLow = TorqueSurfaceContainerLow
    val Slate500 = TorqueSlate500
    val Slate900 = TorqueSlate900
    val RojoError = com.example.torque.ui.theme.RojoError
    val VerdeExito = com.example.torque.ui.theme.VerdeExito
    val AmarilloAdvertencia = com.example.torque.ui.theme.AmarilloAdvertencia
}

// --- Navegación y Utilidad ---
fun navegarA(context: Context, destino: Class<*>, extras: (Intent.() -> Unit)? = null) {
    val intent = Intent(context, destino)
    extras?.invoke(intent)
    context.startActivity(intent)
}

fun lanzarActividad(
    launcher: ActivityResultLauncher<Intent>,
    context: Context,
    destino: Class<*>,
    extras: (Intent.() -> Unit)? = null
) {
    val intent = Intent(context, destino)
    extras?.invoke(intent)
    launcher.launch(intent)
}

fun mostrarToast(context: Context, mensaje: String) {
    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
}

// --- Envoltorios de UI ---
@Composable
fun ImagenDeFondo(resourceId: Int, alpha: Float = 1f) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 1f - alpha))
        )
    }
}

@Composable
fun TarjetaTorque(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ColoresTorque.SurfaceDark,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun CampoTextoTorque(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = ColoresTorque.OnSurfaceVariant) },
        singleLine = singleLine,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        textStyle = TextStyle(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = ColoresTorque.Primary,
            unfocusedBorderColor = ColoresTorque.Outline,
            cursorColor = ColoresTorque.Primary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
fun EncabezadoDeSeccion(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            color = ColoresTorque.Slate500,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        ),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
fun BotonTorque(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = ColoresTorque.Primary,
    contentColor: Color = Color.White,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.size(8.dp))
            }
            Text(
                text.uppercase(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
        )
    }
}

}

@Composable
fun ChipDeSeleccionRapida(
    text: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) ColoresTorque.Primary else ColoresTorque.SurfaceVariant
    val contentColor = if (isSelected) ColoresTorque.OnPrimary else ColoresTorque.OnSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ElementoCheckMotoKeep(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColoresTorque.SurfaceDark)
            .border(1.dp, ColoresTorque.BorderDark, RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = description, color = ColoresTorque.Slate500, fontSize = 12.sp)
        }

        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = ColoresTorque.Primary,
                uncheckedColor = ColoresTorque.BorderDark,
                checkmarkColor = Color.White
            ),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun FiltroChip(
    texto: String,
    estaSeleccionado: Boolean,
    onClick: () -> Unit = {},
    forma: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    colorSeleccionado: Color = ColoresTorque.PrimaryContainer,
    colorSeleccionadoContenido: Color = ColoresTorque.OnPrimaryContainer,
    colorNoSeleccionadoFondo: Color = Color.Transparent,
    colorNoSeleccionadoBorde: Color = ColoresTorque.Outline,
    mostrarCheck: Boolean = false
) {
    val colorFondo = if (estaSeleccionado) colorSeleccionado else colorNoSeleccionadoFondo
    val colorContenido = if (estaSeleccionado) colorSeleccionadoContenido else ColoresTorque.OnSurfaceVariant
    val colorBorde = if (estaSeleccionado) Color.Transparent else colorNoSeleccionadoBorde

    Box(
        modifier = Modifier
            .clip(forma)
            .background(colorFondo)
            .border(if (estaSeleccionado) 0.dp else 1.dp, colorBorde, forma)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (estaSeleccionado && mostrarCheck) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = colorContenido)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(texto, color = colorContenido, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
