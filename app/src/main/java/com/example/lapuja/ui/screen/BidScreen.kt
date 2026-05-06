package com.example.lapuja.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BidScreen(
    onConfirmClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var bidAmount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Hacer oferta", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Producto: iPhone 13")
        Text("Oferta actual: $420.00")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bidAmount,
            onValueChange = { bidAmount = it },
            label = { Text("Nueva oferta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConfirmClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar oferta")
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}