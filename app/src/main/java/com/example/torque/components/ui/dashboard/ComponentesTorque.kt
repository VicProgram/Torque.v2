package com.example.torque.components.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torque.R

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
fun DialogoTorque(
    onDismiss: () -> Unit,
    showDismiss: Boolean = true,
    title: String,
    icon: (@Composable () -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    confirmText: String = stringResource(R.string.btn_save),
    confirmColor: Color = ColoresTorque.Primary,
    dismissText: String = stringResource(R.string.btn_cancel),
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColoresTorque.SurfaceDark,
        icon = icon,
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = content,
        confirmButton = {
            if (onConfirm != null) {
                TextButton(onClick = onConfirm) {
                    Text(confirmText, color = confirmColor)
                }
            }
        },
        dismissButton = {
            if (showDismiss) {
                TextButton(onClick = onDismiss) {
                    Text(dismissText, color = ColoresTorque.OnSurfaceVariant)
                }
            }
        }
    )
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
