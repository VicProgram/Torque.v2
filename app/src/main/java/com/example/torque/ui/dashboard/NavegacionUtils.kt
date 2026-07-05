package com.example.torque.ui.dashboard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher

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
