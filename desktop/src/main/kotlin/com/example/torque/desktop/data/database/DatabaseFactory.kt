package com.example.torque.desktop.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.torque.desktop.db.TorqueDatabase
import java.io.File

object DatabaseFactory {
    private const val NOMBRE_DB = "torque.db"
    private val RUTA_BASE = File(System.getProperty("user.home"), ".torque")

    private var driver: SqlDriver? = null

    fun obtenerDriver(): SqlDriver {
        return driver ?: JdbcSqliteDriver(
            "jdbc:sqlite:${RUTA_BASE.resolve(NOMBRE_DB).absolutePath}"
        ).also { sqlDriver ->
            RUTA_BASE.mkdirs()
            TorqueDatabase.Schema.create(sqlDriver)
            driver = sqlDriver
        }
    }

    fun obtenerBaseDeDatos(): TorqueDatabase {
        return TorqueDatabase(obtenerDriver())
    }

    fun cerrar() {
        driver?.close()
        driver = null
    }

    fun rutaBase(): File = RUTA_BASE

    fun rutaFotos(): File = RUTA_BASE.resolve("fotos").also { it.mkdirs() }
}
