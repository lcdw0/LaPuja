package com.example.lapuja.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNav(navController: NavController) {

    NavigationBar {

        val currentRoute =
            navController.currentBackStackEntryAsState()
                .value?.destination?.route

        // 🏠 Inicio
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home")
            },
            label = {
                Text("Inicio")
            },
            icon = {
                Text("🏠")
            }
        )

        // 🔥 Subastas
        NavigationBarItem(
            selected = currentRoute == "auction",
            onClick = {
                navController.navigate("auction")
            },
            label = {
                Text("Subastas")
            },
            icon = {
                Text("🔥")
            }
        )

        // 📜 Historial
        NavigationBarItem(
            selected = currentRoute == "history",
            onClick = {
                navController.navigate("history")
            },
            label = {
                Text("Historial")
            },
            icon = {
                Text("📜")
            }
        )

        // 👤 Perfil
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile")
            },
            label = {
                Text("Perfil")
            },
            icon = {
                Text("👤")
            }
        )
    }
}