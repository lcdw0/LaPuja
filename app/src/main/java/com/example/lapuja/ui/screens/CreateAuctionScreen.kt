package com.example.lapuja.ui.screens

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.lapuja.data.AuctionItem
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAuctionScreen(
    productos: MutableList<AuctionItem>,
    onAuctionCreated: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
    val usuarioId = prefs.getLong("usuarioId", 0L)
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    var categoriaExpandida by remember { mutableStateOf(false) }
    var mostrarFecha by remember { mutableStateOf(false) }
    var mostrarHora by remember { mutableStateOf(false) }

    var fechaMillis by remember { mutableStateOf<Long?>(null) }
    var horaSeleccionada by remember { mutableStateOf<Int?>(null) }
    var minutoSeleccionado by remember { mutableStateOf<Int?>(null) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val categorias = listOf(
        "Tecnología", "Computadoras", "Celulares", "Videojuegos",
        "Electrodomésticos", "Vehículos", "Ropa", "Hogar",
        "Coleccionables", "Otros"
    )

    val launcherImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) imagenUri = uri
    }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val hoy = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val fechaSeleccionada = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = utcTimeMillis
                }

                val fechaLocal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, fechaSeleccionada.get(Calendar.YEAR))
                    set(Calendar.MONTH, fechaSeleccionada.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, fechaSeleccionada.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                return fechaLocal.timeInMillis >= hoy.timeInMillis
            }
        }
    )

    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = true
    )

    fun construirFechaFinal(): Calendar? {
        val millis = fechaMillis ?: return null
        val hora = horaSeleccionada ?: return null
        val minuto = minutoSeleccionado ?: return null

        val fechaUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = millis
        }

        return Calendar.getInstance().apply {
            set(Calendar.YEAR, fechaUtc.get(Calendar.YEAR))
            set(Calendar.MONTH, fechaUtc.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, fechaUtc.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun fechaTexto(): String {
        val cal = construirFechaFinal() ?: return ""
        return SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(cal.time)
    }

    fun fechaApi(): String {
        val cal = construirFechaFinal() ?: Calendar.getInstance()
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(cal.time)
    }

    fun crearParteImagen(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null

            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())

            MultipartBody.Part.createFormData(
                name = "file",
                filename = "subasta_${System.currentTimeMillis()}.jpg",
                body = requestBody
            )
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Crear subasta", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                error = ""
            },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                error = ""
            },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = precio,
            onValueChange = {
                precio = it
                error = ""
            },
            label = { Text("Precio inicial") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = categoriaExpandida,
            onExpandedChange = { categoriaExpandida = !categoriaExpandida }
        ) {
            OutlinedTextField(
                value = categoria,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpandida)
                },
                modifier = Modifier
                    .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            ExposedDropdownMenu(
                expanded = categoriaExpandida,
                onDismissRequest = { categoriaExpandida = false }
            ) {
                categorias.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            categoria = opcion
                            categoriaExpandida = false
                            error = ""
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fechaTexto(),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Fecha y hora de finalización") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { mostrarFecha = true }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { launcherImagen.launch(arrayOf("image/*")) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                if (imagenUri == null)
                    "Seleccionar foto del producto"
                else
                    "Cambiar foto del producto"
            )
        }

        if (imagenUri != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        ImageView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    update = { imageView ->
                        imageView.setImageURI(imagenUri)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val precioConvertido = precio.toDoubleOrNull()

                when {
                    nombre.isBlank() -> {
                        error = "Ingrese el nombre del producto."
                        return@Button
                    }

                    descripcion.isBlank() -> {
                        error = "Ingrese una descripción del producto."
                        return@Button
                    }

                    precio.isBlank() -> {
                        error = "Ingrese el precio inicial."
                        return@Button
                    }

                    precioConvertido == null || precioConvertido <= 0 -> {
                        error = "Ingrese un precio válido mayor que cero."
                        return@Button
                    }

                    categoria.isBlank() -> {
                        error = "Seleccione una categoría."
                        return@Button
                    }

                    fechaMillis == null -> {
                        error = "Seleccione la fecha de finalización."
                        return@Button
                    }

                    horaSeleccionada == null || minutoSeleccionado == null -> {
                        error = "Seleccione la hora de finalización."
                        return@Button
                    }

                    imagenUri == null -> {
                        error = "Seleccione una foto del producto."
                        return@Button
                    }

                    usuarioId == 0L -> {
                        error = "Debe iniciar sesión para crear una subasta."
                        return@Button
                    }
                }

                val fechaElegida = construirFechaFinal()

                if (fechaElegida == null || fechaElegida.timeInMillis <= System.currentTimeMillis()) {
                    error = "La fecha y hora de finalización debe ser posterior a la actual."
                    return@Button
                }

                cargando = true
                error = ""

                scope.launch {
                    try {
                        val parteImagen = crearParteImagen(context, imagenUri!!)

                        if (parteImagen == null) {
                            error = "No se pudo preparar la imagen."
                            cargando = false
                            return@launch
                        }

                        val responseImagen = RetrofitClient.api.subirImagenSubasta(parteImagen)

                        if (!responseImagen.isSuccessful || responseImagen.body()?.ok != true) {
                            error = responseImagen.body()?.mensaje ?: "No se pudo subir la imagen."
                            cargando = false
                            return@launch
                        }

                        val urlImagen = responseImagen.body()?.url

                        if (urlImagen.isNullOrBlank()) {
                            error = "La API no devolvió la URL de la imagen."
                            cargando = false
                            return@launch
                        }

                        val response = RetrofitClient.api.crearSubasta(
                            SubastaRequest(
                                nombre = nombre,
                                descripcion = descripcion,
                                precioInicial = precioConvertido!!,
                                categoria = categoria,
                                imagen = urlImagen,
                                usuarioId = usuarioId,
                                fechaFin = fechaApi()
                            )
                        )

                        if (response.isSuccessful) {
                            nombre = ""
                            descripcion = ""
                            precio = ""
                            categoria = ""
                            fechaMillis = null
                            horaSeleccionada = null
                            minutoSeleccionado = null
                            imagenUri = null
                            error = ""

                            onAuctionCreated()
                        } else {
                            error = "No se pudo crear la subasta. Revise los datos ingresados."
                        }
                    } catch (e: Exception) {
                        error = "No se pudo conectar con la API."
                    } finally {
                        cargando = false
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (cargando) "Publicando..." else "Publicar subasta")
        }
    }

    if (mostrarFecha) {
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val seleccion = datePickerState.selectedDateMillis

                        if (seleccion == null) {
                            error = "Seleccione una fecha."
                            return@TextButton
                        }

                        fechaMillis = seleccion
                        mostrarFecha = false
                        mostrarHora = true
                    }
                ) {
                    Text("Siguiente")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFecha = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarHora) {
        AlertDialog(
            onDismissRequest = { mostrarHora = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        horaSeleccionada = timePickerState.hour
                        minutoSeleccionado = timePickerState.minute
                        mostrarHora = false
                        error = ""
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarHora = false }) {
                    Text("Cancelar")
                }
            },
            title = {
                Text("Seleccione la hora")
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}