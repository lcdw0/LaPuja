package com.example.lapuja

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.lapuja.components.BottomNav
import com.example.lapuja.data.Bid
import com.example.lapuja.screens.*
import com.example.lapuja.ui.theme.LaPujaTheme

class MainActivity : ComponentActivity() {

    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("app", MODE_PRIVATE)

        // saldo inicial
        if (!prefs.contains("saldo")) {
            prefs.edit().putFloat("saldo", 10000.0f).apply()
        }

        enableEdgeToEdge()

        setContent {

            LaPujaTheme {

                MainScreen(prefs)
            }
        }
    }
}

@Composable
fun MainScreen(prefs: SharedPreferences) {

    val navController = rememberNavController()

    val historial = remember {

        mutableStateListOf<Bid>()
    }

    // 🔐 verificar sesión
    val usuarioGuardado =
        prefs.getString("correo", null)

    val inicio = if (usuarioGuardado == null) {

        "login"

    } else {

        "home"
    }

    Scaffold(

        bottomBar = {

            val currentRoute =
                navController.currentBackStackEntryAsState()
                    .value?.destination?.route

            // ocultar navbar en login/register
            if (
                currentRoute != "login" &&
                currentRoute != "register"
            ) {

                BottomNav(navController)
            }
        }

    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = inicio,
            modifier = Modifier.padding(paddingValues)
        ) {

            // 🔐 LOGIN
            composable("login") {

                LoginScreen(
                    navController,
                    prefs
                )
            }

            // 📝 REGISTER
            composable("register") {

                RegisterScreen(
                    navController,
                    prefs
                )
            }

            // 🏠 HOME
            composable("home") {

                HomeScreen(navController)
            }

            // 🔥 SUBASTAS
            composable("auction") {

                AuctionScreen(
                    prefs = prefs,
                    historial = historial
                )
            }

            // 📜 HISTORIAL
            composable("history") {

                HistoryScreen(historial)
            }

            // 👤 PERFIL
            composable("profile") {

                ProfileScreen(

                        prefs = prefs,
                        navController = navController

                )
            }
        }
    }
}