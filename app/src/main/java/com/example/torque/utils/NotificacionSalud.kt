package com.example.torque.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.torque.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.example.torque.ajustes.dataStore

object NotificacionSalud {

    private const val CANAL_MANTENIMIENTO = "mantenimiento_vencimiento"
    private const val TAG_NOTIFICADOS = "notificaciones_enviadas"

    private val ClaveNotificados = stringPreferencesKey(TAG_NOTIFICADOS)

    fun crearCanal(contexto: Context) {
        val nombre = "Vencimiento de Mantenimiento"
        val descripcion = "Notificaciones de mantenimientos próximos a vencer o vencidos"
        val importancia = NotificationManager.IMPORTANCE_DEFAULT
        val canal = NotificationChannel(CANAL_MANTENIMIENTO, nombre, importancia).apply {
            description = descripcion
        }
        val manager = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(canal)
    }

    suspend fun verificarYNotificar(
        contexto: Context,
        resultadoSalud: ResultadoSalud,
        @Suppress("UNUSED_PARAMETER") kmActuales: Int,
        motoNombre: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return
        }

        crearCanal(contexto)

        val yaNotificados = contexto.dataStore.data.map { prefs ->
            prefs[ClaveNotificados]?.split(",")?.toSet() ?: emptySet()
        }.first()

        val nuevosNotificados = mutableSetOf<String>()
        nuevosNotificados.addAll(yaNotificados)

        for (detalle in resultadoSalud.desglose) {
            procesarDetalle(detalle, motoNombre, yaNotificados, nuevosNotificados, contexto)
        }

        contexto.dataStore.edit { prefs ->
            prefs[ClaveNotificados] = nuevosNotificados.joinToString(",")
        }
    }

    private fun procesarDetalle(
        detalle: DetalleSalud,
        motoNombre: String,
        yaNotificados: Set<String>,
        nuevosNotificados: MutableSet<String>,
        contexto: Context
    ) {
        if (detalle.tipo.esDesgaste) return
        if (detalle.tipo.intervaloKms == 0 && detalle.tipo.intervaloDias == 0) return

        val tipoId = detalle.tipo.id
        val cumplimiento = detalle.cumplimiento
        val nombreTipo = detalle.tipo.nombre

        val claveWarning = "${tipoId}_warning"
        val claveExpired = "${tipoId}_expired"

        if (cumplimiento in 1..20 && claveWarning !in yaNotificados) {
            mostrarNotificacion(
                contexto = contexto,
                id = (tipoId * 10 + 1).toInt(),
                titulo = "Mantenimiento próximo a vencer",
                mensaje = "$nombreTipo de $motoNombre necesita atención (${100 - cumplimiento}% del intervalo recorrido)"
            )
            nuevosNotificados.add(claveWarning)
        }

        if (cumplimiento == 0 && claveExpired !in yaNotificados) {
            mostrarNotificacion(
                contexto = contexto,
                id = (tipoId * 10 + 2).toInt(),
                titulo = "Mantenimiento vencido",
                mensaje = "$nombreTipo de $motoNombre — ya deberías haberlo realizado"
            )
            nuevosNotificados.add(claveExpired)
        }
    }

    private fun mostrarNotificacion(contexto: Context, id: Int, titulo: String, mensaje: String) {
        val notificacion = NotificationCompat.Builder(contexto, CANAL_MANTENIMIENTO)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(contexto).notify(id, notificacion)
    }
}
