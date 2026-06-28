package com.example.lapuja.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.UsuarioUpdateRequest
import com.example.lapuja.ui.components.AppProfileImage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun EditProfileScreen(
    prefs: SharedPreferences,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val usuarioId = prefs.getLong("usuarioId", 0L)

    var nombre by remember { mutableStateOf(prefs.getString("nombre", "") ?: "") }
    var correo by remember { mutableStateOf(prefs.getString("correo", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var fotoPerfil by remember { mutableStateOf(prefs.getString("fotoPerfil", null)) }
    var imagenNuevaUri by remember { mutableStateOf<Uri?>(null) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    fun crearParteImagen(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: return null

            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())

            MultipartBody.Part.createFormData(
                name = "file",
                filename = "perfil_${System.currentTimeMillis()}.jpg",
                body = requestBody
            )
        } catch (e: Exception) {
            null
        }
    }

    val launcherImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            imagenNuevaUri = uri
            fotoPerfil = uri.toString()
            mensaje = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Editar perfil",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AppProfileImage(
                imageUrl = fotoPerfil,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                launcherImagen.launch(arrayOf("image/*"))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (fotoPerfil == null) "Seleccionar foto de perfil" else "Cambiar foto de perfil")
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                mensaje = ""
            },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                mensaje = ""
            },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                mensaje = ""
            },
            label = { Text("Nueva contraseña opcional") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = mensaje,
                color = if (mensaje.contains("correctamente")) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (nombre.isBlank() || correo.isBlank()) {
                    mensaje = "Completa nombre y correo."
                    return@Button
                }

                if (usuarioId == 0L) {
                    mensaje = "No se encontró el usuario activo."
                    return@Button
                }

                cargando = true
                mensaje = ""

                scope.launch {
                    try {
                        var fotoFinal = fotoPerfil

                        if (imagenNuevaUri != null) {
                            val parteImagen = crearParteImagen(context, imagenNuevaUri!!)

                            if (parteImagen == null) {
                                mensaje = "No se pudo preparar la imagen."
                                cargando = false
                                return@launch
                            }

                            val responseImagen = RetrofitClient.api.subirImagenPerfil(parteImagen)

                            if (!responseImagen.isSuccessful || responseImagen.body()?.ok != true) {
                                mensaje = responseImagen.body()?.mensaje ?: "No se pudo subir la foto de perfil."
                                cargando = false
                                return@launch
                            }

                            fotoFinal = responseImagen.body()?.url

                            if (fotoFinal.isNullOrBlank()) {
                                mensaje = "La API no devolvió la URL de la foto."
                                cargando = false
                                return@launch
                            }
                        }

                        val response = RetrofitClient.api.actualizarUsuario(
                            id = usuarioId,
                            request = UsuarioUpdateRequest(
                                nombre = nombre,
                                correo = correo,
                                password = password,
                                fotoPerfil = fotoFinal
                            )
                        )

                        if (response.isSuccessful) {
                            val usuario = response.body()

                            if (usuario?.ok == true) {
                                prefs.edit()
                                    .putString("nombre", usuario.nombre ?: nombre)
                                    .putString("correo", usuario.correo ?: correo)
                                    .putString("fotoPerfil", usuario.fotoPerfil ?: fotoFinal)
                                    .apply()

                                fotoPerfil = usuario.fotoPerfil ?: fotoFinal
                                imagenNuevaUri = null
                                mensaje = "Perfil actualizado correctamente."
                            } else {
                                mensaje = usuario?.mensaje ?: "No se pudo actualizar el perfil."
                            }
                        } else {
                            mensaje = "No se pudo actualizar el perfil."
                        }
                    } catch (e: Exception) {
                        mensaje = "No se pudo conectar con la API."
                    } finally {
                        cargando = false
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (cargando) "Guardando..." else "Guardar cambios")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Volver")
        }
    }
}