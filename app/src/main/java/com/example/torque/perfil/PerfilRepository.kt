package com.example.torque.perfil

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ClavesPerfil {
    val NOMBRE = stringPreferencesKey("perfil_nombre")
    val EMAIL = stringPreferencesKey("perfil_email")
    val TELEFONO = stringPreferencesKey("perfil_telefono")
    val FOTO_PERFIL = stringPreferencesKey("perfil_foto")
    val FECHA_NACIMIENTO = stringPreferencesKey("perfil_fecha_nacimiento_iso")
}

class PerfilRepositorio(private val dataStore: DataStore<Preferences>) {

    val nombreFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[ClavesPerfil.NOMBRE] }
    val emailFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[ClavesPerfil.EMAIL] }
    val telefonoFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[ClavesPerfil.TELEFONO] }
    val fotoFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[ClavesPerfil.FOTO_PERFIL] }
    val fechaNacimientoFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[ClavesPerfil.FECHA_NACIMIENTO] }

    suspend fun establecerNombre(nombre: String) {
        dataStore.edit { prefs -> prefs[ClavesPerfil.NOMBRE] = nombre }
    }

    suspend fun establecerEmail(email: String) {
        dataStore.edit { prefs -> prefs[ClavesPerfil.EMAIL] = email }
    }

    suspend fun establecerTelefono(telefono: String) {
        dataStore.edit { prefs -> prefs[ClavesPerfil.TELEFONO] = telefono }
    }

    suspend fun establecerFoto(ruta: String) {
        dataStore.edit { prefs -> prefs[ClavesPerfil.FOTO_PERFIL] = ruta }
    }

    suspend fun establecerFechaNacimientoIso(fechaIso: String) {
        dataStore.edit { prefs -> prefs[ClavesPerfil.FECHA_NACIMIENTO] = fechaIso }
    }
}
