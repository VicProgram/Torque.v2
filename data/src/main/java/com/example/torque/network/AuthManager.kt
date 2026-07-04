package com.example.torque.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.authDataStore by preferencesDataStore(name = "torque_auth")

class AuthManager(private val context: Context) {

    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_ID_USUARIO = stringPreferencesKey("id_usuario")
        private val KEY_ID_TALLER = stringPreferencesKey("id_taller")
        private val KEY_NOMBRE = stringPreferencesKey("nombre")
        private val KEY_ROL = stringPreferencesKey("rol")
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        const val DEFAULT_SERVER_URL = "http://172.23.238.95:8000"

        @Volatile
        private var instance: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager(context.applicationContext).also { instance = it }
            }
        }
    }

    val tokenFlow: Flow<String?> = context.authDataStore.data.map { it[KEY_TOKEN] }

    val serverUrlFlow: Flow<String> = context.authDataStore.data.map {
        it[KEY_SERVER_URL] ?: DEFAULT_SERVER_URL
    }

    suspend fun getToken(): String? = context.authDataStore.data.first()[KEY_TOKEN]

    suspend fun getServerUrl(): String =
        context.authDataStore.data.first()[KEY_SERVER_URL] ?: DEFAULT_SERVER_URL

    suspend fun isLoggedIn(): Boolean = getToken() != null

    suspend fun saveLogin(
        token: String,
        idUsuario: String,
        idTaller: String,
        nombre: String,
        rol: String,
    ) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_ID_USUARIO] = idUsuario
            prefs[KEY_ID_TALLER] = idTaller
            prefs[KEY_NOMBRE] = nombre
            prefs[KEY_ROL] = rol
        }
    }

    suspend fun saveServerUrl(url: String) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = url
        }
    }

    suspend fun logout() {
        context.authDataStore.edit { it.clear() }
    }

    fun createApiService(): TorqueApiService {
        val baseUrl = runBlocking { getServerUrl() }
        val normalised = baseUrl.trimEnd('/') + "/"
        return ApiClient.create(normalised, this)
    }
}
