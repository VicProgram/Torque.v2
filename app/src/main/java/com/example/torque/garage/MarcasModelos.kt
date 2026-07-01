package com.example.torque.garage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.torque.components.ui.dashboard.ColoresTorque

object MarcasModelos {
    val TODAS = MarcasData.TODAS
    val MARCAS = MarcasData.MARCAS
    fun modelosDe(marca: String): List<String> = MarcasData.modelosDe(marca)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorMarca(
    marca: String,
    onMarcaChange: (String) -> Unit,
    onMarcaSeleccionada: (String) -> Unit,
    label: String,
    error: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val marcasFiltradas = remember(marca) {
        MarcasModelos.MARCAS.filter { it.lowercase().contains(marca.lowercase()) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && marcasFiltradas.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = marca,
            onValueChange = {
                onMarcaChange(it)
                onMarcaSeleccionada("")
                expanded = true
            },
            label = { Text(label, color = if (error) ColoresTorque.RojoError else ColoresTorque.OnSurfaceVariant) },
            singleLine = true,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
            textStyle = TextStyle(color = Color.White),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = error,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = if (error) ColoresTorque.RojoError else ColoresTorque.Primary,
                unfocusedBorderColor = if (error) ColoresTorque.RojoError else ColoresTorque.Outline,
                cursorColor = ColoresTorque.Primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded && marcasFiltradas.isNotEmpty(),
            onDismissRequest = { expanded = false },
            containerColor = ColoresTorque.SurfaceDark
        ) {
            marcasFiltradas.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = Color.White) },
                    onClick = {
                        onMarcaChange(item)
                        onMarcaSeleccionada(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorModelo(
    modelo: String,
    onModeloChange: (String) -> Unit,
    marca: String,
    label: String,
    error: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val modelosDisponibles = remember(marca) {
        val modelos = MarcasModelos.modelosDe(marca)
        if (modelos.isEmpty()) MarcasModelos.TODAS.values.flatten().distinct().sorted()
        else modelos
    }
    val modelosFiltrados = remember(modelo, modelosDisponibles) {
        modelosDisponibles.filter { it.lowercase().contains(modelo.lowercase()) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && modelosFiltrados.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = modelo,
            onValueChange = {
                onModeloChange(it)
                expanded = true
            },
            label = { Text(label, color = if (error) ColoresTorque.RojoError else ColoresTorque.OnSurfaceVariant) },
            singleLine = true,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
            textStyle = TextStyle(color = Color.White),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = error,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = if (error) ColoresTorque.RojoError else ColoresTorque.Primary,
                unfocusedBorderColor = if (error) ColoresTorque.RojoError else ColoresTorque.Outline,
                cursorColor = ColoresTorque.Primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded && modelosFiltrados.isNotEmpty(),
            onDismissRequest = { expanded = false },
            containerColor = ColoresTorque.SurfaceDark
        ) {
            modelosFiltrados.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = Color.White) },
                    onClick = {
                        onModeloChange(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
