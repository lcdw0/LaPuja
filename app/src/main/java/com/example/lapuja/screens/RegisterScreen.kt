package com.example.lapuja.screens

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun RegisterScreen(
    navController: NavController,
    prefs: SharedPreferences
) {

    var nombre by remember {

        mutableStateOf("")
    }

    var correo by remember {

        mutableStateOf("")
    }

    var password by remember {

        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "📝 Crear Cuenta",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
            },
            label = {
                Text("Nombre")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
            },
            label = {
                Text("Correo")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Contraseña")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {

                prefs.edit()
                    .putString("nombre", nombre)
                    .putString("correo", correo)
                    .putString("password", password)
                    .apply()

                navController.navigate("login")
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Registrarse")
        }
    }
}