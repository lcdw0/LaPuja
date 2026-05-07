package com.example.lapuja.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class AuctionItem(

    val nombre: String,

    precioInicial: Double,

    val imagen: Int

) {

    var precio by mutableStateOf(precioInicial)

    var tiempo by mutableStateOf(30)

    var ganador by mutableStateOf("Nadie")
}