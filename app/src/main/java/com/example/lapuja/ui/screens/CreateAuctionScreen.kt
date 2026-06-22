package com.example.lapuja.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.R
import com.example.lapuja.data.AuctionItem

@Composable
fun CreateAuctionScreen(
    productos: MutableList<AuctionItem>,
    onAuctionCreated: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("Hoy") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Crear subasta",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio inicial") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = fechaInicio,
            onValueChange = { fechaInicio = it },
            label = { Text("Fecha de inicio") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val precioConvertido = precio.toDoubleOrNull()

                if (nombre.isBlank() || descripcion.isBlank() || precio.isBlank() || categoria.isBlank()) {
                    error = "Completa todos los campos."
                    return@Button
                }

                if (precioConvertido == null || precioConvertido <= 0) {
                    error = "Ingresa un precio válido."
                    return@Button
                }

                val nuevaSubasta = AuctionItem(
                    nombre = nombre,
                    descripcion = descripcion,
                    precioInicial = precioConvertido,
                    imagen = R.drawable.iphone,
                    categoria = categoria,
                    fechaInicio = fechaInicio
                ).apply {
                    estado = "ACTIVA"
                    iniciada = true
                    tiempo = 60
                }

                productos.add(nuevaSubasta)

                nombre = ""
                descripcion = ""
                precio = ""
                categoria = ""
                fechaInicio = "Hoy"
                error = ""

                onAuctionCreated()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Publicar subasta")
        }
    }
}