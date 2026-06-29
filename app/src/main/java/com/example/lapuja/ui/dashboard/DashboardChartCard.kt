package com.example.lapuja.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lapuja.data.model.dashboard.DashboardGraficaItemResponse

@Composable
fun DashboardColumnChartCard(
    titulo: String,
    subtitulo: String,
    datos: List<DashboardGraficaItemResponse>,
    modifier: Modifier = Modifier
) {
    DashboardMiniBarChartCard(
        titulo = titulo,
        subtitulo = subtitulo,
        datos = datos,
        modifier = modifier
    )
}

@Composable
fun DashboardLineChartCard(
    titulo: String,
    subtitulo: String,
    datos: List<DashboardGraficaItemResponse>,
    modifier: Modifier = Modifier
) {
    DashboardTrendCard(
        titulo = titulo,
        subtitulo = subtitulo,
        datos = datos,
        modifier = modifier
    )
}

@Composable
private fun DashboardMiniBarChartCard(
    titulo: String,
    subtitulo: String,
    datos: List<DashboardGraficaItemResponse>,
    modifier: Modifier = Modifier
) {
    val datosVisibles = datos.takeLast(7)
    val maxValor = datosVisibles.maxOfOrNull { it.valor } ?: 0.0
    val total = datos.sumOf { it.valor }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ChartHeader(
                titulo = titulo,
                subtitulo = subtitulo,
                total = total
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                datosVisibles.forEach { item ->
                    val porcentaje = if (maxValor <= 0.0) 0.08f else (item.valor / maxValor).toFloat().coerceAtLeast(0.08f)

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = formatearValorCorto(item.valor),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((120 * porcentaje).dp)
                                .clip(MaterialTheme.shapes.large)
                                .background(MaterialTheme.colorScheme.primary)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.etiqueta.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardTrendCard(
    titulo: String,
    subtitulo: String,
    datos: List<DashboardGraficaItemResponse>,
    modifier: Modifier = Modifier
) {
    val datosVisibles = datos.takeLast(7)
    val total = datos.sumOf { it.valor }
    val maxValor = datosVisibles.maxOfOrNull { it.valor } ?: 0.0

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ChartHeader(
                titulo = titulo,
                subtitulo = subtitulo,
                total = total
            )

            datosVisibles.forEach { item ->
                TrendRow(
                    etiqueta = item.etiqueta,
                    valor = item.valor,
                    maxValor = maxValor
                )
            }
        }
    }
}

@Composable
private fun TrendRow(
    etiqueta: String,
    valor: Double,
    maxValor: Double
) {
    val porcentaje = if (maxValor <= 0.0) 0f else (valor / maxValor).toFloat()

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatearCordobas(valor),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { porcentaje },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun ChartHeader(
    titulo: String,
    subtitulo: String,
    total: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatearCordobas(total),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatearCordobas(valor: Double): String {
    return "C$ ${String.format("%.2f", valor)}"
}

private fun formatearValorCorto(valor: Double): String {
    return if (valor >= 1000) {
        "C$ ${String.format("%.1fk", valor / 1000)}"
    } else {
        String.format("%.0f", valor)
    }
}