package com.example.lapuja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.data.AuctionItem

@Composable
fun AuctionDetailScreen(
    auction: AuctionItem,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Image(
            painter = painterResource(id = auction.imagen),
            contentDescription = auction.nombre,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = auction.nombre,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Categoría: ${auction.categoria}")
        Text("Precio actual: $${auction.precio}")
        Text("Tiempo restante: ${auction.tiempo}s")
        Text("Ganador actual: ${auction.ganador}")
        Text("Estado: ${auction.estado}")
        Text("Ofertas realizadas: ${auction.ofertas}")

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Descripción",
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = auction.descripcion
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                auction.guardado = !auction.guardado
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                if (auction.guardado) "❤️ Guardada" else "🤍 Guardar subasta"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Volver")
        }
    }
}