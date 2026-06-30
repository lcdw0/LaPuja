package com.example.lapuja.utils

import java.text.NumberFormat
import java.util.Locale

fun nombreCompleto(
    nombre: String?,
    apellidos: String?
): String {
    return listOfNotNull(nombre, apellidos)
        .joinToString(" ")
        .trim()
        .ifBlank { "Usuario LaPuja" }
}

fun formatearCordobas(valor: Double?): String {
    val formato = NumberFormat.getNumberInstance(Locale.US)
    return "C$ ${formato.format(valor ?: 0.0)}"
}

fun formatearCordobasDecimal(valor: Double?): String {
    val formato = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    return "C$ ${formato.format(valor ?: 0.0)}"
}