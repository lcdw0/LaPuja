package com.example.lapuja.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyAuctionsScreen() {

    val subastas = listOf(
        "iPhone 13 Pro",
        "Laptop Gamer",
        "Audífonos Pro"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {

            Text(
                text = "Mis Subastas",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        items(subastas) { subasta ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = subasta,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Estado: Activa")
                }
            }
        }
    }
}