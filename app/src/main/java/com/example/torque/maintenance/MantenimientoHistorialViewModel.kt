package com.example.torque.maintenance

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.FotoMantenimientoEntity
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.utils.ArchivoUtils
import com.example.torque.utils.DateUtils
import com.example.torque.utils.PdfExporter
import com.example.torque.utils.SaludMotoCalculator
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MantenimientoHistorialViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = RepositorioApp.obtenerInstancia(application)

    private val _historial = MutableStateFlow<List<MantenimientoEntity>>(emptyList())
    val historial: StateFlow<List<MantenimientoEntity>> = _historial

    private val _fotosPorMantenimiento = MutableStateFlow<Map<Long, List<FotoMantenimientoEntity>>>(emptyMap())
    val fotosPorMantenimiento: StateFlow<Map<Long, List<FotoMantenimientoEntity>>> = _fotosPorMantenimiento

    private var historialJob: Job? = null

    fun cargarHistorial(idMoto: Long? = null) {
        historialJob?.cancel()
        historialJob = viewModelScope.launch {
            if (idMoto != null) {
                repositorio.obtenerMantenimientosPorMoto(idMoto).collect { lista ->
                    _historial.value = lista
                    cargarFotos(lista)
                }
            } else {
                _historial.value = emptyList()
            }
        }
    }

    private suspend fun cargarFotos(mantenimientos: List<MantenimientoEntity>) {
        if (mantenimientos.isEmpty()) {
            _fotosPorMantenimiento.value = emptyMap()
            return
        }
        val ids = mantenimientos.map { it.id }
        val todas = repositorio.obtenerFotosPorMantenimientoBatch(ids)
        val mapa = todas.groupBy { it.idMantenimiento }
        _fotosPorMantenimiento.value = mapa
    }

    suspend fun obtenerFotos(mantenimientoId: Long): List<FotoMantenimientoEntity> {
        return repositorio.obtenerFotosPorMantenimientoList(mantenimientoId)
    }

    suspend fun agregarFoto(mantenimientoId: Long, uri: Uri, tipo: String = "foto"): Boolean {
        val ruta = ArchivoUtils.copiarImagenAInterno(getApplication(), uri) ?: return false
        val foto = FotoMantenimientoEntity(
            idMantenimiento = mantenimientoId,
            ruta = ruta,
            tipo = tipo,
            creadoEn = SimpleDateFormat(DateUtils.FORMATO_FECHA_COMPLETA, Locale.getDefault()).format(Date())
        )
        repositorio.insertarFotoMantenimiento(foto)
        cargarHistorial(_historial.value.firstOrNull()?.idMoto)
        return true
    }

    suspend fun eliminarFoto(foto: FotoMantenimientoEntity) {
        repositorio.eliminarFotoMantenimiento(foto)
        withContext(Dispatchers.IO) { File(foto.ruta).delete() }
        cargarHistorial(_historial.value.firstOrNull()?.idMoto)
    }

    fun eliminarMantenimiento(mantenimiento: MantenimientoEntity) {
        viewModelScope.launch {
            repositorio.eliminarMantenimiento(mantenimiento)
            cargarHistorial(_historial.value.firstOrNull()?.idMoto)
        }
    }

    suspend fun exportarPdf(contexto: android.content.Context, uri: Uri, idMoto: Long): Result<Unit> {
        val moto = repositorio.obtenerMotoPorId(idMoto) ?: return Result.failure(Exception("Moto no encontrada"))
        val mantenimientos = repositorio.obtenerTodosMantenimientosPorMoto(idMoto)
        val salud = SaludMotoCalculator.calcular(mantenimientos, moto.kilometros, moto.tipoVehiculo)
        return PdfExporter.exportarHistorial(contexto, uri, moto, mantenimientos, salud)
    }
}
