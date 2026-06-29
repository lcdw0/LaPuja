package com.example.lapuja

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lapuja.components.BottomNav
import com.example.lapuja.data.AuctionItem
import com.example.lapuja.data.Bid
import com.example.lapuja.ui.screens.*
import com.example.lapuja.ui.theme.LaPujaTheme
import com.example.lapuja.ui.dashboard.DashboardScreen

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("app", MODE_PRIVATE)

        if (!prefs.contains("saldo")) {
            prefs.edit()
                .putFloat("saldo", 10000.0f)
                .apply()
        }

        enableEdgeToEdge()

        setContent {
            LaPujaTheme {
                MainScreen(prefs = prefs)
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

    val productos = remember {
        mutableStateListOf<AuctionItem>()
    }

    val usuarioGuardado = prefs.getString("correo", null)

    val startDestination = if (usuarioGuardado == null) {
        "login"
    } else {
        "home"
    }

    Scaffold(
        bottomBar = {
            val currentRoute = navController
                .currentBackStackEntryAsState()
                .value
                ?.destination
                ?.route

            if (
                currentRoute != "login" &&
                currentRoute != "register" &&
                currentRoute?.startsWith("auction_detail") != true
            ) {
                BottomNav(navController = navController)
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    prefs = prefs
                )
            }

            composable("register") {
                RegisterScreen(
                    navController = navController,
                    prefs = prefs
                )
            }

            composable("home") {
                HomeScreen(
                    navController = navController
                )
            }

            composable("auction") {
                AuctionScreen(
                    prefs = prefs,
                    historial = historial,
                    productos = productos,
                    onAuctionClick = { auction ->
                        navController.navigate("auction_detail/${auction.idApi}")
                    }
                )
            }

            composable("create_auction") {
                CreateAuctionScreen(
                    productos = productos,
                    onAuctionCreated = {
                        navController.navigate("auction") {
                            popUpTo("create_auction") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(
                route = "auction_detail/{subastaId}",
                arguments = listOf(
                    navArgument("subastaId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->

                val subastaId = backStackEntry.arguments?.getLong("subastaId") ?: 0L

                AuctionDetailScreen(
                    subastaId = subastaId,
                    prefs = prefs,
                    historial = historial,
                    navController = navController,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("history") {
                HistoryScreen(
                    historial = historial,
                    navController = navController
                )
            }

            composable("profile") {
                ProfileScreen(
                    prefs = prefs,
                    navController = navController
                )
            }

            composable("edit_profile") {
                EditProfileScreen(
                    prefs = prefs,
                    navController = navController
                )
            }

            composable("my_auctions") {
                MyAuctionsScreen(
                    productos = productos,
                    navController = navController
                )
            }

            composable("my_bids") {
                MyBidsScreen(
                    historial = historial,
                    navController = navController
                )
            }

            composable("payment_methods") {
                PaymentMethodsScreen()
            }

            composable("saved_auctions") {
                SavedAuctionsScreen(
                    navController = navController
                )
            }

            composable("recharge_wallet") {
                RechargeWalletScreen(
                    navController = navController,
                    prefs = prefs
                )
            }

            composable("wallet_history") {
                WalletHistoryScreen()
            }

            composable("held_funds") {
                HeldFundsScreen()
            }

            composable(
                route = "edit_auction/{subastaId}",
                arguments = listOf(
                    navArgument("subastaId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->

                val subastaId = backStackEntry.arguments?.getLong("subastaId") ?: 0L

                EditAuctionScreen(
                    subastaId = subastaId,
                    onAuctionUpdated = {
                        navController.navigate("my_auctions") {
                            popUpTo("edit_auction/{subastaId}") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            fun obtenerUsuarioIdDesdePrefs(prefs: SharedPreferences): Long {
                return when (val id = prefs.all["usuarioId"]) {
                    is Long -> id
                    is Int -> id.toLong()
                    is String -> id.toLongOrNull() ?: 0L
                    else -> 0L
                }
            }

            composable("dashboard") {
                val usuarioId = obtenerUsuarioIdDesdePrefs(prefs)

                DashboardScreen(
                    usuarioId = usuarioId
                )
            }
        }
    }
}