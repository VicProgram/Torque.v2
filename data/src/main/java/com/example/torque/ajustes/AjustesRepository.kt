package com.example.torque.ajustes

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AjustesRepositorio(private val dataStore: DataStore<Preferences>) {

    private object ClavesPreferencias {
        val NOTIFICACIONES_ACTIVADAS = booleanPreferencesKey("notificaciones_activadas")
        val MODO_OSCURO_ACTIVADO = booleanPreferencesKey("modo_oscuro_activado")
    }

    val notificacionesActivadasFlow: Flow<Boolean> = dataStore.data.map { preferencias ->
        preferencias[ClavesPreferencias.NOTIFICACIONES_ACTIVADAS] ?: true
    }

    val modoOscuroActivadoFlow: Flow<Boolean> = dataStore.data.map { preferencias ->
        preferencias[ClavesPreferencias.MODO_OSCURO_ACTIVADO] ?: true
    }

    suspend fun establecerNotificacionesActivadas(activadas: Boolean) {
        dataStore.edit { preferencias ->
            preferencias[ClavesPreferencias.NOTIFICACIONES_ACTIVADAS] = activadas
        }
    }

    suspend fun establecerModoOscuroActivado(activado: Boolean) {
        dataStore.edit { preferencias ->
            preferencias[ClavesPreferencias.MODO_OSCURO_ACTIVADO] = activado
        }
    }

}