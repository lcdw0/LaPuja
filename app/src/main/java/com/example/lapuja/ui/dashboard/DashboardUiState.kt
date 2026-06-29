package com.example.lapuja.ui.dashboard

import com.example.lapuja.data.model.dashboard.DashboardActividadResponse
import com.example.lapuja.data.model.dashboard.DashboardGraficasResponse
import com.example.lapuja.data.model.dashboard.DashboardResumenResponse

data class DashboardUiState(
    val cargando: Boolean = false,
    val refrescando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val resumen: DashboardResumenResponse? = null,
    val graficas: DashboardGraficasResponse? = null,
    val actividad: List<DashboardActividadResponse> = emptyList()
)