package com.example.lapuja.data

data class Auction(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val initialPrice: Double,
    val currentBid: Double,
    val timeLeft: String,
    val seller: String,
    val isSaved: Boolean = false
)