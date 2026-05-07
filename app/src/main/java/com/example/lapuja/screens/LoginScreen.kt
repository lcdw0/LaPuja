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
fun LoginScreen(
    navController: NavController,
    prefs: SharedPreferences
) {

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
            text = "🔐 Iniciar Sesión",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

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

                val savedCorreo =
                    prefs.getString("correo", "")

                val savedPassword =
                    prefs.getString("password", "")

                if (
                    correo == savedCorreo &&
                    password == savedPassword
                ) {

                    navController.navigate("home")
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Entrar")
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(
            onClick = {

                navController.navigate("register")
            }
        ) {

            Text("Crear cuenta")
        }
    }
}