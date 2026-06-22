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
fun MyBidsScreen() {

    val ofertas = listOf(
        "iPhone 13 Pro",
        "PlayStation 5",
        "Apple Watch"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {

            Text(
                text = "Mis Ofertas",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        items(ofertas) { oferta ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = oferta,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Estado: Ganando")
                }
            }
        }
    }
}