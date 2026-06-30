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
    val CARNETS = stringPreferencesKey("perfil_carnets")
}

open class PerfilRepositorio(private val dataStore: DataStore<Preferences>) {

    open val nombreFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[ClavesPerfil.NOMBRE]?.let { CifradorPerfil.descifrar(it) }
    }
    val emailFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[ClavesPerfil.EMAIL]?.let { CifradorPerfil.descifrar(it) }
    }
    val telefonoFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[ClavesPerfil.TELEFONO]?.let { CifradorPerfil.descifrar(it) }
    }
    val fotoFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[ClavesPerfil.FOTO_PERFIL] }
    val fechaNacimientoFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[ClavesPerfil.FECHA_NACIMIENTO] }
    val carnetsFlow: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[ClavesPerfil.CARNETS]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

    open suspend fun establecerNombre(nombre: String) {
        dataStore.edit { prefs -> prefs[ClavesPerfil.NOMBRE] = CifradorPerfil.cifrar(nombre) }
    }

    suspend fun establecerEmail(email: String) {
        dataStore.edit { prefs -> prefs[ClavesPerfil.EMAIL] = CifradorPerfil.cifrar(email) }
    }

    suspend fun establecerTelefono(telefono: String) {
        dataStore.edit { prefs -> prefs[ClavesPerfil.TELEFONO] = CifradorPerfil.cifrar(telefono) }
    }

    suspend fun establecerFoto(ruta: String) {
        dataStore.edit { prefs -> prefs[ClavesPerfil.FOTO_PERFIL] = ruta }
    }

    suspend fun establecerFechaNacimientoIso(fechaIso: String) {
        dataStore.edit { prefs -> prefs[ClavesPerfil.FECHA_NACIMIENTO] = fechaIso }
    }

    suspend fun establecerCarnets(carnets: Set<String>) {
        dataStore.edit { prefs ->
            prefs[ClavesPerfil.CARNETS] = carnets.joinToString(",")
        }
    }
}
