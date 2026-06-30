package com.example.lapuja.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.data.remote.SubastaResponse

@Composable
fun BidSection(
    auction: SubastaResponse,
    usuarioId: Long,
    saldoActual: Double,
    montoPersonalizado: String,
    onMontoChange: (String) -> Unit,
    onPujar: (Double) -> Unit,
    onMensaje: (String) -> Unit,
    puedePujar: Boolean
) {

    val esPropietario = auction.usuarioId == usuarioId

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Saldo disponible",
                fontSize = 15.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$${String.format("%.2f", saldoActual)}",
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (esPropietario) {

                Text(
                    text = "Esta es tu subasta.",
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "No puedes ofertar en tu propia publicación.",
                    color = Color.Gray
                )

            } else if (puedePujar) {

                Button(
                    onClick = {
                        onPujar(auction.precioActual + 0.5)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("🔥 Pujar +$0.50")
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = montoPersonalizado,
                    onValueChange = {
                        onMontoChange(it)
                    },
                    label = {
                        Text("Monto personalizado")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {

                        val monto =
                            montoPersonalizado.toDoubleOrNull()

                        if (monto == null) {
                            onMensaje("Ingrese un monto válido.")
                            return@OutlinedButton
                        }

                        onPujar(monto)

                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Ofertar monto personalizado")
                }

            } else {

                Text(
                    text = "Esta subasta ya finalizó. No se pueden realizar más pujas.",
                    color = Color.Gray,
                    fontSize = 16.sp
                )

            }
        }
    }
}