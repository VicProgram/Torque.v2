package com.example.torque.desktop.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.torque.desktop.db.TorqueDatabase
import com.example.torque.shared.model.FotoMantenimientoData
import com.example.torque.shared.model.MantenimientoDto
import com.example.torque.shared.model.MotoData
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DesktopRepositorioTest {

    private lateinit var repositorio: DesktopRepositorio
    private lateinit var driver: JdbcSqliteDriver

    @Before
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TorqueDatabase.Schema.create(driver)
        val db = TorqueDatabase(driver)
        repositorio = DesktopRepositorio(testDb = db)
    }

    @After
    fun tearDown() {
        driver.close()
    }

    @Test
    fun `contarMotos devuelve 0 cuando no hay datos`() {
        assertEquals(0, repositorio.contarMotos())
    }

    @Test
    fun `obtenerMotos devuelve lista vacia cuando no hay datos`() {
        assertTrue(repositorio.obtenerMotos().isEmpty())
    }

    @Test
    fun `cargarBackup inserta motos y se pueden leer`() {
        repositorio.cargarBackup(
            motos = listOf(
                MotoData(marca = "Yamaha", modelo = "MT-07", anio = 2022, kilometros = 5000)
            ),
            mantenimientos = emptyList(),
            fotosMoto = emptyList(),
            fotosMantenimiento = emptyList()
        )
        assertEquals(1, repositorio.contarMotos())
        val motos = repositorio.obtenerMotos()
        assertEquals("Yamaha", motos.first().marca)
    }

    @Test
    fun `cargarBackup con multiple motos y mantenimientos`() {
        repositorio.cargarBackup(
            motos = listOf(
                MotoData(marca = "Honda", modelo = "CBR600RR", anio = 2020, kilometros = 15000),
                MotoData(marca = "Kawasaki", modelo = "Ninja 400", anio = 2023, kilometros = 3000)
            ),
            mantenimientos = listOf(
                MantenimientoDto(
                    idMoto = 1, nombreMantenimiento = "Cambio de Aceite",
                    fecha = "2024-01-15", kms = 10000, intervaloKms = 5000,
                    tipoMantenimientoId = 1
                ),
                MantenimientoDto(
                    idMoto = 1, nombreMantenimiento = "Filtro de Aceite",
                    fecha = "2024-01-15", kms = 10000, intervaloKms = 5000,
                    tipoMantenimientoId = 2
                )
            ),
            fotosMoto = emptyList(),
            fotosMantenimiento = emptyList()
        )
        assertEquals(2, repositorio.contarMotos())
        val mantenimientos = repositorio.obtenerMantenimientosPorMoto(1)
        assertEquals(2, mantenimientos.size)
    }

    @Test
    fun `obtenerMotoPorId devuelve la moto correcta`() {
        repositorio.cargarBackup(
            motos = listOf(
                MotoData(marca = "Suzuki", modelo = "GSX-S750", anio = 2021, kilometros = 8000)
            ),
            mantenimientos = emptyList(),
            fotosMoto = emptyList(),
            fotosMantenimiento = emptyList()
        )
        val id = repositorio.obtenerMotos().first().id
        val moto = repositorio.obtenerMotoPorId(id)
        assertNotNull(moto)
        assertEquals("Suzuki", moto!!.marca)
    }

    @Test
    fun `obtenerMotoPorId devuelve null cuando no existe`() {
        assertNull(repositorio.obtenerMotoPorId(999))
    }

    @Test
    fun `obtenerMantenimientosPorMoto devuelve lista vacia cuando no hay`() {
        repositorio.cargarBackup(
            motos = listOf(MotoData(marca = "BMW", modelo = "F900R", anio = 2022, kilometros = 6000)),
            mantenimientos = emptyList(),
            fotosMoto = emptyList(),
            fotosMantenimiento = emptyList()
        )
        val id = repositorio.obtenerMotos().first().id
        assertTrue(repositorio.obtenerMantenimientosPorMoto(id).isEmpty())
    }

    @Test
    fun `cargarBackup con fotos de moto`() {
        repositorio.cargarBackup(
            motos = listOf(MotoData(marca = "Ducati", modelo = "Monster", anio = 2023, kilometros = 2000)),
            mantenimientos = emptyList(),
            fotosMoto = listOf(
                com.example.torque.shared.model.FotoData(idMoto = 1, ruta = "/tmp/ducati1.jpg", creadoEn = "2024-01-01"),
                com.example.torque.shared.model.FotoData(idMoto = 1, ruta = "/tmp/ducati2.jpg", creadoEn = "2024-01-02")
            ),
            fotosMantenimiento = emptyList()
        )
        val id = repositorio.obtenerMotos().first().id
        val fotos = repositorio.obtenerFotosPorMoto(id)
        assertEquals(2, fotos.size)
    }

    @Test
    fun `limpiarTodo elimina todos los datos`() {
        repositorio.cargarBackup(
            motos = listOf(MotoData(marca = "Aprilia", modelo = "RS 660", anio = 2024, kilometros = 1000)),
            mantenimientos = listOf(
                MantenimientoDto(idMoto = 1, nombreMantenimiento = "Revision inicial", fecha = "2024-06-01", kms = 1000, intervaloKms = 5000)
            ),
            fotosMoto = listOf(com.example.torque.shared.model.FotoData(idMoto = 1, ruta = "/tmp/test.jpg")),
            fotosMantenimiento = listOf(FotoMantenimientoData(idMantenimiento = 1, ruta = "/tmp/mant.jpg"))
        )
        assertEquals(1, repositorio.contarMotos())
        repositorio.limpiarTodo()
        assertEquals(0, repositorio.contarMotos())
    }
}
