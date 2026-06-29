package com.example.lapuja.ui.screens

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.lapuja.data.AuctionItem
import com.example.lapuja.data.remote.RetrofitClient
import com.example.lapuja.data.remote.SubastaImagenRequest
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

    val imagenesSeleccionadas = remember { mutableStateListOf<Uri>() }

    var imagenArrastrandoIndex by remember { mutableStateOf<Int?>(null) }
    var desplazamientoX by remember { mutableStateOf(0f) }

    val categorias = listOf(
        "Tecnología", "Computadoras", "Celulares", "Videojuegos",
        "Electrodomésticos", "Vehículos", "Ropa", "Hogar",
        "Coleccionables", "Otros"
    )

    val launcherImagenes = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val disponibles = 10 - imagenesSeleccionadas.size
            val nuevas = uris.take(disponibles)

            imagenesSeleccionadas.addAll(nuevas)
            error = ""

            if (uris.size > disponibles) {
                error = "Solo se permiten hasta 10 imágenes por subasta."
            }
        }
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

    fun moverImagen(origen: Int, destino: Int) {
        if (origen !in imagenesSeleccionadas.indices || destino !in imagenesSeleccionadas.indices) {
            return
        }

        val imagen = imagenesSeleccionadas.removeAt(origen)
        imagenesSeleccionadas.add(destino, imagen)
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Imágenes del producto", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecciona de 1 a 10 imágenes. La primera imagen será la portada. Mantén presionada una imagen para arrastrarla y cambiar el orden.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                if (imagenesSeleccionadas.size >= 10) {
                    error = "Ya seleccionaste el máximo de 10 imágenes."
                } else {
                    launcherImagenes.launch(arrayOf("image/*"))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Agregar imágenes (${imagenesSeleccionadas.size}/10)")
        }

        if (imagenesSeleccionadas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(imagenesSeleccionadas) { index, uri ->
                    val arrastrando = imagenArrastrandoIndex == index

                    Card(
                        modifier = Modifier
                            .width(165.dp)
                            .zIndex(if (arrastrando) 1f else 0f)
                            .graphicsLayer {
                                translationX = if (arrastrando) desplazamientoX else 0f
                                scaleX = if (arrastrando) 1.04f else 1f
                                scaleY = if (arrastrando) 1.04f else 1f
                            }
                            .pointerInput(index, imagenesSeleccionadas.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        imagenArrastrandoIndex = index
                                        desplazamientoX = 0f
                                    },
                                    onDragEnd = {
                                        imagenArrastrandoIndex = null
                                        desplazamientoX = 0f
                                    },
                                    onDragCancel = {
                                        imagenArrastrandoIndex = null
                                        desplazamientoX = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()

                                        desplazamientoX += dragAmount.x

                                        val actual = imagenArrastrandoIndex ?: index
                                        val limite = 95f

                                        if (desplazamientoX > limite && actual < imagenesSeleccionadas.lastIndex) {
                                            moverImagen(actual, actual + 1)
                                            imagenArrastrandoIndex = actual + 1
                                            desplazamientoX = 0f
                                        }

                                        if (desplazamientoX < -limite && actual > 0) {
                                            moverImagen(actual, actual - 1)
                                            imagenArrastrandoIndex = actual - 1
                                            desplazamientoX = 0f
                                        }
                                    }
                                )
                            },
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                shape = RoundedCornerShape(14.dp)
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
                                        imageView.setImageURI(uri)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (index == 0)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (index == 0) "Portada" else "Imagen ${index + 1}",
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Mantén presionado para mover",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    imagenesSeleccionadas.removeAt(index)
                                    error = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
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

                    imagenesSeleccionadas.isEmpty() -> {
                        error = "Seleccione al menos una imagen del producto."
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
                        val urlsSubidas = mutableListOf<String>()

                        for (uri in imagenesSeleccionadas) {
                            val parteImagen = crearParteImagen(context, uri)

                            if (parteImagen == null) {
                                error = "No se pudo preparar una de las imágenes."
                                cargando = false
                                return@launch
                            }

                            val responseImagen = RetrofitClient.api.subirImagenSubasta(parteImagen)

                            if (!responseImagen.isSuccessful || responseImagen.body()?.ok != true) {
                                error = responseImagen.body()?.mensaje ?: "No se pudo subir una imagen."
                                cargando = false
                                return@launch
                            }

                            val urlImagen = responseImagen.body()?.url

                            if (urlImagen.isNullOrBlank()) {
                                error = "La API no devolvió la URL de una imagen."
                                cargando = false
                                return@launch
                            }

                            urlsSubidas.add(urlImagen)
                        }

                        val responseSubasta = RetrofitClient.api.crearSubasta(
                            SubastaRequest(
                                nombre = nombre,
                                descripcion = descripcion,
                                precioInicial = precioConvertido!!,
                                categoria = categoria,
                                imagen = urlsSubidas.first(),
                                usuarioId = usuarioId,
                                fechaFin = fechaApi()
                            )
                        )

                        if (!responseSubasta.isSuccessful || responseSubasta.body() == null) {
                            error = "No se pudo crear la subasta. Revise los datos ingresados."
                            cargando = false
                            return@launch
                        }

                        val subastaCreada = responseSubasta.body()!!
                        val subastaId = subastaCreada.id

                        for (url in urlsSubidas) {
                            val responseAgregarImagen = RetrofitClient.api.agregarImagenSubasta(
                                subastaId = subastaId,
                                request = SubastaImagenRequest(url = url)
                            )

                            if (!responseAgregarImagen.isSuccessful) {
                                error = "La subasta fue creada, pero no se pudieron asociar todas las imágenes."
                                cargando = false
                                return@launch
                            }
                        }

                        nombre = ""
                        descripcion = ""
                        precio = ""
                        categoria = ""
                        fechaMillis = null
                        horaSeleccionada = null
                        minutoSeleccionado = null
                        imagenesSeleccionadas.clear()
                        error = ""

                        onAuctionCreated()

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