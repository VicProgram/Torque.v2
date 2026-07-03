package com.example.torque.desktop.data.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.torque.desktop.db.TorqueDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DatabaseFactoryTest {

    private lateinit var driver: JdbcSqliteDriver
    private lateinit var db: TorqueDatabase

    @Before
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TorqueDatabase.Schema.create(driver)
        db = TorqueDatabase(driver)
    }

    @After
    fun tearDown() {
        driver.close()
    }

    @Test
    fun `base de datos se crea correctamente`() {
        assertNotNull(db)
    }

    @Test
    fun `tabla motos existe y se pueden insertar datos`() {
        db.torqueQueries.insertarMoto(
            marca = "Honda", modelo = "CBR600RR", anio = 2020,
            matricula = "5678DEF", cilindrada = "599cc",
            caballos = "120cv", estilo = "Deportiva",
            kilometros = 10000, esPrincipal = 1,
            uriFoto = null, creadoEn = "2024-06-01", actualizadoEn = null
        )
        val motos = db.torqueQueries.obtenerTodasMotos().executeAsList()
        assertEquals(1, motos.size)
        assertEquals("Honda", motos[0].marca)
    }

    @Test
    fun `tabla mantenimientos existe`() {
        db.torqueQueries.insertarMoto(
            marca = "Kawasaki", modelo = "Ninja 400", anio = 2023,
            matricula = "9012GHI", cilindrada = "399cc",
            caballos = "45cv", estilo = "Deportiva",
            kilometros = 3000, esPrincipal = 0,
            uriFoto = null, creadoEn = "2024-03-10", actualizadoEn = null
        )
        val idMoto = db.torqueQueries.obtenerTodasMotos().executeAsOne().idMoto
        db.torqueQueries.insertarMantenimiento(
            idMoto = idMoto, nombreMantenimiento = "Cambio de Aceite",
            fecha = "2024-06-15", kms = 3000, intervaloKms = 5000,
            precio = "45.00", tiempo = null, notas = "Aceite 10W40",
            tareas = "Cambio de Aceite Motor", creadoEn = "2024-06-15",
            tipoMantenimientoId = 1, porcentajeVida = 100
        )
        val mantenimientos = db.torqueQueries.obtenerMantenimientosPorMoto(idMoto).executeAsList()
        assertEquals(1, mantenimientos.size)
        assertEquals("Cambio de Aceite", mantenimientos[0].nombreMantenimiento)
    }

    @Test
    fun `tabla fotos_moto existe`() {
        db.torqueQueries.insertarMoto(
            marca = "Suzuki", modelo = "GSX-S750", anio = 2021,
            matricula = "3456JKL", cilindrada = "749cc",
            caballos = "112cv", estilo = "Naked",
            kilometros = 8000, esPrincipal = 0,
            uriFoto = null, creadoEn = "2024-02-20", actualizadoEn = null
        )
        val idMoto = db.torqueQueries.obtenerTodasMotos().executeAsOne().idMoto
        db.torqueQueries.insertarFoto(idMoto = idMoto, ruta = "/tmp/foto1.jpg", creadoEn = "2024-02-20")
        val fotos = db.torqueQueries.obtenerFotosPorMoto(idMoto).executeAsList()
        assertEquals(1, fotos.size)
    }

    @Test
    fun `tabla fotos_mantenimiento existe`() {
        db.torqueQueries.insertarMoto(
            marca = "BMW", modelo = "F900R", anio = 2022,
            matricula = "7890MNO", cilindrada = "895cc",
            caballos = "105cv", estilo = "Naked",
            kilometros = 6000, esPrincipal = 0,
            uriFoto = null, creadoEn = "2024-04-01", actualizadoEn = null
        )
        val idMoto = db.torqueQueries.obtenerTodasMotos().executeAsOne().idMoto
        db.torqueQueries.insertarMantenimiento(
            idMoto = idMoto, nombreMantenimiento = "Cambio de Pastillas",
            fecha = "2024-07-01", kms = 6000, intervaloKms = 5000,
            precio = null, tiempo = null, notas = null,
            tareas = "Pastillas de Freno Delantero", creadoEn = "2024-07-01",
            tipoMantenimientoId = 10, porcentajeVida = 100
        )
        val idMant = db.torqueQueries.obtenerMantenimientosPorMoto(idMoto).executeAsOne().id
        db.torqueQueries.insertarFotoMantenimiento(
            idMantenimiento = idMant, ruta = "/tmp/mant_foto.jpg",
            tipo = "foto", creadoEn = "2024-07-01"
        )
        val fotos = db.torqueQueries.obtenerFotosPorMantenimiento(idMant).executeAsList()
        assertEquals(1, fotos.size)
    }

    @Test
    fun `contarMotos devuelve 0 inicialmente`() {
        val count = db.torqueQueries.contarMotos().executeAsOne()
        assertEquals(0, count)
    }

    @Test
    fun `se pueden insertar multiples motos y contarlas`() {
        fun insert(marca: String, modelo: String) {
            db.torqueQueries.insertarMoto(
                marca = marca, modelo = modelo, anio = 2020,
                matricula = null, cilindrada = null,
                caballos = null, estilo = null,
                kilometros = 0, esPrincipal = 0,
                uriFoto = null, creadoEn = null, actualizadoEn = null
            )
        }
        insert("Yamaha", "MT-07")
        insert("Honda", "CB500")
        insert("Kawasaki", "Z900")
        val count = db.torqueQueries.contarMotos().executeAsOne()
        assertEquals(3, count)
    }

    @Test
    fun `limpiar tablas en orden inverso`() {
        db.torqueQueries.insertarMoto(
            marca = "Test", modelo = "Test", anio = 2020,
            matricula = null, cilindrada = null,
            caballos = null, estilo = null,
            kilometros = 0, esPrincipal = 0,
            uriFoto = null, creadoEn = null, actualizadoEn = null
        )
        val idMoto = db.torqueQueries.obtenerTodasMotos().executeAsOne().idMoto
        db.torqueQueries.insertarMantenimiento(
            idMoto = idMoto, nombreMantenimiento = "Test", fecha = "2024-01-01",
            kms = 0, intervaloKms = 0, precio = null, tiempo = null,
            notas = null, tareas = null, creadoEn = null,
            tipoMantenimientoId = 0, porcentajeVida = 100
        )
        assertTrue(db.torqueQueries.contarMotos().executeAsOne() > 0)
        db.torqueQueries.eliminarTodoMantenimientos()
        db.torqueQueries.eliminarTodoFotos()
        db.torqueQueries.eliminarTodoFotosMantenimiento()
        db.torqueQueries.eliminarTodoMotos()
        assertEquals(0, db.torqueQueries.contarMotos().executeAsOne())
    }
}
