package com.example.lapuja.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.FavoritoResponse
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaResponse
import com.example.lapuja.ui.components.AppImage
import kotlinx.coroutines.launch

@Composable
fun SavedAuctionsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    val usuarioId = prefs.getLong("usuarioId", 0L)
    val scope = rememberCoroutineScope()

    val subastas = remember { mutableStateListOf<SubastaResponse>() }
    var favoritosMap by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }

    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }

    fun cargarFavoritos() {
        scope.launch {
            try {
                cargando = true
                mensaje = ""

                if (usuarioId == 0L) {
                    mensaje = "Debes iniciar sesión para ver tus favoritos."
                    subastas.clear()
                    return@launch
                }

                val favoritosResponse = RetrofitClient.api.listarFavoritos(usuarioId)

                if (!favoritosResponse.isSuccessful) {
                    mensaje = "No se pudieron cargar los favoritos."
                    return@launch
                }

                val favoritos = favoritosResponse.body() ?: emptyList()

                favoritosMap = favoritos
                    .filter { it.subastaId != null && it.id != null }
                    .associate { it.subastaId!! to it.id!! }

                subastas.clear()

                favoritos.forEach { favorito: FavoritoResponse ->
                    favorito.subastaId?.let { id ->
                        val subastaResponse = RetrofitClient.api.obtenerSubasta(id)

                        if (subastaResponse.isSuccessful) {
                            subastaResponse.body()?.let { subasta ->
                                subastas.add(subasta)
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                mensaje = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarFavoritos()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(
                text = "❤️ Subastas guardadas",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Las subastas que marcaste como favoritas.",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        when {
            cargando -> {
                item {
                    Text(
                        text = "Cargando...",
                        color = Color.Gray
                    )
                }
            }

            mensaje.isNotEmpty() -> {
                item {
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            subastas.isEmpty() -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Todavía no tienes favoritos.",
                                fontSize = 20.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Guarda una subasta para verla aquí.",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            else -> {
                items(subastas) { subasta ->
                    val favoritoId = favoritosMap[subasta.id]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable {
                                navController.navigate("auction_detail/${subasta.id}")
                            },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column {
                            AppImage(
                                imageUrl = subasta.imagen,
                                contentDescription = subasta.nombre,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = subasta.nombre,
                                            fontSize = 22.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = subasta.categoria,
                                            color = Color.Gray
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    if (favoritoId != null) {
                                                        val response =
                                                            RetrofitClient.api.eliminarFavorito(favoritoId)

                                                        if (response.isSuccessful) {
                                                            subastas.remove(subasta)
                                                            favoritosMap =
                                                                favoritosMap - subasta.id
                                                        } else {
                                                            mensaje = "No se pudo quitar de favoritos."
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    mensaje = "No se pudo conectar con la API."
                                                }
                                            }
                                        }
                                    ) {
                                        Text("❤️")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Precio actual: $${subasta.precioActual}"
                                )

                                Text(
                                    text = "Ofertas: ${subasta.ofertas}"
                                )

                                Text(
                                    text = "Ganador: ${subasta.ganador}"
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = subasta.estado,
                                    color = when (subasta.estado) {
                                        "ACTIVA" -> Color(0xFF4CAF50)
                                        "PROGRAMADA" -> Color(0xFFFFC107)
                                        else -> Color.Red
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}