package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lapuja.data.remote.ChatConversacionResponse
import com.example.lapuja.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun ChatListScreen(
    prefs: SharedPreferences,
    onChatClick: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    val usuarioId = prefs.getLong("usuarioId", 0L)

    var conversaciones by remember {
        mutableStateOf<List<ChatConversacionResponse>>(emptyList())
    }

    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    fun cargarConversaciones() {
        scope.launch {
            try {
                cargando = true
                mensaje = ""

                val response = RetrofitClient.api.listarConversacionesChat(usuarioId)

                if (response.isSuccessful) {
                    conversaciones = response.body() ?: emptyList()
                } else {
                    mensaje = "No se pudieron cargar las conversaciones."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarConversaciones()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text("Chats", fontSize = 30.sp)

            Spacer(modifier = Modifier.height(12.dp))

            if (cargando) {
                Text("Cargando conversaciones...")
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (mensaje.isNotEmpty()) {
                Text(mensaje, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (!cargando && conversaciones.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("No tenés conversaciones", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cuando ganés o vendás una subasta, aparecerá aquí.")
                    }
                }
            }
        }

        items(conversaciones) { conversacion ->
            ChatConversacionItem(
                conversacion = conversacion,
                usuarioId = usuarioId,
                onClick = {
                    conversacion.id?.let { onChatClick(it) }
                }
            )
        }
    }
}

@Composable
private fun ChatConversacionItem(
    conversacion: ChatConversacionResponse,
    usuarioId: Long,
    onClick: () -> Unit
) {
    val otroUsuario = if (usuarioId == conversacion.compradorId) {
        conversacion.vendedorNombre ?: "Vendedor"
    } else {
        conversacion.compradorNombre ?: "Comprador"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = otroUsuario.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(otroUsuario, fontSize = 19.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = conversacion.subastaTitulo ?: "Subasta",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = conversacion.ultimoMensaje ?: "Sin mensajes todavía",
                    fontSize = 14.sp
                )
            }

            val noLeidos = conversacion.mensajesNoLeidos ?: 0L

            if (noLeidos > 0) {
                Badge {
                    Text(noLeidos.toString())
                }
            }
        }
    }
}