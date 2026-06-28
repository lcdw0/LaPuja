package com.example.lapuja.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.CancelarSubastaRequest
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaResponse
import kotlinx.coroutines.launch

@Composable
fun OwnerActionsSection(
    auction: SubastaResponse,
    usuarioId: Long,
    navController: NavController,
    onSubastaActualizada: () -> Unit,
    onMensaje: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var mostrarDialogoCancelar by remember { mutableStateOf(false) }

    val esPropietario = auction.usuarioId == usuarioId
    val puedeEditarOCancelar = esPropietario &&
            auction.estado == "ACTIVA" &&
            auction.ofertas == 0

    val puedeFinalizar = esPropietario &&
            auction.estado == "ACTIVA" &&
            auction.ofertas > 0

    if (!esPropietario) return

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (puedeEditarOCancelar) {
            Button(
                onClick = {
                    navController.navigate("edit_auction/${auction.id}")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Editar subasta")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    mostrarDialogoCancelar = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancelar subasta")
            }
        }

        if (puedeFinalizar) {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.finalizarSubasta(auction.id)

                            if (response.isSuccessful) {
                                onMensaje("Subasta finalizada correctamente.")
                                onSubastaActualizada()
                            } else {
                                onMensaje("No se pudo finalizar la subasta.")
                            }
                        } catch (e: Exception) {
                            onMensaje("No se pudo conectar con la API.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Finalizar subasta")
            }
        }

        if (!puedeEditarOCancelar && !puedeFinalizar) {
            Text(
                text = "Esta subasta pertenece a tu cuenta.",
                color = MaterialTheme.colorScheme.outline
            )
        }
    }

    if (mostrarDialogoCancelar) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCancelar = false
            },
            title = {
                Text("Cancelar subasta")
            },
            text = {
                Text("¿Seguro que deseas cancelar esta subasta? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCancelar = false

                        scope.launch {
                            try {
                                val response = RetrofitClient.api.cancelarSubasta(
                                    id = auction.id,
                                    request = CancelarSubastaRequest(usuarioId = usuarioId)
                                )

                                if (response.isSuccessful) {
                                    onMensaje("Subasta cancelada correctamente.")
                                    onSubastaActualizada()
                                } else {
                                    onMensaje("No se pudo cancelar la subasta.")
                                }
                            } catch (e: Exception) {
                                onMensaje("No se pudo conectar con la API.")
                            }
                        }
                    }
                ) {
                    Text("Sí, cancelar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCancelar = false
                    }
                ) {
                    Text("Volver")
                }
            }
        )
    }
}