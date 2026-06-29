package com.example.lapuja.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lapuja.data.remote.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    fun cargarDashboard(usuarioId: Long) {
        cargarDatos(usuarioId, esRefresh = false)
    }

    fun refrescarDashboard(usuarioId: Long) {
        cargarDatos(usuarioId, esRefresh = true)
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }

    private fun cargarDatos(usuarioId: Long, esRefresh: Boolean) {
        viewModelScope.launch {
            val estadoActual = _uiState.value

            _uiState.value = estadoActual.copy(
                cargando = !esRefresh && estadoActual.resumen == null,
                refrescando = esRefresh,
                error = null,
                mensaje = null
            )

            try {
                val resumenDeferred = async {
                    api.obtenerDashboardResumen(usuarioId)
                }

                val graficasDeferred = async {
                    api.obtenerDashboardGraficas(usuarioId)
                }

                val actividadDeferred = async {
                    api.obtenerDashboardActividad(usuarioId)
                }

                _uiState.value = DashboardUiState(
                    cargando = false,
                    refrescando = false,
                    error = null,
                    mensaje = if (esRefresh) "Dashboard actualizado correctamente" else null,
                    resumen = resumenDeferred.await(),
                    graficas = graficasDeferred.await(),
                    actividad = actividadDeferred.await()
                )

            } catch (e: Exception) {
                _uiState.value = estadoActual.copy(
                    cargando = false,
                    refrescando = false,
                    error = if (estadoActual.resumen == null) {
                        e.message ?: "No se pudo cargar el dashboard"
                    } else {
                        null
                    },
                    mensaje = if (estadoActual.resumen != null) {
                        "No fue posible actualizar"
                    } else {
                        null
                    }
                )
            }
        }
    }
}