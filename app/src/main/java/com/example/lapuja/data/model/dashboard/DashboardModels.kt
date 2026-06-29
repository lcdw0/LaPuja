package com.example.lapuja.data.model.dashboard

data class DashboardResumenResponse(
    val totalSubastasCreadas: Long,
    val totalPujasRealizadas: Long,
    val subastasGanadas: Long,
    val subastasPerdidas: Long,
    val subastasVendidas: Long,
    val subastasCanceladas: Long,
    val dineroGastado: Double,
    val dineroGanado: Double,
    val totalRecargado: Double,
    val totalRetirado: Double,
    val tarjetasRegistradas: Long,
    val porcentajeVictorias: Double
)

data class DashboardGraficasResponse(
    val graficaCompras: List<DashboardGraficaItemResponse>,
    val graficaVentas: List<DashboardGraficaItemResponse>,
    val graficaSemanal: List<DashboardGraficaItemResponse>,
    val resumenMensual: List<DashboardGraficaItemResponse>
)

data class DashboardGraficaItemResponse(
    val etiqueta: String,
    val valor: Double
)

data class DashboardActividadResponse(
    val tipo: String,
    val titulo: String,
    val descripcion: String?,
    val monto: Double?,
    val fecha: String?
)