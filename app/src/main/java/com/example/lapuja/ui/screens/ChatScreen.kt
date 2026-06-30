package com.example.lapuja.ui.screens

import android.content.SharedPreferences
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.lapuja.data.remote.ChatConversacionResponse
import com.example.lapuja.data.remote.ChatMensajeRequest
import com.example.lapuja.data.remote.ChatMensajeResponse
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaResponse
import com.example.lapuja.ui.components.AppImage
import com.example.lapuja.ui.components.AppProfileImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatScreen(
    prefs: SharedPreferences,
    conversacionId: Long,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val usuarioId = prefs.getLong("usuarioId", 0L)
    val context = LocalContext.current

    var conversacion by remember { mutableStateOf<ChatConversacionResponse?>(null) }
    var subasta by remember { mutableStateOf<SubastaResponse?>(null) }
    var mensajes by remember { mutableStateOf<List<ChatMensajeResponse>>(emptyList()) }
    var texto by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var imagenSeleccionada by remember { mutableStateOf<String?>(null) }

    fun cargarConversacion() {
        scope.launch {
            try {
                val response = RetrofitClient.api.listarConversacionesChat(usuarioId)

                if (response.isSuccessful) {
                    val conversacionEncontrada = response.body()
                        ?.firstOrNull { it.id == conversacionId }

                    conversacion = conversacionEncontrada

                    val subastaId = conversacionEncontrada?.subastaId

                    if (subastaId != null) {
                        val responseSubasta = RetrofitClient.api.obtenerSubasta(subastaId)

                        if (responseSubasta.isSuccessful) {
                            subasta = responseSubasta.body()
                        }
                    }
                }
            } catch (e: Exception) {
                mensajeError = "No se pudo cargar la conversación."
            }
        }
    }

    fun cargarMensajes() {
        scope.launch {
            try {
                val response = RetrofitClient.api.listarMensajesChat(conversacionId)

                if (response.isSuccessful) {
                    mensajes = response.body() ?: emptyList()

                    RetrofitClient.api.marcarMensajesChatComoLeidos(
                        conversacionId = conversacionId,
                        usuarioId = usuarioId
                    )

                    if (mensajes.isNotEmpty()) {
                        listState.animateScrollToItem(mensajes.size - 1)
                    }
                }
            } catch (e: Exception) {
                mensajeError = "No se pudieron cargar los mensajes."
            }
        }
    }

    fun enviarMensaje() {
        val contenido = texto.trim()
        if (contenido.isEmpty()) return

        scope.launch {
            try {
                cargando = true
                mensajeError = ""

                val response = RetrofitClient.api.enviarMensajeChat(
                    conversacionId = conversacionId,
                    request = ChatMensajeRequest(
                        emisorId = usuarioId,
                        contenido = contenido
                    )
                )

                if (response.isSuccessful) {
                    texto = ""
                    cargarMensajes()
                    cargarConversacion()
                } else {
                    mensajeError = "No se pudo enviar el mensaje."
                }
            } catch (e: Exception) {
                mensajeError = "No se pudo conectar con la API."
            } finally {
                cargando = false
            }
        }
    }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    cargando = true
                    mensajeError = ""

                    val multipart = crearMultipartDesdeUri(
                        context = context,
                        uri = it,
                        fieldName = "file"
                    )

                    val uploadResponse = RetrofitClient.api.subirImagenChat(multipart)

                    if (uploadResponse.isSuccessful && uploadResponse.body()?.ok == true) {
                        val imageUrl = uploadResponse.body()?.url

                        if (!imageUrl.isNullOrBlank()) {
                            val mensajeResponse = RetrofitClient.api.enviarMensajeChat(
                                conversacionId = conversacionId,
                                request = ChatMensajeRequest(
                                    emisorId = usuarioId,
                                    contenido = null,
                                    tipoMensaje = "IMAGEN",
                                    imagenUrl = imageUrl
                                )
                            )

                            if (mensajeResponse.isSuccessful) {
                                cargarMensajes()
                                cargarConversacion()
                            } else {
                                mensajeError = "No se pudo enviar la imagen."
                            }
                        }
                    } else {
                        mensajeError = uploadResponse.body()?.mensaje ?: "No se pudo subir la imagen."
                    }
                } catch (e: Exception) {
                    mensajeError = "No se pudo seleccionar o subir la imagen."
                } finally {
                    cargando = false
                }
            }
        }
    }

    LaunchedEffect(conversacionId) {
        cargarConversacion()
        cargarMensajes()

        while (true) {
            delay(4000)
            cargarMensajes()
            cargarConversacion()
        }
    }

    val otroUsuario = conversacion?.let {
        if (usuarioId == it.compradorId) {
            it.vendedorNombre ?: "Vendedor"
        } else {
            it.compradorNombre ?: "Comprador"
        }
    } ?: "Chat"

    val otroUsuarioId = conversacion?.let {
        if (usuarioId == it.compradorId) {
            it.vendedorId
        } else {
            it.compradorId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090D14),
                        Color(0xFF121826),
                        Color(0xFF090D14)
                    )
                )
            )
    ) {
        ChatHeader(
            nombre = otroUsuario,
            fotoPerfil = conversacion?.otroUsuarioFotoPerfil,
            subasta = conversacion?.subastaTitulo ?: "Subasta",
            enLinea = conversacion?.otroUsuarioEnLinea == true,
            onBackClick = {
                navController.navigate("chat_list") {
                    popUpTo("chat_list") {
                        inclusive = false
                    }
                }
            },
            onVerPerfil = {
                otroUsuarioId?.let {
                    navController.navigate("public_profile/$it")
                }
            }
        )

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = Color(0xFFFF6B81),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )
        }

        AuctionChatCard(
            subasta = subasta,
            tituloFallback = conversacion?.subastaTitulo ?: "Subasta relacionada",
            onClick = {
                val id = subasta?.id ?: conversacion?.subastaId
                if (id != null) {
                    navController.navigate("auction_detail/$id")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            state = listState
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                DateChip(text = "Hoy")
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(mensajes) { mensaje ->
                MensajeBurbujaSocial(
                    mensaje = mensaje,
                    esMio = mensaje.emisorId == usuarioId,
                    onImageClick = { imageUrl ->
                        imagenSeleccionada = imageUrl
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        ChatInputBar(
            texto = texto,
            cargando = cargando,
            onTextoChange = { texto = it },
            onEnviar = { enviarMensaje() },
            onImagenClick = {
                galeriaLauncher.launch("image/*")
            }
        )

        if (!imagenSeleccionada.isNullOrBlank()) {
            ImagenChatPantallaCompleta(
                imageUrl = imagenSeleccionada!!,
                onCerrar = {
                    imagenSeleccionada = null
                }
            )
        }
    }
}

@Composable
private fun ChatHeader(
    nombre: String,
    fotoPerfil: String?,
    subasta: String,
    enLinea: Boolean,
    onBackClick: () -> Unit,
    onVerPerfil: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Regresar",
                tint = Color.White
            )
        }

        AppProfileImage(
            imageUrl = fotoPerfil,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                color = Color.White,
                fontSize = 23.sp
            )

            Text(
                text = if (enLinea) "● En línea" else "● Desconectado",
                color = if (enLinea) Color(0xFF2ECC71) else Color.Gray,
                fontSize = 14.sp
            )
        }

        var menuExpanded by remember { mutableStateOf(false) }

        Box {
            IconButton(
                onClick = {
                    menuExpanded = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = {
                    menuExpanded = false
                }
            ) {
                DropdownMenuItem(
                    text = { Text("Ver perfil") },
                    onClick = {
                        menuExpanded = false
                        onVerPerfil()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Reportar usuario (Próximamente)") },
                    onClick = {
                        menuExpanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Bloquear usuario (Próximamente)") },
                    onClick = {
                        menuExpanded = false
                    }
                )
            }
        }
    }

    Text(
        text = "Conversación sobre: $subasta",
        color = Color.LightGray,
        fontSize = 13.sp,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun AuctionChatCard(
    subasta: SubastaResponse?,
    tituloFallback: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF6C63FF).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                if (!subasta?.imagen.isNullOrBlank()) {
                    AppImage(
                        imageUrl = subasta?.imagen,
                        contentDescription = subasta?.nombre ?: tituloFallback,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("🔥", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sobre la subasta",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                Text(
                    text = subasta?.nombre ?: tituloFallback,
                    color = Color.White,
                    fontSize = 18.sp
                )

                Text(
                    text = "$${subasta?.precioActual ?: 0.0} · ${subasta?.estado ?: "Chat activo"}",
                    color = Color(0xFF2ECC71),
                    fontSize = 13.sp
                )
            }

            Text(
                text = "Ver",
                color = Color(0xFFB6A7FF),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun DateChip(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.12f)
        ) {
            Text(
                text = text,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MensajeBurbujaSocial(
    mensaje: ChatMensajeResponse,
    esMio: Boolean,
    onImageClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!esMio) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = Color(0xFF6C63FF).copy(alpha = 0.25f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = mensaje.emisorNombre?.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 290.dp),
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp,
                bottomStart = if (esMio) 22.dp else 4.dp,
                bottomEnd = if (esMio) 4.dp else 22.dp
            ),
            color = if (esMio) Color(0xFF6C63FF) else Color.White.copy(alpha = 0.13f)
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 14.dp,
                    end = 14.dp,
                    top = 10.dp,
                    bottom = 8.dp
                )
            ) {
                if (!esMio) {
                    Text(
                        text = mensaje.emisorNombre ?: "Usuario",
                        fontSize = 12.sp,
                        color = Color(0xFFB6A7FF)
                    )

                    Spacer(modifier = Modifier.height(3.dp))
                }

                if (mensaje.tipoMensaje == "IMAGEN" && !mensaje.imagenUrl.isNullOrBlank()) {
                    AppImage(
                        imageUrl = mensaje.imagenUrl,
                        contentDescription = "Imagen del chat",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                mensaje.imagenUrl?.let { onImageClick(it) }
                            }
                    )

                    if (!mensaje.contenido.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = mensaje.contenido,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = mensaje.contenido ?: "",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatearHoraMensaje(mensaje.fechaEnvio),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.68f)
                    )

                    if (esMio) {
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = if (mensaje.leido == true) "✓✓" else "✓",
                            fontSize = 11.sp,
                            color = if (mensaje.leido == true) {
                                Color(0xFF9ED0FF)
                            } else {
                                Color.White.copy(alpha = 0.65f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    texto: String,
    cargando: Boolean,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onImagenClick: () -> Unit
) {
    Surface(
        color = Color(0xFF090D14),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clickable { onImagenClick() },
                shape = CircleShape,
                color = Color(0xFF6C63FF)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "🖼️",
                        fontSize = 22.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = texto,
                onValueChange = onTextoChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Escribe un mensaje...", color = Color.Gray)
                },
                shape = RoundedCornerShape(50),
                singleLine = false,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.20f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                    focusedContainerColor = Color.White.copy(alpha = 0.08f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                    cursorColor = Color.White
                ),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onEnviar,
                enabled = texto.trim().isNotEmpty() && !cargando,
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C63FF),
                    disabledContainerColor = Color.White.copy(alpha = 0.15f)
                )
            ) {
                Text("➤", color = Color.White, fontSize = 22.sp)
            }
        }
    }
}

private fun formatearHoraMensaje(fecha: String?): String {
    if (fecha.isNullOrBlank()) return ""

    return try {
        val limpia = fecha.substringBefore(".")
        val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val salida = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = entrada.parse(limpia)
        salida.format(date!!)
    } catch (e: Exception) {
        ""
    }
}

private fun crearMultipartDesdeUri(
    context: android.content.Context,
    uri: Uri,
    fieldName: String
): MultipartBody.Part {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("No se pudo leer la imagen")

    val tempFile = File.createTempFile("chat_image_", ".jpg", context.cacheDir)

    tempFile.outputStream().use { output ->
        inputStream.copyTo(output)
    }

    val requestBody = tempFile
        .asRequestBody("image/*".toMediaTypeOrNull())

    return MultipartBody.Part.createFormData(
        fieldName,
        tempFile.name,
        requestBody
    )
}

@Composable
private fun ImagenChatPantallaCompleta(
    imageUrl: String,
    onCerrar: () -> Unit
) {
    Dialog(onDismissRequest = onCerrar) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AppImage(
                imageUrl = imageUrl,
                contentDescription = "Imagen ampliada",
                modifier = Modifier.fillMaxSize()
            )

            Button(
                onClick = onCerrar,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cerrar")
            }
        }
    }
}