package com.example.lapuja.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lapuja.data.model.dashboard.DashboardActividadResponse
import com.example.lapuja.data.model.dashboard.DashboardGraficaItemResponse
import com.example.lapuja.data.model.dashboard.DashboardResumenResponse
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import com.example.lapuja.ui.navigation.Routes
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    usuarioId: Long,
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(usuarioId) {
        viewModel.cargarDashboard(usuarioId)
    }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        when {
            uiState.cargando -> {
                Box(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    DashboardLoading()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    DashboardError(
                        mensaje = uiState.error ?: "No se pudo cargar el dashboard",
                        onRetry = {
                            viewModel.cargarDashboard(usuarioId)
                        }
                    )
                }
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.refrescando,
                    onRefresh = {
                        viewModel.refrescarDashboard(usuarioId)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            DashboardHeader()
                        }

                        uiState.resumen?.let { resumen ->
                            item {
                                DashboardStatsGrid(
                                    resumen = resumen,
                                    navController = navController
                                )
                            }
                        }

                        uiState.graficas?.let { graficas ->
                            item {
                                DashboardResumenMensual(graficas.resumenMensual)
                            }

                            item {
                                DashboardColumnChartCard(
                                    titulo = "Actividad semanal",
                                    subtitulo = "Subastas creadas esta semana",
                                    datos = graficas.graficaSemanal
                                )
                            }

                            item {
                                DashboardLineChartCard(
                                    titulo = "Compras del mes",
                                    subtitulo = "Dinero gastado por día",
                                    datos = graficas.graficaCompras
                                )
                            }

                            item {
                                DashboardLineChartCard(
                                    titulo = "Ventas del mes",
                                    subtitulo = "Dinero ganado por día",
                                    datos = graficas.graficaVentas
                                )
                            }
                        }

                        item {
                            SectionTitle("Actividad reciente")
                        }

                        if (uiState.actividad.isEmpty()) {
                            item {
                                EmptyActivityCard()
                            }
                        } else {
                            items(uiState.actividad) { actividad ->
                                ActividadItemCard(actividad)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Resumen inteligente de tu actividad en LaPuja",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DashboardLoading() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(180.dp)
                        .height(32.dp)
                )

                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(18.dp)
                )
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SkeletonCard(modifier = Modifier.weight(1f))
                        SkeletonCard(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            SkeletonWideCard(height = 120.dp)
        }

        item {
            SkeletonWideCard(height = 240.dp)
        }

        item {
            SkeletonWideCard(height = 240.dp)
        }
    }
}

@Composable
private fun SkeletonCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(132.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
            )

            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(24.dp)
            )
        }
    }
}

@Composable
private fun SkeletonWideCard(
    height: androidx.compose.ui.unit.Dp
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
            )

            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            )

            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
            )
        }
    }
}

@Composable
private fun SkeletonBox(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    )
}

@Composable
private fun DashboardError(
    mensaje: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No se pudo cargar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        }
    }
}

@Composable
private fun DashboardStatsGrid(
    resumen: DashboardResumenResponse,
    navController: NavController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DashboardStatCard(
                icono = "📦",
                titulo = "Subastas",
                valor = resumen.totalSubastasCreadas.toString(),
                detalle = "Creadas",
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(Routes.MY_AUCTIONS)
                }
            )

            DashboardStatCard(
                icono = "🔥",
                titulo = "Pujas",
                valor = resumen.totalPujasRealizadas.toString(),
                detalle = "Realizadas",
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(Routes.MY_BIDS)
                }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DashboardStatCard(
                icono = "🏆",
                titulo = "Ganadas",
                valor = resumen.subastasGanadas.toString(),
                detalle = "Como comprador",
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate("history")
                }
            )

            DashboardStatCard(
                icono = "💼",
                titulo = "Vendidas",
                valor = resumen.subastasVendidas.toString(),
                detalle = "Como vendedor",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DashboardStatCard(
                icono = "🛒",
                titulo = "Gastado",
                valor = formatearCordobas(resumen.dineroGastado),
                detalle = "Compras",
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(Routes.WALLET)
                }
            )

            DashboardStatCard(
                icono = "💰",
                titulo = "Ganado",
                valor = formatearCordobas(resumen.dineroGanado),
                detalle = "Ventas",
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(Routes.WALLET)
                }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = "Porcentaje de victorias",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "${String.format("%.2f", resumen.porcentajeVictorias)}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = "🎯",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    icono: String,
    titulo: String,
    valor: String,
    detalle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() }
            else Modifier
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icono)
            }

            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = detalle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardResumenMensual(
    datos: List<DashboardGraficaItemResponse>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionTitle("Resumen mensual")

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ResumenMensualMiniCard(
                icono = "🛒",
                titulo = "Compras",
                valor = valorPorEtiqueta(datos, "Compras"),
                modifier = Modifier.weight(1f)
            )

            ResumenMensualMiniCard(
                icono = "💵",
                titulo = "Ventas",
                valor = valorPorEtiqueta(datos, "Ventas"),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ResumenMensualMiniCard(
                icono = "💳",
                titulo = "Recargas",
                valor = valorPorEtiqueta(datos, "Recargas"),
                modifier = Modifier.weight(1f)
            )

            ResumenMensualMiniCard(
                icono = "🏦",
                titulo = "Retiros",
                valor = valorPorEtiqueta(datos, "Retiros"),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ResumenMensualMiniCard(
    icono: String,
    titulo: String,
    valor: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icono)

            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatearCordobas(valor),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionTitle(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun ActividadItemCard(
    actividad: DashboardActividadResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconoActividad(actividad.tipo))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = actividad.titulo,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )

                actividad.descripcion?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                actividad.monto?.let {
                    Text(
                        text = formatearCordobas(it),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyActivityCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "🕊️",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Todavía no hay actividad reciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Cuando pujés, ganés, vendás o recargués saldo, aparecerá aquí.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun valorPorEtiqueta(
    datos: List<DashboardGraficaItemResponse>,
    etiqueta: String
): Double {
    return datos.firstOrNull { it.etiqueta.equals(etiqueta, ignoreCase = true) }?.valor ?: 0.0
}

private fun formatearCordobas(valor: Double?): String {
    return "C$ ${String.format("%.2f", valor ?: 0.0)}"
}

private fun iconoActividad(tipo: String?): String {
    return when (tipo) {
        "RECARGA" -> "💳"
        "REEMBOLSO" -> "↩️"
        "PUJA" -> "🔥"
        "VENTA" -> "💰"
        "PAGO_FINAL" -> "🛒"
        "RETIRO" -> "🏦"
        "SUBASTA" -> "📦"
        "GANADA" -> "🏆"
        else -> "🔔"
    }
}