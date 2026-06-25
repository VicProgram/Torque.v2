package com.example.torque.ajustes

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AjustesRepositorio(private val dataStore: DataStore<Preferences>) {

    private object ClavesPreferencias {
        val NOTIFICACIONES_ACTIVADAS = booleanPreferencesKey("notificaciones_activadas")
        val MODO_OSCURO_ACTIVADO = booleanPreferencesKey("modo_oscuro_activado")
        val LOGO_SELECCIONADO = intPreferencesKey("logo_seleccionado")
    }

    val notificacionesActivadasFlow: Flow<Boolean> = dataStore.data.map { preferencias ->
        preferencias[ClavesPreferencias.NOTIFICACIONES_ACTIVADAS] ?: true
    }

    val modoOscuroActivadoFlow: Flow<Boolean> = dataStore.data.map { preferencias ->
        preferencias[ClavesPreferencias.MODO_OSCURO_ACTIVADO] ?: true
    }

    val logoSelccionadoFlow: Flow<Int> = dataStore.data.map { preferencias ->
        preferencias[ClavesPreferencias.LOGO_SELECCIONADO] ?: 1
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

    suspend fun establecerLogoSeleccionado(logo: Int) {
        dataStore.edit { preferencias ->
            preferencias[ClavesPreferencias.LOGO_SELECCIONADO] = logo
        }
    }
}