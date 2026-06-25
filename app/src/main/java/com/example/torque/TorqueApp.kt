package com.example.torque

import android.app.Application
import com.example.torque.ajustes.AjustesRepositorio
import com.example.torque.ajustes.dataStore
import com.example.torque.room.RepositorioApp
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.context.startKoin
import org.koin.dsl.module

class TorqueApp : Application() {

    override fun onCreate() {
        super.onCreate()

        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.tracesSampleRate = 0.2
            options.isEnableUserInteractionTracing = true
        }

        startKoin {
            androidContext(this@TorqueApp)
            modules(appModule)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }
}

val appModule = module {
    single { RepositorioApp.obtenerInstancia(get()) }
    single { AjustesRepositorio(get().dataStore) }
}
