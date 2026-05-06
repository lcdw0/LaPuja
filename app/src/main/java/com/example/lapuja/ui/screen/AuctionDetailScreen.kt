package com.example.lapuja.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuctionDetailScreen(
    onBidClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Detalle de subasta", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Imagen del producto")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("iPhone 13", style = MaterialTheme.typography.headlineSmall)
        Text("Categoría: Tecnología")
        Text("Descripción: iPhone en excelente estado.")
        Text("Precio inicial: $300.00")
        Text("Oferta actual: $420.00")
        Text("Tiempo restante: 2h 15m")
        Text("Vendedor: Mario")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBidClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hacer oferta")
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}