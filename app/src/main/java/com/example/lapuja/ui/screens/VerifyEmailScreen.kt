package com.example.lapuja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.RetrofitClient

@Composable
fun VerifyEmailScreen(
    token: String,
    navController: NavController
) {
    var cargando by remember { mutableStateOf(true) }
    var ok by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("Verificando correo...") }

    LaunchedEffect(token) {
        try {
            val response = RetrofitClient.api.verificarCorreo(token)

            if (response.isSuccessful) {
                val body = response.body()
                ok = body?.get("ok") == true
                mensaje = body?.get("mensaje")?.toString()
                    ?: if (ok) "Correo verificado correctamente" else "No se pudo verificar el correo"
            } else {
                ok = false
                mensaje = "Error al verificar el correo"
            }
        } catch (e: Exception) {
            ok = false
            mensaje = "No se pudo conectar con el servidor"
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (cargando) {
            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verificando tu correo...",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = if (ok) "Correo verificado" else "Verificación fallida",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            ) {
                Text("Ir al login")
            }
        }
    }
}