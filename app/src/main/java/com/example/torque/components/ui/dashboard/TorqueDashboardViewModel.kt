package com.example.torque.components.ui.dashboard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.torque.ajustes.dataStore
import com.example.torque.perfil.PerfilRepositorio
import com.example.torque.room.RepositorioApp
import com.example.torque.room.entity.FotoEntity
import com.example.torque.room.entity.MantenimientoEntity
import com.example.torque.room.entity.MotoEntity
import com.example.torque.utils.DateUtils
import com.example.torque.utils.ResultadoSalud
import com.example.torque.utils.SaludMotoCalculator
import com.example.torque.utils.TiposMantenimiento
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class DashboardUiState(
    val motos: List<MotoEntity> = emptyList(),
    val selectedIndex: Int = 0,
    val fotos: List<FotoEntity> = emptyList(),
    val ultimoMantenimiento: MantenimientoEntity? = null,
    val resultadoSalud: ResultadoSalud? = null,
    val nombrePerfil: String? = null,
    val kmRestantes: Int? = null,
    val progreso: Float = 0f,
    val notificacionesPendientes: Int = 0
)

class TorqueDashboardViewModel(
    private val repositorio: RepositorioApp,
    private val perfilRepositorio: PerfilRepositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var dataJob: Job? = null

    init {
        viewModelScope.launch { observeAllMotos() }
        viewModelScope.launch { observeNombrePerfil() }
    }

    private suspend fun observeAllMotos() {
        repositorio.obtenerMotosFlow().collect { motos ->
            val sorted = motos.sortedByDescending { it.esPrincipal }
            val currentIndex = _uiState.value.selectedIndex
            val clampedIndex = if (currentIndex < sorted.size) currentIndex else 0
            _uiState.update { it.copy(motos = sorted, selectedIndex = clampedIndex) }
            val moto = sorted.getOrNull(clampedIndex)
            if (moto != null) {
                startObservingData(moto.idMoto)
            } else {
                dataJob?.cancel()
                _uiState.value = DashboardUiState(nombrePerfil = _uiState.value.nombrePerfil)
            }
        }
    }

    fun selectMoto(index: Int) {
        val moto = _uiState.value.motos.getOrNull(index) ?: return
        _uiState.update { it.copy(selectedIndex = index) }
        startObservingData(moto.idMoto)
    }

    private suspend fun observeNombrePerfil() {
        perfilRepositorio.nombreFlow.collect { nombre ->
            _uiState.update { it.copy(nombrePerfil = nombre) }
        }
    }

    private fun startObservingData(idMoto: Long) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            val ultimo = repositorio.obtenerUltimoMantenimiento(idMoto)
            updateDerivedState(ultimo)

            launch {
                repositorio.obtenerFotosPorMoto(idMoto).collect { fotos ->
                    _uiState.update { it.copy(fotos = fotos) }
                }
            }

            launch {
                repositorio.obtenerMantenimientosPorMoto(idMoto).collect { mantenimientos ->
                    val moto = _uiState.value.motos.getOrNull(_uiState.value.selectedIndex)
                    if (moto != null) {
                        val salud = SaludMotoCalculator.calcular(mantenimientos, moto.kilometros)
                        val pendientes = salud.desglose.count {
                            it.cumplimiento <= 20 && !it.tipo.esDesgaste
                        }
                        _uiState.update { it.copy(
                            resultadoSalud = salud,
                            notificacionesPendientes = pendientes
                        )}
                    }
                }
            }
        }
    }

    private fun updateDerivedState(ultimo: MantenimientoEntity?) {
        val moto = _uiState.value.motos.getOrNull(_uiState.value.selectedIndex) ?: return
        _uiState.update { it.copy(ultimoMantenimiento = ultimo) }

        val kmRestantes = if (ultimo != null && ultimo.intervaloKms > 0) {
            val proxServicio = ultimo.kms + ultimo.intervaloKms
            val restante = proxServicio - moto.kilometros
            if (restante < 0) -1 else restante
        } else null

        val progreso = if (ultimo != null && ultimo.intervaloKms > 0 && kmRestantes != null) {
            val transcurrido = moto.kilometros - ultimo.kms
            if (ultimo.intervaloKms > 0) (transcurrido.toFloat() / ultimo.intervaloKms).coerceIn(0f, 1f) else 0f
        } else 0f

        _uiState.update { it.copy(kmRestantes = kmRestantes, progreso = progreso) }
    }

    fun actualizarKilometros(km: Int) {
        viewModelScope.launch {
            val moto = _uiState.value.motos.getOrNull(_uiState.value.selectedIndex) ?: return@launch
            repositorio.actualizarKilometros(moto.idMoto, km)
        }
    }

    fun guardarAjustesDesgaste(idMoto: Long, porcentajes: Map<Long, Int>, onComplete: () -> Unit) {
        viewModelScope.launch {
            for ((tipoId, pct) in porcentajes) {
                val ultimo = repositorio.obtenerUltimoMantenimientoPorTipo(idMoto, tipoId)
                if (ultimo != null) {
                    repositorio.actualizarPorcentajeVida(ultimo.id, pct)
                } else {
                    val tipo = TiposMantenimiento.desdeId(tipoId) ?: continue
                    val now = FORMATO_COMPLETO.format(Date())
                    val nuevo = MantenimientoEntity(
                        idMoto = idMoto,
                        nombreMantenimiento = tipo.nombre,
                        fecha = FORMATO_VISUAL.format(Date()),
                        kms = 0,
                        intervaloKms = 0,
                        precio = "",
                        tiempo = "",
                        notas = "Ajuste de desgaste",
                        tareas = tipo.nombre,
                        creadoEn = now,
                        tipoMantenimientoId = tipoId,
                        porcentajeVida = pct
                    )
                    repositorio.insertarMantenimiento(nuevo)
                }
            }
            onComplete()
        }
    }

    companion object {
        private val FORMATO_COMPLETO = java.text.SimpleDateFormat(DateUtils.FORMATO_FECHA_COMPLETA, java.util.Locale.getDefault())
        private val FORMATO_VISUAL = java.text.SimpleDateFormat(DateUtils.FORMATO_FECHA_VISUAL, java.util.Locale.getDefault())

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[AndroidViewModelFactory.APPLICATION_KEY] as Application
                TorqueDashboardViewModel(
                    repositorio = RepositorioApp.obtenerInstancia(app),
                    perfilRepositorio = PerfilRepositorio(app.dataStore)
                )
            }
        }
    }
}
