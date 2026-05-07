package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AddPaymentMethodScreen(
    navController: NavController,
    prefs: SharedPreferences
) {

    var nombreTitular by remember { mutableStateOf("") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var fechaExpiracion by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF121212),
                        Color(0xFF1E1E1E)
                    )
                )
            )
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Agregar método de pago",
                            fontSize = 30.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = nombreTitular,
                            onValueChange = {
                                nombreTitular = it
                                mensaje = ""
                            },
                            label = {
                                Text("Nombre del titular")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = numeroTarjeta,
                            onValueChange = {
                                numeroTarjeta = it
                                mensaje = ""
                            },
                            label = {
                                Text("Número de tarjeta")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = fechaExpiracion,
                            onValueChange = {
                                fechaExpiracion = it
                                mensaje = ""
                            },
                            label = {
                                Text("Fecha expiración")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = cvv,
                            onValueChange = {
                                cvv = it
                                mensaje = ""
                            },
                            label = {
                                Text("CVV")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        if (mensaje.isNotEmpty()) {

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = mensaje,
                                color = Color.Red
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {

                                mensaje =
                                    if (
                                        nombreTitular.isBlank() ||
                                        numeroTarjeta.isBlank() ||
                                        fechaExpiracion.isBlank() ||
                                        cvv.isBlank()
                                    ) {
                                        "Completá todos los campos"
                                    } else {

                                        prefs.edit()
                                            .putString("tarjeta_nombre", nombreTitular)
                                            .putString("tarjeta_numero", numeroTarjeta)
                                            .putString("tarjeta_fecha", fechaExpiracion)
                                            .apply()

                                        navController.popBackStack()

                                        ""
                                    }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {

                            Text("Guardar método.")
                        }
                    }
                }
            }
        }
    }
}