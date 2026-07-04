package com.example.torque.network.di

import com.example.torque.network.ApiClient
import com.example.torque.network.AuthManager
import com.example.torque.network.TorqueApiService
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module {
    single { AuthManager.getInstance(androidContext()) }

    single {
        val authManager: AuthManager = get()
        val baseUrl = runBlocking { authManager.getServerUrl() }
        val normalised = baseUrl.trimEnd('/') + "/"
        ApiClient.create(normalised, authManager)
    }
}
