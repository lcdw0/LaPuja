package com.example.lapuja.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNav(navController: NavController) {

    NavigationBar {

        val currentRoute =
            navController.currentBackStackEntryAsState()
                .value?.destination?.route

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

        NavigationBarItem(
            selected = currentRoute == "create_auction",
            onClick = {
                navController.navigate("create_auction")
            },
            label = {
                Text("Crear")
            },
            icon = {
                Text("➕")
            }
        )

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