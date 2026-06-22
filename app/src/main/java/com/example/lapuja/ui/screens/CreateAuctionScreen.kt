package com.example.lapuja.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAuctionScreen() {

    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Tecnología") }
    var descripcion by remember { mutableStateOf("") }
    var precioInicial by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    var mostrarCalendarioInicio by remember {
        mutableStateOf(false)
    }

    var mostrarCalendarioFin by remember {
        mutableStateOf(false)
    }

    val estadoFechaInicio = rememberDatePickerState()
    val estadoFechaFin = rememberDatePickerState()

    val categorias = listOf(
        "Tecnología",
        "Gaming",
        "Gadgets",
        "Vehículos",
        "Coleccionables",
        "Otros"
    )

    fun formatearFecha(millis: Long?): String {

        return if (millis != null) {

            val formato =
                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                )

            formato.format(Date(millis))

        } else {

            ""
        }
    }

    if (mostrarCalendarioInicio) {

        DatePickerDialog(
            onDismissRequest = {
                mostrarCalendarioInicio = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        fechaInicio =
                            formatearFecha(
                                estadoFechaInicio.selectedDateMillis
                            )

                        mostrarCalendarioInicio = false
                    }
                ) {

                    Text("Aceptar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarCalendarioInicio = false
                    }
                ) {

                    Text("Cancelar")
                }
            }

        ) {

            DatePicker(
                state = estadoFechaInicio
            )
        }
    }

    if (mostrarCalendarioFin) {

        DatePickerDialog(
            onDismissRequest = {
                mostrarCalendarioFin = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        fechaFin =
                            formatearFecha(
                                estadoFechaFin.selectedDateMillis
                            )

                        mostrarCalendarioFin = false
                    }
                ) {

                    Text("Aceptar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarCalendarioFin = false
                    }
                ) {

                    Text("Cancelar")
                }
            }

        ) {

            DatePicker(
                state = estadoFechaFin
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {

            Text(
                text = "Crear Subasta",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),

                shape = RoundedCornerShape(22.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray)
                        .clickable { },

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "📷 Agregar foto del artículo",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                },

                label = {
                    Text("Nombre del producto")
                },

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,

                onExpandedChange = {
                    expanded = !expanded
                }
            ) {

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { },

                    readOnly = true,

                    label = {
                        Text("Categoría")
                    },

                    trailingIcon = {

                        ExposedDropdownMenuDefaults
                            .TrailingIcon(
                                expanded = expanded
                            )
                    },

                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),

                    shape = RoundedCornerShape(14.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,

                    onDismissRequest = {
                        expanded = false
                    }
                ) {

                    categorias.forEach { item ->

                        DropdownMenuItem(
                            text = {
                                Text(item)
                            },

                            onClick = {

                                categoria = item

                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = descripcion,

                onValueChange = {
                    descripcion = it
                },

                label = {
                    Text("Descripción")
                },

                modifier = Modifier.fillMaxWidth(),

                minLines = 3,

                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = precioInicial,

                onValueChange = {
                    precioInicial = it
                },

                label = {
                    Text("Precio inicial")
                },

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        mostrarCalendarioInicio = true
                    }
            ) {

                OutlinedTextField(
                    value = fechaInicio,
                    onValueChange = { },

                    readOnly = true,
                    enabled = false,

                    label = {
                        Text("Fecha de inicio")
                    },

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(14.dp),

                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor =
                            MaterialTheme.colorScheme.onSurface,

                        disabledBorderColor =
                            MaterialTheme.colorScheme.outline,

                        disabledLabelColor =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        mostrarCalendarioFin = true
                    }
            ) {

                OutlinedTextField(
                    value = fechaFin,
                    onValueChange = { },

                    readOnly = true,
                    enabled = false,

                    label = {
                        Text("Fecha de finalización")
                    },

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(14.dp),

                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor =
                            MaterialTheme.colorScheme.onSurface,

                        disabledBorderColor =
                            MaterialTheme.colorScheme.outline,

                        disabledLabelColor =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (mensaje.isNotEmpty()) {

                Text(
                    text = mensaje,

                    color =
                        if (
                            mensaje.contains("correctamente")
                        ) {

                            Color(0xFF4CAF50)

                        } else {

                            Color(0xFFFF6B81)
                        },

                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {

                    mensaje =
                        if (
                            nombre.isBlank() ||
                            descripcion.isBlank() ||
                            precioInicial.isBlank() ||
                            fechaInicio.isBlank() ||
                            fechaFin.isBlank()
                        ) {

                            "Completá todos los campos para publicar la subasta"

                        } else {

                            "Subasta publicada correctamente"
                        }
                },

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(14.dp)
            ) {

                Text("Publicar subasta")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}