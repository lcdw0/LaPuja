package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ProfileScreen(
    prefs: SharedPreferences,
    navController: NavController
) {

    val nombre =
        prefs.getString("nombre", "Usuario")

    val saldo =
        prefs.getFloat("saldo", 0.0f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "👤 Perfil",
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Nombre: $nombre",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Saldo: $$saldo",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 🚪 CERRAR SESIÓN
        Button(
            onClick = {

                prefs.edit()
                    .remove("correo")
                    .remove("password")
                    .apply()

                navController.navigate("login")
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Cerrar Sesión")
        }
    }
}