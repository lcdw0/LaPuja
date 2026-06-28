package com.example.lapuja.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AuctionItem(

    val nombre: String,

    val descripcion: String,

    precioInicial: Double,

    val imagen: Int,

    val categoria: String,

    val fechaInicio: String

) {

    var precio by mutableStateOf(precioInicial)

    var tiempo by mutableStateOf(30)
    var ganador by mutableStateOf("Nadie")

    var guardado by mutableStateOf(false)

    var ofertas by mutableStateOf(0)

    var estado by mutableStateOf("ACTIVA")

    var iniciada by mutableStateOf(true)

    var idApi: Long = 0L
}